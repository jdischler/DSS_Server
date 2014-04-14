package util;

import play.*;
import java.util.*;
import java.io.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses slope, soil depth, silt and CEC to calculate corn, soy, grass and alfalfa yield (Tonnes per hec) 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are slope, soil depth, silt and CEC layers, selected cells in the raster map and crop rotation layer 
// Outputs are ASCII map of corn, soy, grass and alfalfa yield
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_CropYield
{
	//--------------------------------------------------------------------------
	public float[][] run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(" >> Computing Yield");

		// Defining variables based on the selected layer
		//int Grass_Mask = 256; // 9
		//int Corn_Mask = 1; // 1	
		//int Soy_Mask = 2; // 2	
		//int Corn_Soy_Mask = 4; // 3
		//int Alfalfa_Mask = 128; // 8	
		//int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask | Corn_Soy_Mask;
		
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int Alfalfa_Mask = cdl.convertStringsToMask("Alfalfa");
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		
		// Corn and Grass Yield
		float Corn_Y = 0;
		float Grass_Y = 0;
		float Soy_Y = 0;
		float Alfalfa_Y = 0;
		
		float Yield = 0;

		// Yield Modification/Scalar - default to multiply by 1.0, which is NO change
		float cornYieldModifier = 1.0f; //
		float soyYieldModifier = 1.0f; //
		float alfalfaYieldModifier = 1.0f; //
		float grassYieldModifier = 1.0f; //
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {	
			// Value comes in as a percent, e.g. -5%...convert to a multipler
			cornYieldModifier = scenario.mAssumptions.getAssumptionFloat("ym_corn") / 100.0f + 1.0f;
			soyYieldModifier = scenario.mAssumptions.getAssumptionFloat("ym_soy") / 100.0f + 1.0f;
			alfalfaYieldModifier = scenario.mAssumptions.getAssumptionFloat("ym_alfalfa") / 100.0f + 1.0f;
			grassYieldModifier = scenario.mAssumptions.getAssumptionFloat("ym_grass") / 100.0f + 1.0f;
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info(" Corn yield from client = " + Float.toString(cornYieldModifier) );
		Logger.info(" Soy yield from client = " + Float.toString(soyYieldModifier) );
		Logger.info(" Alfalfa yield from client = " + Float.toString(alfalfaYieldModifier) );
		Logger.info(" Grass yield from client = " + Float.toString(grassYieldModifier) );
		//----------------------------------------------------------------------		
		
		// Define separate arrays to keep corn and grass production
		// Crop Yield
		float[][] yield = new float[height][width];
Logger.info("  > Allocated memory for Yield");
		
		float slope[][] = Layer_Base.getLayer("Slope").getFloatData();
		float silt[][] = Layer_Base.getLayer("Silt").getFloatData();
		float depth[][] = Layer_Base.getLayer("Depth").getFloatData();
		float cec[][] = Layer_Base.getLayer("CEC").getFloatData();
		
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				
				if ((rotationData[y][x] & TotalMask) > 0) 
				{
					if (slope[y][x] < 0 || depth[y][x] < 0 || silt[y][x] < 0 || cec[y][x] < 0)
					{
						yield[y][x] = -9999.0f;
					}
					else if ((rotationData[y][x] & Corn_Mask) > 0) 
					{
						// Bushels per Ac
						Corn_Y = 22.000f - 1.05f * slope[y][x] + 0.190f * depth[y][x] + 0.817f * silt[y][x] + 1.32f * cec[y][x];
						// Correct for techno advances
						Corn_Y = Corn_Y * 1.30f;
						// add stover
						Corn_Y = Corn_Y + Corn_Y;
						// Mg per Ha
						Yield = Corn_Y * 0.053f;
						// Factor in yield modifcation from client, which defaults to 0% change, ie * 1.0f
						Yield *= cornYieldModifier;
					}
					else if ((rotationData[y][x] & Grass_Mask) > 0) 
					{
						// short tons per Ac
						Grass_Y = 0.77f - 0.031f * slope[y][x] + 0.008f * depth[y][x] + 0.029f * silt[y][x] + 0.038f * cec[y][x];
						// Correct for techno advances
						Grass_Y = Grass_Y * 1.05f;
						// Mg per Ha
						Yield = Grass_Y * 1.91f;
						// Factor in yield modifcation from client, which defaults to 0% change, ie * 1.0f
						Yield *= grassYieldModifier;
					}
					else if ((rotationData[y][x] & Soy_Mask) > 0) 
					{
						// Bushels per Ac
						Soy_Y = 6.37f - 0.34f * slope[y][x] + 0.065f * depth[y][x] + 0.278f * silt[y][x] + 0.437f * cec[y][x];
						// Correct for techno advances
						Soy_Y = Soy_Y * 1.2f;
						// Mg per Ha
						Soy_Y = Soy_Y * 0.0585f;
						// add residue
						Yield = Soy_Y + Soy_Y * 1.5f;
						// Factor in yield modifcation from client, which defaults to 0% change, ie * 1.0f
						Yield *= soyYieldModifier;
					}
					/*else if ((rotationData[y][x] & Corn_Soy_Mask) > 0) {
						// Bushels per Ac
						Corn_Y = 22.000f - 1.05f * slope[y][x] + 0.190f * depth[y][x] + 0.817f * silt[y][x] + 1.32f * cec[y][x];
						// Correct for techno advances
						Corn_Y = Corn_Y * 1.30f;
						// add stover
						Corn_Y = Corn_Y + Corn_Y;
						// Bushels per Ac
						Soy_Y = 6.37f - 0.34f * slope[y][x] + 0.065f * depth[y][x] + 0.278f * silt[y][x] + 0.437f * cec[y][x];
						// Correct for techno advances
						Soy_Y = Soy_Y * 1.2f;
						// Tonnes per Hec
						Soy_Y = Soy_Y * 0.0585f;
						// add residue
						Yield = ((Corn_Y * 0.053f) + (Soy_Y + Soy_Y * 1.5f)) / 2.0f;
					}*/
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) 
					{
						// Short tons per Acre
						Alfalfa_Y = 1.26f - 0.045f * slope[y][x] + 0.007f * depth[y][x] + 0.027f * silt[y][x] + 0.041f * cec[y][x];
						// Yield Correction Factor for modern yield
						Alfalfa_Y = Alfalfa_Y * 1.05f;
						// Mg per Ha
						Yield = Alfalfa_Y * 1.905f;
						// Factor in yield modifcation from client, which defaults to 0% change, ie * 1.0f
						Yield *= alfalfaYieldModifier;
					}
					
					// Set Min and Max
					if(Yield < 0)
					{
						yield[y][x] = 0.0f;
					}
					else if (Yield > 25) 
					{
						yield[y][x] = 25.0f;
					}
					else 
					{
						yield[y][x] = Yield;
					}
				}
				else 
				{
					yield[y][x] = -9999.0f;
				}
				
			}
		}
		
		return yield;
	}
}

