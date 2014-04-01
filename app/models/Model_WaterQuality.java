package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//import org.codehaus.jackson.*;
//import org.codehaus.jackson.node.*;
//import java.lang.reflect.array;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program calculates phosphorus for each pixel at watershed scale and then sum them up at watershed level (Kg per year)
// Input is crop rotation layer 
// Version 12/20/2013
//
//------------------------------------------------------------------------------
public class Model_WaterQuality extends Model_Base
{

	private static final String mPhosphorusModelFile = "water_quality";
	// Number of watersheds in study area
	private static final int mNumWatersheds = 140;
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) 
	{

//Logger.info(">>> Computing Model Nitrogen / Phosphorus");
Logger.info(">>> Computing Water_Quality Model");
long timeStart = System.currentTimeMillis();
		
		// Spatial Layers
		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012");
		float[][] Rivers = Layer_Base.getLayer("rivers").getFloatData();
		int[][] watersheds = Layer_Base.getLayer("watersheds").getIntData();
		// Id for tracking watershed
		int watershedIdx = 0;
		
		// Mask
		// Grass
		int Grass_Mask = cdl.convertStringsToMask("grass");
		// Alfalfa
		int Alfalfa_Mask = cdl.convertStringsToMask("alfalfa");
		//int mGrassMask = Grass_Mask | Alfalfa_Mask;	
		// Forest
		int Forest_Mask = cdl.convertStringsToMask("woodland");
		// Ag
		//inGRAINS = 2, inVEGGIES = 3, inTREECROP = 4, inOTHERCROP = 15, inSOY =  16, inCORN_GRAIN = 18, inSOY_GRAIN = 19, inOIL = 21;
		int Ag_Mask = 1 + 2 + 4 + 8 + 16384 + 32768 + 131072 + 262144 + 1048576;
		// Urban
		int Urban_Mask = cdl.convertStringsToMask("urban");
		int SubUrban_Mask = cdl.convertStringsToMask("suburban");
		Urban_Mask = Urban_Mask | SubUrban_Mask;
		// Total Mask
		//int Corn_Mask = cdl.convertStringsToMask("corn");
		//int Soy_Mask = cdl.convertStringsToMask("soy");
		//int Alfalfa_Mask = cdl.convertStringsToMask("Alfalfa");
		//int TotalMask = Grass_Mask | Alfalfa_Mask | Forest_Mask | Ag_Mask | Urban_Mask;
		//int TotalMask = mAgMask | mGrassMask;
		
		// Arrays to sum phosphorus within each watershed
		int[] CountCellsInWatershed = new int[mNumWatersheds];
		// Arrays to save phosphorus at watershed scale
		float[] Phosphorus = new float[mNumWatersheds];
		// Arrays to save phosphorus at cell base
		float[][] PhosphorusData = new float[height][width];
			
		// P-export coefficient for Ag, grass, forest, Alfalfa and urban (Kg per hectare per year)
		float P_flux = 0;
		// Distance of cells from river in terms of area for Ag, grass, forest, Alfalfa and urban
		int Dist = 0;
		// Transmission coefficients for Ag, grass, forest, Alfalfa and urban
		float Transmission = 0;
		
		// full raster save process...
Logger.info("  > Allocated memory for Water_Quality");

		// Water quality model
		// 1st step. Calculate phosphorus for each cell in the landscape
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				if (rotationData[y][x] > 0)
				{
					// Grass
					if ((rotationData[y][x] & Grass_Mask) > 0) 
					{
						//P_flux = 1.0f;
						P_flux = 0.75f;
						//Transmission = 0.6f;
						Transmission = 0.2f;
					} 
					// Alfalfa
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) 
					{
						P_flux = 1.0f;
						Transmission = 0.3f;
					} 
					// Forest
					else if ((rotationData[y][x] & Forest_Mask) > 0) 
					{
						P_flux = 0.1f;
						//Transmission = 0.25f;
						Transmission = 0.1f;
					} 
					// Agriculture
					else if ((rotationData[y][x] & Ag_Mask) > 0) 
					{
						//P_flux = 3f;
						P_flux = 2.0f;
						//Transmission = 0.75f;
						Transmission = 0.93f;
					}
					// Urban
					else if ((rotationData[y][x] & Urban_Mask) > 0) 
					{
						P_flux = 1.5f;
						//Transmission = 0.95f;
						Transmission = 1.00f;
					}
					// Other land use classes
					else
					{
						P_flux = 0;
						Transmission = 0;
					}
					
					// Calculate phosphorus for each cell in the landscape (Kg per year)
					Dist = (int)(Rivers[y][x] / 30) + 1;
					PhosphorusData[y][x] = P_flux * 900 * 0.0001f * (float)(Math.pow(Transmission, Dist));
					
					// 2st step. Add the calculated cells within a watershed
					watershedIdx = watersheds[y][x];
					
					if (watershedIdx >= 0) 
					{
						CountCellsInWatershed[watershedIdx]++;
						Phosphorus[watershedIdx] = Phosphorus[watershedIdx] + PhosphorusData[y][x];
					}
				}
				else 
				{
					PhosphorusData[y][x] = -9999.0f;
					//phosphorusData[y][x] = 0;
				}
			}
		}
		
		// 3rd step...fill in a full-sized raster with those values so they can be used
		//	to compute heatmaps or be analyzed with the standard code-paths...
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				watershedIdx = watersheds[y][x];
				
				if (watershedIdx >= 0) 
				{
					PhosphorusData[y][x] = Phosphorus[watershedIdx];
				}
				else 
				{
					PhosphorusData[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		//results.add(new ModelResult("nitrogen", scenario.mOutputDir, nitrogenData, width, height));
		results.add(new ModelResult("water_quality", scenario.mOutputDir, PhosphorusData, width, height));
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model_Water_Quality is finished - timing: " + Float.toString(timeSec));

		return results;
	}
}
