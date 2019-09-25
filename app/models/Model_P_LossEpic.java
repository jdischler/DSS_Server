package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program calculates phosphorus for each pixel at watershed scale using the results of EPIC models and then sum them up at watershed level (Kg per year)
// Version 01/20/2014
//
//------------------------------------------------------------------------------
public class Model_P_LossEpic extends Model_Base
{
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}

	private static final String mPLossModelFile = "p_loss_epic";
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) 
	{
Logger.info(">>> Computing P_Loss_EPIC Model");

		//----------------------------------------------------------------------
		// Get Client Multipliers
		//----------------------------------------------------------------------
		float MM = 1.0f; // manure multiplier
		float CCM = 1.0f;// cover crop multiplier
		float TM = 1.0f;// till multiplier
		
		// Manure values from client
		float manureAnnualModifier = 1.0f; 
		float fallManureAnnualModifier = 1.0f; 
		float covercropAnnualModifier = 1.0f;
		float tillAnnualModifier = 1.0f;

		float manurePerennialModifier = 1.0f;
		float fallManurePerennialModifier = 1.0f;		
	
		// Get user changeable yield scaling values from the client...
		//----------------------------------------------------------------------
		try {	
			// values come in as straight multiplier
			manureAnnualModifier = scenario.mAssumptions.getAssumptionFloat("p_m_annuals");
			fallManureAnnualModifier = scenario.mAssumptions.getAssumptionFloat("p_fm_annuals");
			covercropAnnualModifier = scenario.mAssumptions.getAssumptionFloat("p_cc_annuals");
			tillAnnualModifier = scenario.mAssumptions.getAssumptionFloat("p_t_annuals");

			// values come in as straight multiplier
			manurePerennialModifier = scenario.mAssumptions.getAssumptionFloat("p_m_perennials");
			fallManurePerennialModifier = scenario.mAssumptions.getAssumptionFloat("p_fm_perennials");
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		detailedLog(" PLoss - annuals manure from client = " + Float.toString(manureAnnualModifier) );
		detailedLog(" PLoss - annuals fall manure from client = " + Float.toString(fallManureAnnualModifier) );
		detailedLog(" PLoss - annuals cover crop from client = " + Float.toString(covercropAnnualModifier) );
		detailedLog(" PLoss - annuals till from client = " + Float.toString(tillAnnualModifier) );

		detailedLog(" PLoss - perennial manure from client = " + Float.toString(manurePerennialModifier) );
		detailedLog(" PLoss - perennial fall manure from client = " + Float.toString(fallManurePerennialModifier) );

		// Spatial Layers
		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012");
		float[][] Alfa_p = Layer_Base.getLayer("alfa_p").getFloatData();
		float[][] Corn_p = Layer_Base.getLayer("corn_p").getFloatData();
		float[][] Soy_p = Layer_Base.getLayer("soy_p").getFloatData();
		float[][] Grass_p = Layer_Base.getLayer("grass_p").getFloatData();
		// Distance to river
		float[][] Rivers = Layer_Base.getLayer("rivers").getFloatData();
		
		// Mask
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Alfalfa_Mask = cdl.convertStringsToMask("alfalfa");
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		
		float[][] PhosphorusData = new float[height][width];
Logger.info("  > Allocated memory for P_Loss_EPIC");

		// Distance of cells from river in terms of area for Ag, grass, forest, Alfalfa and urban
		int Dist = 0;
		// Transmission coefficients for Ag, grass, forest, Alfalfa and urban
		float Transmission = 0;
		
		// Water quality model
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				PhosphorusData[y][x] = -9999.0f;

				int landCover = rotationData[y][x];
				MM = 1.0f; // set default multiplier
				CCM = 1.0f;
				TM = 1.0f;
				
				if ((landCover & TotalMask) > 0) {
					Transmission = -1.0f;
					// Grass
					if ((landCover & Grass_Mask) > 0 && Grass_p[y][x] > -9999) {
						
						Transmission = 0.20f;
						PhosphorusData[y][x] = Grass_p[y][x];
						MM = ManagementOptions.getFertilizerMultiplier(landCover, 
								1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
								fallManurePerennialModifier, manurePerennialModifier);
					}
					// Corn
					else if ((landCover & Corn_Mask) > 0 && Corn_p[y][x] > -9999) {
						
						Transmission = 0.80f;
						PhosphorusData[y][x] = Corn_p[y][x];
						MM = ManagementOptions.getFertilizerMultiplier(landCover, 
								1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
								fallManureAnnualModifier, manureAnnualModifier);
						TM = ManagementOptions.E_Till.getIfActiveOn(landCover, tillAnnualModifier, 1.0f);
						CCM = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, covercropAnnualModifier, 1.0f);
					}
					// Soy
					else if ((landCover & Soy_Mask) > 0 && Soy_p[y][x] > -9999) {
						
						Transmission = 0.80f;
						PhosphorusData[y][x] = Soy_p[y][x];
						MM = ManagementOptions.getFertilizerMultiplier(landCover, 
								1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
								fallManureAnnualModifier, manureAnnualModifier);
						TM = ManagementOptions.E_Till.getIfActiveOn(landCover, tillAnnualModifier, 1.0f);
						CCM = ManagementOptions.E_CoverCrop.getIfActiveOn(landCover, covercropAnnualModifier, 1.0f);
					} 
					// Alfalfa
					else if ((landCover & Alfalfa_Mask) > 0 && Alfa_p[y][x] > -9999) {
						
						Transmission = 0.30f;
						PhosphorusData[y][x] = Alfa_p[y][x];
						MM = ManagementOptions.getFertilizerMultiplier(landCover, 
								1.0f, 1.0f, // these values correspond to NO Fert multiplier and synthetic multiplier
								fallManurePerennialModifier, manurePerennialModifier);
					}
					
					if (Transmission >= 0.0f) {
						// Correct phosphorus Calculation for each cell in the landscape based on the distance
						// Convert Kg per Ha to Mg per cell
						Dist = (int)(Rivers[y][x] / 30.0f) + 1;
						PhosphorusData[y][x] = (PhosphorusData[y][x] *
								MM * TM * CCM * // apply multipliers for management options that came from client 
								900.0f * 0.0001f * (float)(Math.pow(Transmission, Dist))) / 1000.0f;
					}
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult("p_loss_epic", scenario.mOutputDir, PhosphorusData, width, height));
		
		return results;
	}
}

