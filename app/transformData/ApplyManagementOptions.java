package util;

import play.*;
import java.util.*;

//------------------------------------------------------------------------------
// Apply Management Options
//
// Takes an in-memory CDL and applies management options on top based on statistical 
//	averages
//
// TODO: All of these numbers are FAKE and should be tweaked/refined.
//------------------------------------------------------------------------------
public class ApplyManagementOptions
{
	// Could do a global tillage percentage but maybe more flexible this way?
	private static float mCornTillPercent = 0.64f;
	private static float mSoyTillPercent = 0.64f;
	private static float mAlfalfaTillPercent = 0.64f;
	private static float mGrassTillPercent = 0.0f;

	private static float mCornFertilizedPercent = 0.99f;
	private static float mSoyFertilizedPercent = 0.99f;
	private static float mAlfalfaFertilizedPercent = 0.9f;
	private static float mGrassFertilizedPercent = 0.5f;
	
	// TODO: redo this if needed. Could factor in distance to dairy as well as other?
	private static float mCornManurePercent = 0.5f;
	private static float mSoyManurePercent = 0.5f;
	private static float mAlfalfaManurePercent = 0.5f;
	private static float mGrassManurePercent = 0.0f;
	
	private static float mCornFallManurePercent = 0.5f;
	private static float mSoyFallManurePercent = 0.5f;
	private static float mAlfalfaFallManurePercent = 0.5f;
	private static float mGrassFallManurePercent = 0.5f;
	
	private static float mCornCoverCropPercent = 0.5f;
	private static float mSoyCoverCropPercent = 0.5f;
	private static float mAlfalfaCoverCropPercent = 0.5f;
	private static float mGrassCoverCropPercent = 1.0f; // Treating grass as its own cover crop make sense?
	
	private static float mAllContourPercent = 0.5f;
	private static float mAllTerracePercent = 0.5f;
	//--------------------------------------------------------------------------
	public static void now() {
		
		Logger.info("Adding management options to CDL:");
		
		Layer_Base cdl = Layer_Base.getLayer("cdl_2012");

		if (cdl == null) {
			try {
				
				cdl = new Layer_Integer("cdl_2012"); cdl.init();
			}
			catch (Exception e) {
				Logger.info(e.toString());
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
		
		// NOTE: setting the same seed so the results are the same every run unless the logic below changes
		Random rnd = new Random(0);
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				int rasterCell = rasterData[y][x];
				int options = 0;
				if ((rasterCell & totalMask) > 0) {
					if (rnd.nextFloat() > mAllContourPercent) 		options = ManagementOptions.E_Contour.setOn(options);
					if (rnd.nextFloat() > mAllTerracePercent) 		options = ManagementOptions.E_Terrace.setOn(options);
				}
				if ((rasterCell & cornMask) > 0) {
					if (rnd.nextFloat() > mCornTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() > mCornFertilizedPercent) 	options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() > mCornManurePercent) 		options = ManagementOptions.E_Manure.setOn(options);
					if (rnd.nextFloat() > mCornFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() > mCornCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & soyMask) > 0) {
					if (rnd.nextFloat() > mSoyTillPercent) 			options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() > mSoyFertilizedPercent) 	options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() > mSoyManurePercent) 		options = ManagementOptions.E_Manure.setOn(options);
					if (rnd.nextFloat() > mSoyFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() > mSoyCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & alfalfaMask) > 0) {
					if (rnd.nextFloat() > mAlfalfaTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() > mAlfalfaFertilizedPercent)options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() > mAlfalfaManurePercent) 	options = ManagementOptions.E_Manure.setOn(options);
					if (rnd.nextFloat() > mAlfalfaFallManurePercent)options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() > mAlfalfaCoverCropPercent) options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				else if ((rasterCell & grassMask) > 0) {
					if (rnd.nextFloat() > mGrassTillPercent) 		options = ManagementOptions.E_Till.setOn(options);
					if (rnd.nextFloat() > mGrassFertilizedPercent) 	options = ManagementOptions.E_Fertilizer.setOn(options);
					if (rnd.nextFloat() > mGrassManurePercent) 		options = ManagementOptions.E_Manure.setOn(options);
					if (rnd.nextFloat() > mGrassFallManurePercent) 	options = ManagementOptions.E_FallManure.setOn(options);
					if (rnd.nextFloat() > mGrassCoverCropPercent) 	options = ManagementOptions.E_CoverCrop.setOn(options);
				}
				// Now set the potentially modified rasterCell back onto the array...
				rasterData[y][x] = rasterCell | options;
			}
		}
	}
	
}
