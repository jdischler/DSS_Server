package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import java.lang.reflect.Array;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses corn, soy, grass and alfalfa production to calculate Net Energy 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Outputs are ASCII map of Ethanol, Net_Energy and Net_Income
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_EthanolNetEnergyIncome extends Model_Base
{
	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}
	
	private static String mEthanolModelFile = "ethanol";
	private static String mNetEnergyModelFile = "net_energy";
	private static String mNetIncomeModelFile = "net_income";
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(">>> Computing Model Ethanol / Net Energy / Net Income");
long timeStart = System.currentTimeMillis();
		// Precompute yield....
		Model_CropYield cropYield = new Model_CropYield();
		float[][] calculatedYield = cropYield.run(scenario);

		float [][] netEnergyData = new float[height][width];
		float [][] netIncomeData = new float[height][width];
		float [][] ethanolData = new float[height][width];
Logger.info("  > Allocated memory for NetEnergy, NetIncom, Fuel");
		
		float Net_Energy_C = 0;
		float Net_Energy_S = 0;
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int Alfalfa_Mask = cdl.convertStringsToMask("Alfalfa");
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		
		// Proportion of Stover 
		float Prop_Stover_Harvest = 0.38f;
		
		// Energy Input at Farm (MJ per Ha)
		float EI_CF = 18151f; // HILL
		float EI_CSF = 2121f; // HILL 1/4 fuel use for stover harvest
		float EI_SF = 6096f; // Hill
		float EI_AF = 9075f; // Corn Grain Hill * 1/2
		float EI_GF = 7411f; // EBAMM
		
		// Energy Input in Processing (MJ per L)
		float EI_CP = 13.99f; // HILL
		float EI_CSP = 1.71f; // EBAMM cellulosic
		float EI_GP = 1.71f; // EBAMM cellulosic
		float EI_SP = 10.39f; // Hill
		float EI_AP = 1.71f; // Corn Grain Hill * 1/2
		
		// Energy output (MJ per L)
		float EO_C = 21.26f + 4.31f; // HILL
		float EO_CS = 21.26f + 3.40f; // EBAMM cellulosic
		float EO_G = 21.26f + 3.40f; // EBAMM cellulosic
		float EO_S = 32.93f + 21.94f; // Hill
		float EO_A = 21.26f + 3.40f; // EBAMM cellulosic
		
		// Conversion Efficiency (L per Mg)
		float CEO_C = 400; // HILL
		float CEO_CS = 380; // EBAMM cellulosic
		float CEO_S = 200; // Hill
		float CEO_A = 380; // EBAMM cellulosic
		float CEO_G = 380; // EBAMM cellulosic
				
		float returnAmount;	// Gross return
		
		// Net Income 
		// Price for production
		float PC_Cost = 1135; // $ per ha cost for Corn
		float PCS_Cost = 412; // $ per ha cost for Corn Stover
		float PG_Cost = 412; // $ per ha cost for Grass
		float PS_Cost = 627; // $ per ha cost for Soy
		float PA_Cost = 620; // $ per ha cost for Alfalfa
		
		// Price per tonne for sell
		float P_Per_Corn = 274;
		float P_Per_Stover = 70;
		float P_Per_Grass = 107;
		float P_Per_Soy = 249;
		float P_Per_Alfalfa = 230;

		// Get user changeable values from the client...
		//----------------------------------------------------------------------
		// Net Income
		try {
			// Production
			PC_Cost = scenario.mAssumptions.getAssumptionFloat("p_corn_p");
			PCS_Cost = scenario.mAssumptions.getAssumptionFloat("p_stover_p");
			PG_Cost = scenario.mAssumptions.getAssumptionFloat("p_grass_p");
			PS_Cost = scenario.mAssumptions.getAssumptionFloat("p_soy_p");
			PA_Cost = scenario.mAssumptions.getAssumptionFloat("p_alfalfa_p");
			// Sell
			P_Per_Corn = scenario.mAssumptions.getAssumptionFloat("p_corn_s");
			P_Per_Stover = scenario.mAssumptions.getAssumptionFloat("p_stover_s");
			P_Per_Grass = scenario.mAssumptions.getAssumptionFloat("p_grass_s");
			P_Per_Soy = scenario.mAssumptions.getAssumptionFloat("p_soy_s");
			P_Per_Alfalfa = scenario.mAssumptions.getAssumptionFloat("p_alfalfa_s");
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		// Net Energy
		try {
			// Energy Input at Farm
			EI_CF = scenario.mAssumptions.getAssumptionFloat("e_corn");
			EI_CSF = scenario.mAssumptions.getAssumptionFloat("e_stover");
			EI_GF = scenario.mAssumptions.getAssumptionFloat("e_grass");
			EI_SF = scenario.mAssumptions.getAssumptionFloat("e_soy");
			EI_AF = scenario.mAssumptions.getAssumptionFloat("e_alfalfa");
			// Conversion Efficiency
			CEO_C = scenario.mAssumptions.getAssumptionFloat("e_corn_ce");
			CEO_CS = scenario.mAssumptions.getAssumptionFloat("e_stover_ce");
			CEO_G = scenario.mAssumptions.getAssumptionFloat("e_grass_ce");
			CEO_S = scenario.mAssumptions.getAssumptionFloat("e_soy_ce");
			CEO_A = scenario.mAssumptions.getAssumptionFloat("e_alfalfa_ce");
		}
		
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		// Net Income
		// Production
		detailedLog(" Corn production price from client = " + Float.toString(PC_Cost) );
		detailedLog(" Stover production price from client = " + Float.toString(PCS_Cost) );
		detailedLog(" Grass production price from client = " + Float.toString(PG_Cost) );
		detailedLog(" Soy production price from client = " + Float.toString(PS_Cost) );
		detailedLog(" Alfalfa production price from client = " + Float.toString(PA_Cost) );
		
		// Sell
		detailedLog(" Corn price from client = " + Float.toString(P_Per_Corn) );
		detailedLog(" Stover price from client = " + Float.toString(P_Per_Stover) );
		detailedLog(" Grass price from client = " + Float.toString(P_Per_Grass) );
		detailedLog(" Soy price from client = " + Float.toString(P_Per_Soy) );
		detailedLog(" Alfalfa price from client = " + Float.toString(P_Per_Alfalfa) );
		//----------------------------------------------------------------------		

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				float yield = calculatedYield[y][x];
				float ethanol = 0, netEnergy = 0, netIncome = 0;
				if (yield > -9999.0f) {
					if ((rotationData[y][x] & Corn_Mask) > 0) {
						// L per Ha
						ethanol = yield * 0.5f * CEO_C + yield * 0.25f * CEO_CS;
						// Net_Energy - MJ per Ha
						Net_Energy_C = (yield * 0.5f * CEO_C * EO_C) - (EI_CF + EI_CP * yield * 0.5f * CEO_C);
						Net_Energy_S = (yield * Prop_Stover_Harvest * 0.5f * CEO_CS * EO_CS) - (EI_CSF + EI_CSP * yield * Prop_Stover_Harvest * 0.5f * CEO_CS);
						netEnergy = Net_Energy_C + Net_Energy_S;
						// Gross inc return $ per Ha
						returnAmount = P_Per_Corn * 0.5f * yield + P_Per_Stover * Prop_Stover_Harvest * 0.5f * yield;
						// Net Income $ per Ha
						netIncome = returnAmount - PC_Cost - PCS_Cost;
					}
					else if ((rotationData[y][x] & Grass_Mask) > 0) {
						// L per Ha
						ethanol = yield * CEO_G;
						// MJ per Ha
						netEnergy = (yield * CEO_G * EO_G) - (EI_GF + EI_GP * yield * CEO_G);
						// Gross return $ per ha
						returnAmount = P_Per_Grass * yield;
						// Net Income $ per ha
						netIncome = returnAmount  - PG_Cost;
					}
					else if ((rotationData[y][x] & Soy_Mask) > 0) {
						// L per Ha
						ethanol = yield * CEO_S;
						// MJ per Ha
						netEnergy = (yield * 0.40f * CEO_S * EO_S) - (EI_SF + EI_SP * yield * CEO_S);
						// Soy return $ per Ha
						returnAmount = P_Per_Soy * yield;
						// Net Income $ per Ha
						netIncome = returnAmount  - PS_Cost;
					}
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) {
						// L per Ha
						ethanol = yield * CEO_A;
						// MJ per Ha
						netEnergy = (yield * CEO_A * EO_A) - (EI_AF + EI_AP * yield * CEO_A);
						// Alfalfa return $ per Ha
						returnAmount = P_Per_Alfalfa * yield;
						// Net Income $ per Ha
						netIncome = returnAmount  - PA_Cost;
					}
					
					// Convert L per Ha to L per cell
					//ethanolData[y][x] = Math.round(ethanol * 900.0f / 10000.0f * 100.0f) / 100.0f;
					ethanolData[y][x] = ethanol * 900.0f / 10000.0f;
					// Convert MJ per Ha to MJ per cell
					netEnergyData[y][x] = netEnergy * 900.0f / 10000.0f;
					// Convert $ per Ha to $ per cell
					netIncomeData[y][x] = netIncome * 900.0f / 10000.0f;
				}
				else {
					ethanolData[y][x] = -9999.0f;
					netEnergyData[y][x] = -9999.0f;
					netIncomeData[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult("ethanol", scenario.mOutputDir, ethanolData, width, height));
		results.add(new ModelResult("net_energy", scenario.mOutputDir, netEnergyData, width, height));
		results.add(new ModelResult("net_income", scenario.mOutputDir, netIncomeData, width, height));

long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model Ethanol / Net Energy / Net Income finished");
Logger.debug(" Execution timing: " + Float.toString(timeSec));

		return results;
	}
}

