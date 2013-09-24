package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
//import java.lang.reflect.Array;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses proportion of Ag at watershed scale to calculate Nitrogen and Phosphorus
// This model is from unpublished work by Tim University of Wisconsin Madison
// Input is crop rotation layer 
// Outputs are Nitrogen and Phosphorus for each watershed
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_NitrogenPhosphorus_New extends Model_Base
{
	private static final String mNitrogenModelFile = "nitrogen";
	private static final String mPhosphorusModelFile = "phosphorus";
	
	// Number of watersheds in study area
	private static final int mNumWatersheds = 31;

	//--------------------------------------------------------------------------
	public List<ModelResult> run(int[][] rotationData, int width, int height, String destFolder) {
		
Logger.info(">>> Computing Model Nitrogen / Phosphorus");
long timeStart = System.currentTimeMillis();
		
		int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		
		int[] countCellsInWatershed = new int[mNumWatersheds];
		int[] countAgCellsInWatershed = new int[mNumWatersheds];
		// simplified bins, per watershed calculation
		float[] nitrogen = new float[mNumWatersheds];
		float[] phosphorus = new float[mNumWatersheds];
		// full raster save process...
		float [][] nitrogenData = new float[height][width];
		float [][] phosphorusData = new float[height][width];
Logger.info("  > Allocated memory for Nitrogen / Phosphorus");

		int[][] watersheds = Layer_Base.getLayer("watersheds").getIntData();

		// 1st step...analyze whole watershed layer...count number of cells in each 
		//	watershed as well as number of those cells that are ag.
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int watershedIdx = watersheds[y][x]; 
				if (watershedIdx != 0) {
					// watershed index zero is reserved for no-data
					watershedIdx--;
					countCellsInWatershed[watershedIdx]++;
					if ((rotationData[y][x] & Ag_Mask) > 0) {
						countAgCellsInWatershed[watershedIdx]++;
					}
				}
			}
		}
			
		// 2nd step...compute proportion of ag and estimate nitrogen and phosphorus
		for (int i = 0; i < mNumWatersheds; i++) {
			float proportionAg = countAgCellsInWatershed[i] / (float)countCellsInWatershed[i];
			nitrogen[i] = (float)Math.pow(10, 1.13f * proportionAg - 0.23f);
			phosphorus[i] = (float)Math.pow(10, 0.79f * proportionAg - 1.44f);
		}
		
		// 3rd step...fill in a full-sized raster with those values so they can be used
		//	to compute heatmaps or be analyzed with the standard code-paths...

		float n = 0, p = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int watershedIdx = watersheds[y][x]; 
				if (watershedIdx != 0) {
					// watershed index zero is reserved for no-data
					watershedIdx--;
					nitrogenData[y][x] = nitrogen[watershedIdx];
					phosphorusData[y][x] = phosphorus[watershedIdx];
				}
				else {
					nitrogenData[y][x] = -9999.0f;
					phosphorusData[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult("nitrogen", destFolder, nitrogenData, width, height));
		results.add(new ModelResult("phosphorus", destFolder, phosphorusData, width, height));
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model_Nitrogen_Phosphorus is finished - timing: " + Float.toString(timeSec));

		return results;
	}
}

