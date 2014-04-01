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
// This program calculates soil loss for each pixel
// The inputs are where R is the rainfall erosivity, K is the soil erodibility factor, LS is the slope length factor, C is the crop-management factor and P is the support practice factor
// A = R * K * LS * C * P
// Units are normmally ton per Ha year for A, MJ mm per ha hr yr for R, ton ha hr per ha MJ mm for K, 
// R was calculated using Eq from below page
// http://gisedu.colostate.edu/webcontent/nr505/ethiopia/group4/GIS%20Analyses.html#rainfall
// K layer was incorporated from SSURGO data base and we converted it to ton per ha from ton / acre
// Other units are LS, C and P are dimensionless
// Input is crop rotation layer 
// Version 02/10/2014
//
//------------------------------------------------------------------------------
public class Model_Soil_Loss extends Model_Base
{

	private static final String mSoilLossModelFile = "Soil_Loss";
	// Number of watersheds in study area
	//private static final int mNumWatersheds = 140;
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) 
	{

//Logger.info(">>> Computing Model Nitrogen / Phosphorus");
Logger.info(">>> Computing Soil_Loss Model");
long timeStart = System.currentTimeMillis();
		
		// Spatial Layers
		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012");
		//int[][] watersheds = Layer_Base.getLayer("watersheds").getIntData();
		
		// Calculate A using this formula A = R * K * LS * C * P
		// Unit (MJ mm/ha hr yr) or R
		float[][] Rainfall_Erosivity = Layer_Base.getLayer("Rainfall_Erosivity").getFloatData();
		// Unit (ton ha hr/ha MJ mm) or K
		float[][] Soil_Erodibility = Layer_Base.getLayer("Soil_Erodibility").getFloatData();
		//float[][] Slope = Layer_Base.getLayer("Slope").getIntData();
		// LS (Dimensionless)
		float[][] LS = Layer_Base.getLayer("LS").getFloatData();
		// Unit (Dimensionless)
		//float[][] Slope_Steepness = Layer_Base.getLayer("Slope_Steepness").getFloatData();
		// Unit (Dimensionless)
		//float[][] Slope_Length = Layer_Base.getLayer("Slope_Length").getFloatData();
		// Id for tracking watershed
		//int watershedIdx = 0;
		
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
		// Total mask
		int TotalMask = Grass_Mask | Alfalfa_Mask | Forest_Mask | Ag_Mask;
		// Urban
		//int Urban_Mask = cdl.convertStringsToMask("urban");
		//int SubUrban_Mask = cdl.convertStringsToMask("suburban");
		//Urban_Mask = Urban_Mask | SubUrban_Mask;
		// Total Mask
		//int Corn_Mask = cdl.convertStringsToMask("corn");
		//int Soy_Mask = cdl.convertStringsToMask("soy");
		//int Alfalfa_Mask = cdl.convertStringsToMask("Alfalfa");
		//int TotalMask = Grass_Mask | Alfalfa_Mask | Forest_Mask | Ag_Mask | Urban_Mask;
		//int TotalMask = mAgMask | mGrassMask;
		
		// Arrays to sum soil loss within each watershed
		//int[] CountCellsInWatershed = new int[mNumWatersheds];
		// Arrays to save soil loss at watershed scale
		//float[] Soil_Loss = new float[mNumWatersheds];
		// Arrays to save soil loss at cell base (Mg/ha)
		float[][] Soil_Loss_Data = new float[height][width];
			
		// Rainfall erosivity index for Dane County
		//float Rainfall_Erosivity = 150.0f;
		// Cover management factor (Dimensionless)
		float C = 0.0f;
		// Support practice factor (Dimensionless)
		float P = 0.0f;
		
		// full raster save process...
Logger.info("  > Allocated memory for Soil_Loss");

		// Soil Loss Model
		// 1st step. Calculate phosphorus for each cell in the landscape
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				if ((rotationData[y][x] & TotalMask) > 0)
				{
					// Update C and P factors for different LUCC type
					// Grass
					if ((rotationData[y][x] & Grass_Mask) > 0) 
					{
						C = 0.01f;
						P = 1;
						//P = 0.20f;
					} 
					// Alfalfa
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) 
					{
						C = 0.02f;
						P = 1;
						//P = 0.25f;
					} 
					// Forest
					else if ((rotationData[y][x] & Forest_Mask) > 0) 
					{
						C = 0.003f;
						P = 1;
						//P = 0.2f;
					} 
					// Agriculture
					else if ((rotationData[y][x] & Ag_Mask) > 0) 
					{
						C = 0.5f;
						P = 1;
						//P = 0.4f;
					}
					// Other land use classes
					//else
					//{
					//	C = -9999;
					//	P = -9999;
					//}

					if (Rainfall_Erosivity[y][x] > -9999 && Soil_Erodibility[y][x] > -9999 && LS[y][x] > -9999)
					{
						// Convert Tonn per Ha to Tonns per cell
						// Calculate Soil Loss for each cell in the landscape (Tonns per cell per year)
						Soil_Loss_Data[y][x] = Rainfall_Erosivity[y][x] * Soil_Erodibility[y][x] * LS[y][x] * C * P * 900 * 0.0001f;
					}
					
					// 2st step. Add the calculated cells within a watershed
					//watershedIdx = watersheds[y][x];
					
					//if (watershedIdx >= 0) 
					//{
						// watershed index zero is reserved for no-data
						//watershedIdx--;
						//CountCellsInWatershed[watershedIdx]++;
						//Soil_Loss[watershedIdx] = Soil_Loss[watershedIdx] + Soil_Loss_Data[y][x];
					//}
				}
				else 
				{
					Soil_Loss_Data[y][x] = -9999.0f;
					//phosphorusData[y][x] = 0;
				}
			}
		}
		
		// 3rd step...fill in a full-sized raster with those values so they can be used
		//	to compute heatmaps or be analyzed with the standard code-paths...
		//for (int y = 0; y < height; y++) 
		//{
			//for (int x = 0; x < width; x++) 
			//{
			//	watershedIdx = watersheds[y][x];
				
			//	if (watershedIdx >= 0) 
			//	{
					// watershed index zero is reserved for no-data
					//watershedIdx--;
					//Soil_Loss_Data[y][x] = Soil_Loss[watershedIdx];
			//	}
			//	else 
			//	{
			//		Soil_Loss_Data[y][x] = -9999.0f;
			//	}
			//}
		//}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		//results.add(new ModelResult("nitrogen", scenario.mOutputDir, nitrogenData, width, height));
		results.add(new ModelResult("soil_loss", scenario.mOutputDir, Soil_Loss_Data, width, height));
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model_Soil_Loss is finished - timing: " + Float.toString(timeSec));

		return results;
	}
}
