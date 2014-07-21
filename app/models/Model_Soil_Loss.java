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
// Version 02/15/2014
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
		// Both the growing crops and the residue from the crops affect erosion control
		// C factor compares erosion from sites with different crops and residue covers to that of bare, un-cropped site
		// Thus, managemnet decisions about crop rotations and tillage system determine the value of C
		// Undisturbed grassland and forests have very little erosion and the lowest C value, typically below 0.2
		// Soil with no plant or residue cover are most susceptible to erosion and have the highest possible C factor
		float C = 0.0f;
		// Support practice factor (Dimensionless): 1) Contouring, 2) Strip Cropping, 3) Terrace, 4) grassed waterway and 5) No Practice
		// Contouring is tilling and planting across slope rather than with it
		// Strip cropping uses alternate lands of cover and row crops across the slope
		// Terrace are soil embankment that reshape slopes into a series of short slopes that slow water flow and reduce its erosivity
		// Terrace that open to grasses waterways further limit erosion
		// P is 1 for No Practice
		float P = 0.0f;
		// Till Multiplier
		//float T_M = 1.0f;
		// Cover Crop Multiplier
		float CC_M = 1.0f;
		
		// Multipliers from client variables
		//float annualTillage_C1 = 1.0f; //
		float annualCoverCropModifier = 1.0f;		
		float Management_P1 = 1.0f; //
		float Management_P2 = 1.0f;	
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {
			// values come in as straight multiplier
			//annualTillage_C1 = scenario.mAssumptions.getAssumptionFloat("sl_t_annuals_C1");
			annualCoverCropModifier = scenario.mAssumptions.getAssumptionFloat("sl_cc_annuals");
			Management_P1 = scenario.mAssumptions.getAssumptionFloat("sl_Contouring_P1");
			Management_P2 = scenario.mAssumptions.getAssumptionFloat("sl_Terrace_P2");
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		//Logger.info(" Agricultural no till from client = " + Float.toString(annualTillage_C1) );
		Logger.info(" Agricultural cover crop from client = " + Float.toString(annualCoverCropModifier) );
		Logger.info(" Agricultural Contouring from client = " + Float.toString(Management_P1) );
		Logger.info(" Agricultural Terrace from client = " + Float.toString(Management_P2) );
		
		// full raster save process...
Logger.info("  > Allocated memory for Soil_Loss");

		// Soil Loss Model
		// 1st step. Calculate phosphorus for each cell in the landscape
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				int landCover = rotationData[y][x];
				//T_M = 1.0f;
				CC_M = 1.0f;
				if ((landCover & TotalMask) > 0)
				{
					// Update C and P factors for different LUCC type
					// C and P are coming from biophysical table (Invest)
					// Grass
					if ((landCover & Grass_Mask) > 0) 
					{
						C = 0.02f;
						//P = 0.25f;
						//P = 1.0f;
						P = ManagementOptions.E_Contour.getIfActiveOn(landCover, Management_P1, 1.0f) *
						ManagementOptions.E_Terrace.getIfActiveOn(landCover, Management_P2, 1.0f);
					} 
					// Alfalfa
					else if ((landCover & Alfalfa_Mask) > 0) 
					{
						C = 0.02f;
						//P = 0.25f;
						//P = 1.0f;
						P = ManagementOptions.E_Contour.getIfActiveOn(landCover, Management_P1, 1.0f) *
						ManagementOptions.E_Terrace.getIfActiveOn(landCover, Management_P2, 1.0f);
					} 
					// Forest
					else if ((landCover & Forest_Mask) > 0) 
					{
						C = 0.003f;
						//P = 0.2f;
						//P = 1.0f;
						P = ManagementOptions.E_Contour.getIfActiveOn(landCover, Management_P1, 1.0f) *
						ManagementOptions.E_Terrace.getIfActiveOn(landCover, Management_P2, 1.0f);
					} 
					// Agriculture
					else if ((landCover & Ag_Mask) > 0) 
					{
						C = 0.3f;
						//C = C * ManagementOptions.E_Till.getIfActiveOn(landCover, annualTillage_C1, 1.0f);
						//P = 0.4f;
						//P = 1.0f;
						// Return tillage modififier if cell is Tilled
						//NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover, 1.0f, annualNoTillageModifier);
						CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
						P = ManagementOptions.E_Contour.getIfActiveOn(landCover, Management_P1, 1.0f) *
						ManagementOptions.E_Terrace.getIfActiveOn(landCover, Management_P2, 1.0f);
					}
					// Other land use classes
					//else
					//{
					//	C = -9999;
					//	P = -9999;
					//}

					if (Rainfall_Erosivity[y][x] > -9999.0f && Soil_Erodibility[y][x] > -9999.0f && LS[y][x] > -9999.0f)
					{
						// Convert Mg per Ha to Mg per cell
						// Calculate Soil Loss for each cell in the landscape (Mg per cell per year)
						Soil_Loss_Data[y][x] = Rainfall_Erosivity[y][x] * Soil_Erodibility[y][x] * LS[y][x] * 
								C * P * CC_M * // apply multipliers based on management options
								900.0f / 10000.0f;
						// Calculate Soil Loss in the landscape (Mg per Ha per year)
						//Soil_Loss_Data[y][x] = Rainfall_Erosivity[y][x] * Soil_Erodibility[y][x] * LS[y][x] * C * P;
						// Calculate Soil Loss (Mg per Ha per year)
						//Soil_Loss_Data[y][x] = Rainfall_Erosivity[y][x] * Soil_Erodibility[y][x] * LS[y][x] * C * P;
					}
					else
					{
						Soil_Loss_Data[y][x] = -9999.0f;
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

