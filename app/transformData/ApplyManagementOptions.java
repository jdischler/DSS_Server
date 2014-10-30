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
	//private static float mAlfalfaTillPercent = 0.64f;
	private static float mAlfalfaTillPercent = 0.20f;
	//private static float mGrassTillPercent = 0.0f;
	private static float mGrassTillPercent = 0.05f;

	//private static float mCornFertilizedPercent = 0.99f;
	//private static float mSoyFertilizedPercent = 0.99f;
	//private static float mAlfalfaFertilizedPercent = 0.9f;
	//private static float mGrassFertilizedPercent = 0.5f;
	private static float mCornFertilizedPercent = 1.0f;
	private static float mSoyFertilizedPercent = 0.60f;
	private static float mAlfalfaFertilizedPercent = 0.20f;
	private static float mGrassFertilizedPercent = 0.20f;
	
	// Scale manure chance by density of dairy...
	private static float mLowManureMult = 1.2f;
	private static float mMedManureMult = 1.4f; 	
	private static float mHighManureMult = 1.8f; 
	
	//private static float mCornManurePercent = 0.1f;
	//private static float mSoyManurePercent = 0.1f;
	//private static float mAlfalfaManurePercent = 0.04f;
	//private static float mGrassManurePercent = 0.01f;
	private static float mCornManurePercent = 0.20f;
	private static float mSoyManurePercent = 0.10f;
	private static float mAlfalfaManurePercent = 0.20f;
	private static float mGrassManurePercent = 0.20f;
	
	// Note: fall manure percentages ONLY kick in if manure is used in the first place.
	//private static float mCornFallManurePercent = 0.5f;
	//private static float mSoyFallManurePercent = 0.5f;
	//private static float mAlfalfaFallManurePercent = 0.5f;
	//private static float mGrassFallManurePercent = 0.1f;
	private static float mCornFallManurePercent = 0.25f;
	private static float mSoyFallManurePercent = 0.15f;
	private static float mAlfalfaFallManurePercent = 0.15f;
	private static float mGrassFallManurePercent = 0.15f;
	
	//private static float mCornCoverCropPercent = 0.07f;
	//private static float mSoyCoverCropPercent = 0.07f;
	//private static float mAlfalfaCoverCropPercent = 1.0f;
	//private static float mGrassCoverCropPercent = 1.0f;
	private static float mCornCoverCropPercent = 0.10f;
	private static float mSoyCoverCropPercent = 0.10f;
	private static float mAlfalfaCoverCropPercent = 1.0f;
	private static float mGrassCoverCropPercent = 1.0f;
	
	private static float mSteepSlopeGrade = 5; // >=5%
	private static float mSteepContourPercent = 0.30f;
	private static float mSteepTerracePercent = 0.30f;
	private static float mModerateSlopeGrade = 0; // <5%
	//private static float mModerateContourPercent = 0.3f;
	//private static float mModerateTerracePercent = 0.1f;
	private static float mModerateContourPercent = 0.10f;
	private static float mModerateTerracePercent = 0.10f;
	
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
				if ((rasterCell & totalMask) > 0) {
					if (slopeCell >= mSteepSlopeGrade) {
						if (rnd.nextFloat() < mSteepContourPercent)	options = ManagementOptions.E_Contour.setOn(options);
						if (rnd.nextFloat() < mSteepTerracePercent) options = ManagementOptions.E_Terrace.setOn(options);
					}
					else if (slopeCell >= mModerateSlopeGrade) {
						if (rnd.nextFloat() < mModerateContourPercent)	options = ManagementOptions.E_Contour.setOn(options);
						if (rnd.nextFloat() < mModerateTerracePercent)	options = ManagementOptions.E_Terrace.setOn(options);
					}
				}
				// manure modifier
				float manureChance = 1.0f;
				if ((rasterCell & cornMask) > 0) {
					manureChance = mCornManurePercent;
					if (rnd.nextFloat() < mCornTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mCornFertilizedPercent) 	options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() < mCornFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() < mCornCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & soyMask) > 0) {
					manureChance = mSoyManurePercent;
					if (rnd.nextFloat() < mSoyTillPercent) 			options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mSoyFertilizedPercent) 	options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() < mSoyFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() > mSoyCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & alfalfaMask) > 0) {
					manureChance = mAlfalfaManurePercent;
					if (rnd.nextFloat() < mAlfalfaTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mAlfalfaFertilizedPercent)options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() < mAlfalfaFallManurePercent)options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() < mAlfalfaCoverCropPercent) options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & grassMask) > 0) {
					manureChance = mGrassManurePercent;
					if (rnd.nextFloat() < mGrassTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() < mGrassFertilizedPercent) 	options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() < mGrassFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() < mGrassCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				// scale manure chance based on density of dairies...
				if (dairyDensity > 6) manureChance *= mHighManureMult;
				else if (dairyDensity >= 3) manureChance *= mMedManureMult;
				else if (dairyDensity >= 1) manureChance *= mLowManureMult;
				if (rnd.nextFloat() < manureChance) 			options = ManagementOptions.E_Manure.setOn(options);

				// Now set the potentially modified rasterCell back onto the array...
				rasterData[y][x] = rasterCell | options;
			}
		}
	}
	
}
