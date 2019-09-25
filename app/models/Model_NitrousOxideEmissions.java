package util;

import play.*;
import java.util.*;
import java.io.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses to calculate nitrous oxide emissions(Mg per Ha) 
// This model is from unpublished work by 
// Inputs are layers, selected cells in the raster map and crop rotation layer 
// Outputs are ASCII map of nitrous oxide emissions
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_NitrousOxideEmissions
{
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}

	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(">>> Computing Nitrous Oxide Index");
		
		float [][] nitrousOxideData = new float[height][width];
Logger.info("  > Allocated memory for N2O");
		
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int Alfalfa_Mask = cdl.convertStringsToMask("Alfalfa");
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		
		// Input layers
		float n2o_composite[][] = Layer_Base.getLayer("n2o_composite").getFloatData();
		
		// Multipliers from client variables
		float annualTillageModifier = 1.0f; //
		float annualCoverCropModifier = 1.0f;		
		float annualFertilizerModifier = 1.0f;
		float perennialFertilizerModifier = 1.0f;
		float annualFallFertilizerModifier = 1.0f;
		float perennialFallFertilizerModifier = 1.0f;
		
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {	
			// values come in as straight multiplier
			annualTillageModifier = scenario.mAssumptions.getAssumptionFloat("n_t_annuals");
			annualCoverCropModifier = scenario.mAssumptions.getAssumptionFloat("n_cc_annuals");		
			annualFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("n_m_annuals");
			perennialFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("n_m_perennials");	
			annualFallFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("n_fm_annuals");
			perennialFallFertilizerModifier = scenario.mAssumptions.getAssumptionFloat("n_fm_perennials");	
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		detailedLog(" Agricultural till from client = " + Float.toString(annualTillageModifier) );
		detailedLog(" Agricultural cover crop from client = " + Float.toString(annualCoverCropModifier) );
		detailedLog(" Agricultural Fertilizer from client = " + Float.toString(annualFertilizerModifier) );
		detailedLog(" Perennial Fertilizer from client = " + Float.toString(perennialFertilizerModifier) );
		detailedLog(" Annual Fall Fertilizer from client = " + Float.toString(annualFallFertilizerModifier) );
		detailedLog(" Perennial Fall Fertilizer from client = " + Float.toString(perennialFallFertilizerModifier) );
		
		// Till SoilLoss Multiplier
		float T_M = 1.0f;
		// Cover Crop Multiplier
		float CC_M = 1.0f;
		// Fertiliezer Multiplier
		float F_M = 1.0f;
		
		// Constant for input layers
		float cropRotation = 0;
		float fertRate = 0;
		
		// Calculate Nitrous Oxide Emissions
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				int landCover = rotationData[y][x];
				
				// Only process pixels for the landcover types we care about. Skip any cell that would
				//	have a no-data value as any part of the computation
				if ((landCover & TotalMask) > 0 &&
					n2o_composite[y][x] > -9999.0f)
				{
					fertRate = 0.0f;
					
					if ((landCover & Corn_Mask) > 0) { // CORN
						cropRotation = 0.0f;
						fertRate = 168.0f;
						
						// Return tillage modififier if cell is Tilled
						T_M = ManagementOptions.E_Till.getIfActiveOn(landCover, annualTillageModifier, 1.0f);
						CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									annualFallFertilizerModifier, annualFertilizerModifier);
					}
					else if ((landCover & Grass_Mask) > 0) { // GRASS
						cropRotation = -1.268f;
						// Is that right or this belongs to Soy?
						fertRate = 56.0f;
						
						// Return tillage modififier if cell is Tilled
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									perennialFallFertilizerModifier, perennialFertilizerModifier);
					}
					else if ((landCover & Soy_Mask) > 0) { // SOY
						cropRotation = -1.023f;
						
						// Return tillage modififier if cell is Tilled
						T_M = ManagementOptions.E_Till.getIfActiveOn(landCover, annualTillageModifier, 1.0f);
						CC_M = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, annualCoverCropModifier, 1.0f);
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									annualFallFertilizerModifier, annualFertilizerModifier);
					}
					else if ((landCover & Alfalfa_Mask) > 0) { // ALFALFA
						cropRotation = -1.023f;
						
						// Return tillage modififier if cell is Tilled
						F_M = ManagementOptions.getFertilizerMultiplier(landCover, 
									1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
									perennialFallFertilizerModifier, perennialFertilizerModifier);
					}
					
					// Calculate Nitrous Oxide Emissions in Kg per Ha and then convert to Mg per cell per year
					nitrousOxideData[y][x] = ((float)(Math.exp(fertRate * 0.005f + cropRotation +
						n2o_composite[y][x]))) * 900.0f * 0.0001f * 0.001f;
					
					// Change value using multiplier
					nitrousOxideData[y][x] *=  T_M * CC_M * F_M;
					
				}
				else { // else not a landcover type we care about or one of the 4 input cells had no-data
					nitrousOxideData[y][x] = -9999.0f;
				}
			}
		}
	
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult("nitrous_oxide", scenario.mOutputDir, nitrousOxideData, width, height));

		return results;
	}
}

