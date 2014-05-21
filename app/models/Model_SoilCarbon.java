package util;

import play.*;
import java.util.*;
import java.io.*;

import java.lang.reflect.Array;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses corn, soy, grass and alfalfa production to calculate Soil Carbon 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Outputs are ASCII map of Net Energy
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_SoilCarbon
{
	// Raw Soil Carbon Change Factor (RSCCF) 
	static final float RSCCF_Corn_Grass = 0.63f; // Continuous Corn to Grass
	static final float RSCCF_Corn_Alfalfa = 0.37f; // Continuous Corn to Alfalfa
	static final float RSCCF_Soy_Grass = 0.63f; // Continuous Soy to Grass
	static final float RSCCF_Soy_Alfalfa = 0.37f; // Continuous Soy to Alfalfa
	static final float RSCCF_Alfalfa_Grass = 0.59f; // Continuous Grass to Alfalfa
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationT_Data = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(">>> Computing Soil Carbon Index");
long timeStart = System.currentTimeMillis();
		
		float [][] soilCarbonData = new float[height][width];
Logger.info("  > Allocated memory for SOC");

		// Defining variables based on the selected layer
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int Alfalfa_Mask = cdl.convertStringsToMask("Alfalfa");
		
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		float factor = -1.0f;
		float adjFactor = -1.0f;
		
		// Multipliers from client variables
		float annualNoTillageModifier = 1.0f;
		float annualCoverCropModifier = 1.0f;		
		float annualFertilizerModifier = 1.0f;
		float perennialFertilizerModifier = 1.0f;
		float annualFallFertilizerModifier = 1.0f;
		float perennialFallFertilizerModifier = 1.0f;
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try 
		{	
			// values come in as straight multiplier
			annualNoTillageModifier = scenario.mAssumptions.getAssumptionFloat("soc_nt_annuals");
			annualCoverCropModifier = scenario.mAssumptions.getAssumptionFloat("soc_cc_annuals");		
			annualFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("soc_m_annuals");
			perennialFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("soc_m_perennials");	
			annualFallFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("soc_fm_annuals");
			perennialFallFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("soc_fm_perennials");	
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info(" Agricultural no till from client = " + Float.toString(annualNoTillageModifier) );
		Logger.info(" Agricultural cover crop from client = " + Float.toString(annualCoverCropModifier) );
		Logger.info(" Agricultural Fertilizer from client = " + Float.toString(annualFertilizerModifier) );
		Logger.info(" Perennial Fertilizer from client = " + Float.toString(perennialFertilizerModifier) );
		Logger.info(" Annual Fall Fertilizer from client = " + Float.toString(annualFallFertilizerModifier) );
		Logger.info(" Perennial Fall Fertilizer from client = " + Float.toString(perennialFallFertilizerModifier) );
		
		// No Till Multiplier
		float NT_M = 1.0f;
		// Cover Crop Multiplier
		float CC_M = 1.0f;
		// Fertiliezer Multiplier
		float F_M = 1.0f;
		
		int [][] rotationD_Data = Layer_Base.getLayer("cdl_2012").getIntData();
		// Mg per Ha
		float[][] SOC = Layer_Base.getLayer("SOC").getFloatData();
		
		// Soil_Carbon
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if ((rotationD_Data[y][x] & TotalMask) > 0 /*&& scenario.mSelection.mRasterData[y][x] >= 1*/)
				{
					int landCover = rotationD_Data[y][x];
					if (rotationD_Data[y][x] == 0 || rotationT_Data[y][x] == 0 || SOC[y][x] <= 0.0f) {
						soilCarbonData[y][x] = -9999.0f;
					}
					else {
						//factor = 0.0f;
						
						// ----- CORN to...
						if ((rotationD_Data[y][x] & Corn_Mask) > 0) {
							if ((rotationT_Data[y][x] & Grass_Mask) > 0) {
								factor = RSCCF_Corn_Grass; // ...GRASS
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
							else if ((rotationT_Data[y][x] & Alfalfa_Mask) > 0) {
								factor = RSCCF_Corn_Alfalfa; // ...ALFALFA
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
						} // ---- SOY to...
						else if ((rotationD_Data[y][x] & Soy_Mask) > 0) {
							if ((rotationT_Data[y][x] & Grass_Mask) > 0) {
								factor = RSCCF_Soy_Grass; // ...GRASS
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
							else if ((rotationT_Data[y][x] & Alfalfa_Mask) > 0) {
								factor = RSCCF_Soy_Alfalfa; // ...ALFALFA
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
						} // ---- ALFALFA to...
						else if ((rotationD_Data[y][x] & Alfalfa_Mask) > 0) {
							if ((rotationT_Data[y][x] & Grass_Mask) > 0) {
								factor = RSCCF_Alfalfa_Grass; // ...GRASS
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							} 
							else if ((rotationT_Data[y][x] & Corn_Mask) > 0) {
								factor = -RSCCF_Corn_Alfalfa; // ...CORN
								// Return tillage modififier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							else if ((rotationT_Data[y][x] & Soy_Mask) > 0) {
								factor = -RSCCF_Soy_Alfalfa; // ...SOY
								// Return tillage modififier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
						} // ---- GRASS to...
						else if ((rotationD_Data[y][x] & Grass_Mask) > 0) {
							if ((rotationT_Data[y][x] & Corn_Mask) > 0) {
								factor = -RSCCF_Corn_Grass; // ...CORN
								// Return tillage modififier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							else if ((rotationT_Data[y][x] & Soy_Mask) > 0) {
								factor = -RSCCF_Soy_Grass; // ...SOY
								// Return tillage modififier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							else if ((rotationT_Data[y][x] & Alfalfa_Mask) > 0) {
								factor = -RSCCF_Alfalfa_Grass; // ...ALFALFA
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
						}
						
						// Calculate equation based on calculated factor and SOC layer
						adjFactor = -0.5938f * (float)(Math.log(SOC[y][x] * 0.1f)) + 1.6524f;
						
						if (adjFactor <= 0.2f) {
							adjFactor = 0.2f;
						}
						else if (adjFactor >= 1.2f) {
							adjFactor = 1.2f;
						}
						
						if(factor > -1.0f && adjFactor > -1.0f)
						{
							// Convert the change from 20 years to 1 year
							//soilCarbonData[y][x] = SOC[y][x] + (SOC[y][x] * factor * adjFactor) / 20.0f;
							//soilCarbonData[y][x] = SOC[y][x] * adjFactor + SOC[y][x] * factor * adjFactor;
							
							// Mg per Ha and convert to Mg per cell
							soilCarbonData[y][x] = (SOC[y][x] * (1 + (factor * adjFactor) / 20.0f)) * 900.0f / 10000.0f;
							// Convert from Mg to short tons
							//soilCarbonData[y][x] = soilCarbonData[y][x] * 1.102f;
							
							// Change value using multiplier
							soilCarbonData[y][x] *=  NT_M * CC_M * F_M;
						}
						else
						{
							// Corn
							if ((rotationD_Data[y][x] & Corn_Mask) > 0) {
								// Return tillage modififier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							// Soy
							else if ((rotationT_Data[y][x] & Soy_Mask) > 0) {
								// Return tillage modififier if cell is Tilled
								NT_M = ManagementOptions.E_Till.getIfActiveOn(landCover, 1.0f, annualNoTillageModifier);
								CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											annualFallFertilizerModifier, annualFertilizerModifier);
							}
							else if ((rotationT_Data[y][x] & Grass_Mask) > 0) {
								factor = RSCCF_Corn_Grass; // ...GRASS
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
							else if ((rotationT_Data[y][x] & Alfalfa_Mask) > 0) {
								factor = RSCCF_Corn_Alfalfa; // ...ALFALFA
								// Return tillage modififier if cell is Tilled
								F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
											1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
											perennialFallFertilizerModifier, perennialFertilizerModifier);
							}
						}
							
							// Mg per Ha and convert to Mg per cell
							soilCarbonData[y][x] = SOC[y][x] * 900.0f / 10000.0f;
							
							// Change value using multiplier
							soilCarbonData[y][x] *=  NT_M * CC_M * F_M;
							
						}
						//else
						//{
						//	soilCarbonData[y][x] = -9999.0f;
						//}
						//if (adjFactor < 0 || factor < 0 || soilCarbonData[y][x] < SOC[y][x])
						//{
							//Logger.info(Float.toString(adjFactor));
							//Logger.info(Float.toString(factor));
							//Logger.info(Float.toString(soilCarbonData[y][x]) + " " + Float.toString(SOC[y][x]));
						//}
					}
				//}
				else 
				{
					soilCarbonData[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult("soc", scenario.mOutputDir, soilCarbonData, width, height));

long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model Soil Carbon is finished - timing: " + Float.toString(timeSec));

		return results;
	}

}
