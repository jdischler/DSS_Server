package util;

import play.*;
import java.util.*;

//------------------------------------------------------------------------------
// Apply Management Options
//
// Takes an in-memory CDL and applies management options on top based on statistical 
//	averages
//
//------------------------------------------------------------------------------
public class ApplyManagementOptions
{
	// Could do a global tillage percentage but maybe more flexible this way?
	private static float mCornTillPercent = 0.64f;
	private static float mSoyTillPercent = 0.64f;
	private static float mAlfalfaTillPercent = 0.20f;
	private static float mGrassTillPercent = 0.05f;

	private static float mCornFertilizedPercent = 1.0f;
	private static float mSoyFertilizedPercent = 0.50f;
	private static float mAlfalfaFertilizedPercent = 0.20f;
	private static float mGrassFertilizedPercent = 0.20f;
	
	// Note: fall manure percentages ONLY kick in if manure is used in the first place.
	private static float mCornFallManurePercent = 0.25f;
	private static float mSoyFallManurePercent = 0.15f;
	private static float mAlfalfaFallManurePercent = 0.25f;
	private static float mGrassFallManurePercent = 0.25f;
	
	private static float mCornCoverCropPercent = 0.10f;
	private static float mSoyCoverCropPercent = 0.10f;
	private static float mAlfalfaCoverCropPercent = 1.0f;
	private static float mGrassCoverCropPercent = 1.0f;
	
	private static float mSteepSlopeGrade = 5; // >=5%
	private static float mSteepContourPercent = 0.30f;
	
	//--------------------------------------------------------------------------
	public static void now() {
		
		Logger.info("Adding management options to CDL:");
		
		Layer_Base cdl = Layer_Base.getLayer("cdl_2012");

		if (cdl == null) {
			try {
				
				cdl = new Layer_Integer("cdl_2012"); cdl.init();
			}
			catch (Exception e) {
				Logger.warn(e.toString());
				return;
			}
		}

		if (cdl == null) return;
		
		int grassMask = ((Layer_Integer)cdl).convertStringsToMask("grass");
		int cornMask = ((Layer_Integer)cdl).convertStringsToMask("corn");
		int soyMask = ((Layer_Integer)cdl).convertStringsToMask("soy");
		int alfalfaMask = ((Layer_Integer)cdl).convertStringsToMask("alfalfa");
		int totalMask = grassMask | cornMask | soyMask | alfalfaMask;

		int width = cdl.getWidth();
		int height = cdl.getHeight();
		int[][] rasterData = cdl.getIntData();
		
		// Get some other layers we'll query to help decide what to put where, 
		//	e.g., flat land is not likely to be contoured or terraced...
		//			manure is more likely in areas with higher density of dairies
		Layer_Base slope = Layer_Base.getLayer("slope");
		Layer_Base dairy = Layer_Base.getLayer("dairy");
		
		float[][] slopeData = null, dairyData = null; 
		if (slope == null) return;
		slopeData = slope.getFloatData();
		if (slopeData == null) return;
		
		if (dairy == null) return;
		dairyData = dairy.getFloatData();
		if (dairyData == null) return;
		
		// NOTE: setting the same seed so the results are the same every run unless the logic below changes
		Random rnd = new Random(0);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				int rasterCell = rasterData[y][x];
				float slopeCell = slopeData[y][x];
				float dairyDensity = dairyData[y][x];
				
				int options = 0;
				if ((rasterCell & totalMask) == 0) {
					continue;
				}
				// apply countour based on slope...
				if (slopeCell >= mSteepSlopeGrade) {
					if (rnd.nextFloat() < mSteepContourPercent)	options = ManagementOptions.E_Contour.setOn(options);
				}
				// dairy density is from 0-8, currently. density of 0 has 0% manure chance...
				//	8 has 100% chance
				// apply manure based on dairy density
				float manureChance = dairyDensity / 8.0f;
				if (manureChance < 0) manureChance = 0;
				if (manureChance > 8) manureChance = 1;
				if (rnd.nextFloat() < manureChance) 			options = ManagementOptions.E_Manure.setOn(options);
				
				// Apply others per crop
				float fertilizerChance = 0;
				if ((rasterCell & cornMask) > 0) {
					fertilizerChance = mCornFertilizedPercent;
					if (rnd.nextFloat() < mCornTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mCornFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() < mCornCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & soyMask) > 0) {
					fertilizerChance = mSoyFertilizedPercent;
					if (rnd.nextFloat() < mSoyTillPercent) 			options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mSoyFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() > mSoyCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & alfalfaMask) > 0) {
					fertilizerChance = mAlfalfaFertilizedPercent;
					if (rnd.nextFloat() < mAlfalfaTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mAlfalfaFallManurePercent)options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() < mAlfalfaCoverCropPercent) options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & grassMask) > 0) {
					fertilizerChance = mGrassFertilizedPercent;
					if (rnd.nextFloat() < mGrassTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mGrassFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() < mGrassCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}

				// areas with high manure (high dairy density) increases chance of fertilizer usage 
				// if fertChance = 20% and manureChance = 100%, then fertChance becomes average of, or 60%
				if (manureChance > fertilizerChance) {
					fertilizerChance = (manureChance + fertilizerChance) * 0.5f;
				}
				if (rnd.nextFloat() < fertilizerChance) 	options = ManagementOptions.E_Fertilizer.setOn(options);

				// Now set the potentially modified rasterCell back onto the array...
				rasterData[y][x] = rasterCell | options;
			}
		}
	}
	
}
