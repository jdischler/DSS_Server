package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import java.lang.reflect.Array;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses corn, soy, grass and alfalfa production to calculate Net Energy 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Outputs are ASCII map of Net Energy
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_EthanolNetEnergyIncome_New extends Model_Base
{
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
		Model_CropYield_New cropYield = new Model_CropYield_New();
		float[][] calculatedYield = cropYield.run(scenario);

		float [][] netEnergyData = new float[height][width];
		float [][] netIncomeData = new float[height][width];
		float [][] ethanolData = new float[height][width];
Logger.info("  > Allocated memory for NetEnergy, NetIncom, Fuel");
		
		float Net_Energy_C = 0;
		float Net_Energy_S = 0;
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Corn_Soy_Mask = 4; // 3
		int Alfalfa_Mask = 128; // 8	
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask | Corn_Soy_Mask;
		
		// Proportion of Stover 
		float Prop_Stover_Harvest = 0.38f;
		
		// Energy Input at Farm (MJ per Ha)
		float EI_CF = 18151f; // HILL
		float EI_CSF = 2121f; // HILL 1/4 fuel use for stover harvest
		float EI_GF = 7411f; // EBAMM
		float EI_SF = 6096f; // Hill
		float EI_AF = 9075f; // Corn Grain Hill * 1/2
		
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
		float CEO_G = 380; // EBAMM cellulosic
		float CEO_S = 200; // Hill
		float CEO_A = 380; // EBAMM cellulosic
		
		float returnAmount;	// Gross return
		
		float PC_Cost = 1135; // $ per hec cost for Corn
		float PCS_Cost = 412; // $ per hec cost for Corn Stover
		float PG_Cost = 412; // $ per hec cost for Grass
		float PS_Cost = 627; // $ per hec cost for Soy
		float PA_Cost = 620; // $ per hec cost for Alfalfa
		
		// Price per tonne
		float P_Per_Corn = 274;
		float P_Per_Stover = 70;
		float P_Per_Grass = 107;
		float P_Per_Soy = 249;
		float P_Per_Alfalfa = 230;

		// Get user changeable values from the client...
		//----------------------------------------------------------------------
		// NOTE: this is just a sample of how to do it
		float cornPrice = P_Per_Corn;
		
		try {		
			cornPrice = scenario.mAssumptions.getAssumptionFloat("p_corn");
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info(" Corn price from client = " + Float.toString(cornPrice) );
		
		//----------------------------------------------------------------------		

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float yield = calculatedYield[y][x];
				float ethanol = 0, netEnergy = 0, netIncome = 0;
				if (yield > -9999.0f) {
					if ((rotationData[y][x] & Corn_Mask) > 0) {
						// Tonnes per Ha
						ethanol = yield * 0.5f * CEO_C + yield * 0.25f * CEO_CS;
						// Net_Energy - MJ per Ha
						Net_Energy_C = (yield * 0.5f * CEO_C * EO_C) - (EI_CF + EI_CP * yield * 0.5f * CEO_C);
						Net_Energy_S = (yield * Prop_Stover_Harvest * 0.5f * CEO_CS * EO_CS) - (EI_CSF + EI_CSP * yield * Prop_Stover_Harvest * 0.5f * CEO_CS);
						netEnergy = Net_Energy_C + Net_Energy_S;
						// Gross inc return $ per hec
						returnAmount = P_Per_Corn * 0.5f * yield + P_Per_Stover * Prop_Stover_Harvest * 0.5f * yield;
						// Net Income $ per hec
						netIncome = returnAmount - PC_Cost - PCS_Cost;
					}
					else if ((rotationData[y][x] & Grass_Mask) > 0) {
						// Tonnes per pixel
						ethanol = yield * CEO_G;
						// MJ per Ha
						netEnergy = (yield * CEO_G * EO_G) - (EI_GF + EI_GP * yield * CEO_G);
						// Gross return $ per pixel
						returnAmount = P_Per_Grass * yield;
						// Net Income $ per pixel
						netIncome = returnAmount  - PG_Cost;
					}
					else if ((rotationData[y][x] & Soy_Mask) > 0) {
						// Tonnes per pixel
						ethanol = yield * CEO_S;
						// MJ per Ha
						netEnergy = (yield * 0.40f * CEO_S * EO_S) - (EI_SF + EI_SP * yield * CEO_S);
						// Soy return $ per pixel
						returnAmount = P_Per_Soy * yield;
						// Net Income $ per pixel
						netIncome = returnAmount  - PS_Cost;
					}
					else if ((rotationData[y][x] & Corn_Soy_Mask) > 0) {
						// Tonnes per pixel
						ethanol = (yield * 0.5f * CEO_C + yield * 0.25f * CEO_CS + yield * CEO_S) / 2;
						// MJ per Ha
						netEnergy = (((yield * 0.5f * CEO_C * EO_C) - (EI_CF + EI_CP * yield * 0.5f * CEO_C) + (yield * Prop_Stover_Harvest * 0.5f * CEO_CS * EO_CS) - (EI_CSF + EI_CSP * yield * Prop_Stover_Harvest * 0.5f * CEO_CS)) + ((yield * 0.40f * CEO_S * EO_S) - (EI_SF + EI_SP * yield * CEO_S))) / 2;
						// Gross inc return $ per hec
						returnAmount = P_Per_Corn * 0.5f * yield + P_Per_Stover * Prop_Stover_Harvest * 0.5f * yield + P_Per_Soy * yield;
						// Net Income $ per hec
						netIncome = returnAmount - PC_Cost - PCS_Cost - PS_Cost;
					}
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) {
						// Tonnes per pixel
						ethanol = yield * CEO_A;
						// MJ per Ha
						netEnergy = (yield * CEO_A * EO_A) - (EI_AF + EI_AP * yield * CEO_A);
						// Alfalfa return $ per pixel
						returnAmount = P_Per_Alfalfa * yield;
						// Net Income $ per pixel
						netIncome = returnAmount  - PA_Cost;
					}
					
					ethanolData[y][x] = ethanol;
					netEnergyData[y][x] = netEnergy;
					netIncomeData[y][x] = netIncome;
				}
				else {
					ethanolData[y][x] = -9999.0f;
					netEnergyData[y][x] = -9999.0f;
					netIncomeData[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
//		results.add(new ModelResult("yeild", scenario.mOutputDir, calculatedYield, width, height));
		results.add(new ModelResult("ethanol", scenario.mOutputDir, ethanolData, width, height));
		results.add(new ModelResult("net_energy", scenario.mOutputDir, netEnergyData, width, height));
		results.add(new ModelResult("net_income", scenario.mOutputDir, netIncomeData, width, height));

long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model Ethanol / Net Energy / Net Income is finished - timing: " + Float.toString(timeSec));

		return results;
	}
}

