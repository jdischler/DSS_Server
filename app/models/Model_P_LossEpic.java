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
// This program calculates phosphorus for each pixel at watershed scale using the results of EPIC models and then sum them up at watershed level (Kg per year)
// Version 01/20/2014
//
//------------------------------------------------------------------------------
public class Model_P_LossEpic extends Model_Base
{

	private static final String mPLossModelFile = "p_loss_epic";
	// Number of watersheds in study area
	//private static final int mNumWatersheds = 140;
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) 
	{

//Logger.info(">>> Computing Model Phosphorus Loss");
Logger.info(">>> Computing P_Loss_EPIC Model");
long timeStart = System.currentTimeMillis();
		
		// Spatial Layers
		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012");
		// Alfa
		float[][] Alfa_p = Layer_Base.getLayer("alfa_p").getFloatData();
		// Corn
		float[][] Corn_p = Layer_Base.getLayer("corn_p").getFloatData();
		// Soy
		float[][] Soy_p = Layer_Base.getLayer("soy_p").getFloatData();
		// Grass
		float[][] Grass_p = Layer_Base.getLayer("grass_p").getFloatData();
		// Watershed layer
		//int[][] watersheds = Layer_Base.getLayer("watersheds").getIntData();
		// Distance to river
		float[][] Rivers = Layer_Base.getLayer("rivers").getFloatData();
		// Id for tracking watershed
		//int watershedIdx = 0;
		
		// Mask
		// Grass
		int Grass_Mask = cdl.convertStringsToMask("grass");
		// Alfalfa
		int Alfalfa_Mask = cdl.convertStringsToMask("alfalfa");
		// Corn
		int Corn_Mask = cdl.convertStringsToMask("corn");
		// Soy
		int Soy_Mask = cdl.convertStringsToMask("soy");
		
		// Arrays to sum phosphorus within each watershed
		//int[] CountCellsInWatershed = new int[mNumWatersheds];
		// Arrays to save phosphorus at watershed scale
		//float[] Phosphorus = new float[mNumWatersheds];
		// Arrays to save phosphorus at cell base
		float[][] PhosphorusData = new float[height][width];
		
		// Distance of cells from river in terms of area for Ag, grass, forest, Alfalfa and urban
		int Dist = 0;
		// Transmission coefficients for Ag, grass, forest, Alfalfa and urban
		float Transmission = 0;
		
		// full raster save process...
Logger.info("  > Allocated memory for P_Loss_EPIC");

		// Water quality model
		// 1st step. Calculate phosphorus for each cell in the landscape
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				if (rotationData[y][x] > 0)
				{
					// Kg per Ha
					// Grass
					if ((rotationData[y][x] & Grass_Mask) > 0) 
					{
						if (Grass_p[y][x] > -9999)
						{
							Transmission = 0.20f;
							PhosphorusData[y][x] = Grass_p[y][x];
						}
						else
						{
							Transmission = 0;
						}
					}
					// Corn
					else if ((rotationData[y][x] & Corn_Mask) > 0) 
					{
						if(Corn_p[y][x] > -9999)
						{
							Transmission = 0.80f;
							PhosphorusData[y][x] = Corn_p[y][x];
						}
						else
						{
							Transmission = 0;
						}
					}
					// Soy
					else if ((rotationData[y][x] & Soy_Mask) > 0) 
					{
						if(Soy_p[y][x] > -9999)
						{
							Transmission = 0.80f;
							PhosphorusData[y][x] = Soy_p[y][x];
						}
						else
						{
							Transmission = 0;
						}
					} 
					// Alfalfa
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) 
					{
						if (Alfa_p[y][x] > -9999)
						{
							Transmission = 0.30f;
							PhosphorusData[y][x] = Alfa_p[y][x];
						}
						else
						{
							Transmission = 0;
						}
					} 
					else
					{
						Transmission = 0;
					}
					// Correct phosphorus Calculation for each cell in the landscape based on the distance
					// Convert Kg per Ha to Mg per cell
					Dist = (int)(Rivers[y][x] / 30) + 1;
					PhosphorusData[y][x] = (PhosphorusData[y][x] * 900.0f * 0.0001f * (float)(Math.pow(Transmission, Dist))) / 1000.0f;
					
					// 2st step. Add the calculated cells within a watershed
					//watershedIdx = watersheds[y][x];
					
					//if (watershedIdx >= 0) 
					//{
						// watershed index zero is reserved for no-data
						//CountCellsInWatershed[watershedIdx]++;
						//Phosphorus[watershedIdx] = Phosphorus[watershedIdx] + PhosphorusData[y][x];
					//}
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
		//for (int y = 0; y < height; y++) 
		//{
			//for (int x = 0; x < width; x++) 
			//{
				//watershedIdx = watersheds[y][x];
				
				//if (watershedIdx >= 0) 
				//{
				//	PhosphorusData[y][x] = Phosphorus[watershedIdx];
				//}
				//else 
				//{
				//	PhosphorusData[y][x] = -9999.0f;
				//}
			//}
		//}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		//results.add(new ModelResult("nitrogen", scenario.mOutputDir, nitrogenData, width, height));
		results.add(new ModelResult("p_loss_epic", scenario.mOutputDir, PhosphorusData, width, height));
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> P_Loss_EPIC Model is finished - timing: " + Float.toString(timeSec));

		return results;
	}
}

