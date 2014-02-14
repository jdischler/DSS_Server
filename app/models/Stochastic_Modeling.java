package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//import org.codehaus.jackson.*;
//import org.codehaus.jackson.node.*;
//import java.lang.reflect.Array;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses random numbers deviated from average for each ecological models to add a stochastic process to the models 
// Inputs can change randomly for different inputs
// This program run every models for more than 1000000 times to calculate the stable outcome for each model
// This proigram takes into account randomness in different parts for each models
// Version 10/20/2013
//
//------------------------------------------------------------------------------
public class Stochastic_Modeling
{

	//--------------------------------------------------------------------------
	public float[] Stochastic_Modeling() 
	{
		
Logger.info(">>> Computing Stochastic Modeling");
long timeStart = System.currentTimeMillis();
		
		// Number of iteration for each models
		int iteration = 1000000;
		
		// Results
		float[] results = new float[iteration];

		// Used variables to save random values for each models
		float Net_E_C;
		float Net_E_S;
		float Lambda;
		int R_CropType;
		
		// Used variables to save random proportion values
		float EI_CF;
		float EI_CSF;
		float EI_GF;
		float EI_SF;
		float EI_AF;
		
		float EO_C;
		float EO_CS;
		float EO_G;
		float EO_S;
		float EO_A;
		
		float CEO_C;
		float CEO_CS;
		float CEO_G;
		float CEO_S;
		float CEO_A;
		
		float PC_Cost;
		float PCS_Cost;
		float PG_Cost;
		float PS_Cost;
		float PA_Cost;
		
		float P_Per_Corn;
		float P_Per_Stover;
		float P_Per_Grass;
		float P_Per_Soy;
		float P_Per_Alfalfa;
		
		float R_Prop_Ag;
		float R_Prop_Grass;
		float R_Prop_Forest;
		
		float Crop_Rotation_C;
		float Fert_Rate_C;
		float Crop_Rotation_G;
		float Fert_Rate_G;
		float Crop_Rotation_S;
		float Fert_Rate_S;
		float Crop_Rotation_A;
		float Fert_Rate_A;
		
		// Used arrays to save random values for each models 
		float[] Yield_C = new float[iteration];
		float[] Ethanol_C = new float[iteration];
		float[] Net_Energy_C = new float[iteration];
		float[] Net_Income_C = new float[iteration];
		float[] Yield_G = new float[iteration];
		float[] Ethanol_G = new float[iteration];
		float[] Net_Energy_G = new float[iteration];
		float[] Net_Income_G = new float[iteration];
		float[] Yield_S = new float[iteration];
		float[] Ethanol_S = new float[iteration];
		float[] Net_Energy_S = new float[iteration];
		float[] Net_Income_S = new float[iteration];
		float[] Yield_A = new float[iteration];
		float[] Ethanol_A = new float[iteration];
		float[] Net_Energy_A = new float[iteration];
		float[] Net_Income_A = new float[iteration];
		float[] Yield = new float[iteration];
		float[] Ethanol = new float[iteration];
		float[] Net_Energy = new float[iteration];
		float[] Net_Income = new float[iteration];
		
		float[] Habitat_Index = new float[iteration];
		float[] Nitrogen = new float[iteration];
		float[] Phosphorus = new float[iteration];
		float[] Nitrous_Oxide_Emissions_C = new float[iteration];
		float[] Nitrous_Oxide_Emissions_G = new float[iteration];
		float[] Nitrous_Oxide_Emissions_S = new float[iteration];
		float[] Nitrous_Oxide_Emissions_A = new float[iteration];
		float[] Pollinator = new float[iteration];
		float[] Pest_Suppression = new float[iteration];
		float[] Soil_Carbon = new float[iteration];
		// Used variables to save random values for coefficients of each models
		// Proportion of Stover 
		float Prop_Stover_Harvest;
		float Return_Amount;	// Gross return
		
		// Formula to assign random number betwen two values
		// Min + (int)(Math.random() * ((Max - Min) + 1))
		float Min = 0;
		float Max = 1;
		
		// For loop to apply randomness to the models and calculate the outcome
		for (int y = 0; y < iteration; y++) 
		{
			// Calculate ethanol, net income and net energy
			// Corn
			// Calculate yield (between 0 to 25)
			Yield_C[y] = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Energy Input at Farm (MJ per Ha; EI_CF -> 18151; EI_CSF -> 2121f)
			EI_CF =  Min + (float)(Math.random() * ((Max - Min) + 1)); // HILL
			EI_CSF = Min + (float)(Math.random() * ((Max - Min) + 1)); // HILL 1/4 fuel use for stover harvest
			// Energy output (MJ per L; EO_C -> 21.26f + 4.31f; EO_CS -> 21.26f + 3.40f)
			EO_C = Min + (float)(Math.random() * ((Max - Min) + 1)); // HILL
			EO_CS = Min + (float)(Math.random() * ((Max - Min) + 1)); // EBAMM cellulosic
			// Conversion Efficiency (L per Mg; CEO_C -> 400; CEO_CS -> 380)
			CEO_C = Min + (float)(Math.random() * ((Max - Min) + 1)); // HILL
			CEO_CS = Min + (float)(Math.random() * ((Max - Min) + 1)); // EBAMM cellulosic
			// Production cost (PC_Cost -> 1135; PCS_Cost -> 412)
			PC_Cost = Min + (float)(Math.random() * ((Max - Min) + 1)); // $ per hec cost for Corn
			PCS_Cost = Min + (float)(Math.random() * ((Max - Min) + 1)); // $ per hec cost for Corn Stover
			// Price per tonne (P_Per_Corn -> 274; P_Per_Stover -> 70)
			P_Per_Corn = Min + (float)(Math.random() * ((Max - Min) + 1));
			P_Per_Stover = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Proportion of Stover (Prop_Stover_Harvest -> 0.38f)
			Prop_Stover_Harvest = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Tonnes per Ha
			Ethanol_C[y] = Yield_C[y] * 0.5f * CEO_C + Yield_C[y] * 0.25f * CEO_CS;
			// Net_Energy - MJ per Ha
			//Net_E_C = (Yield_C[y] * 0.5f * CEO_C * EO_C) - (EI_CF + EI_CP * Yield_C[y] * 0.5f * CEO_C);
			//Net_E_S = (Yield_C[y] * Prop_Stover_Harvest * 0.5f * CEO_CS * EO_CS) - (EI_CSF + EI_CSP * Yield_C[y] * Prop_Stover_Harvest * 0.5f * CEO_CS);
			//Net_Energy_C[y] = Net_E_C + Net_E_S;
			// Gross inc return $ per hec
			Return_Amount = P_Per_Corn * 0.5f * Yield_C[y] + P_Per_Stover * Prop_Stover_Harvest * 0.5f * Yield_C[y];
			// Net Income $ per hec
			Net_Income_C[y] = Return_Amount - PC_Cost - PCS_Cost;
			
			// Grass
			// Calculate yield (between 0 to 25)
			Yield_G[y] = 0 + (float)(Math.random() * ((25 - 0) + 1));
			// Energy Input at Farm (MJ per Ha; EI_GF -> 7411f)
			EI_GF = Min + (float)(Math.random() * ((Max - Min) + 1)); // EBAMM
			// Energy output (MJ per L; EO_G -> 21.26f + 3.40f)
			EO_G = Min + (float)(Math.random() * ((Max - Min) + 1)); // EBAMM cellulosic
			// Conversion Efficiency (L per Mg; CEO_G -> 380)
			CEO_G = Min + (float)(Math.random() * ((Max - Min) + 1)); // EBAMM cellulosic
			// Production cost (PG_Cost -> 412)
			PG_Cost = Min + (float)(Math.random() * ((Max - Min) + 1)); // $ per hec cost for Grass
			// Price per tonne (P_Per_Grass -> 107)
			P_Per_Grass = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Tonnes per pixel
			Ethanol_G[y] = Yield_G[y] * CEO_G;
			// MJ per Ha
			//Net_Energy_G[y] = (Yield_G[y] * CEO_G * EO_G) - (EI_GF + EI_GP * Yield_G[y] * CEO_G);
			// Gross return $ per pixel
			Return_Amount = P_Per_Grass * Yield_G[y];
			// Net Income $ per pixel
			Net_Income_G[y] = Return_Amount - PG_Cost;
			
			// Soy
			// Calculate yield (between 0 to 25)
			Yield_S[y] = 0 + (float)(Math.random() * ((25 - 0) + 1));
			// Energy Input at Farm (MJ per Ha; EI_SF -> 6096f)
			EI_SF = Min + (float)(Math.random() * ((Max - Min) + 1)); // Hill
			// Energy output (MJ per L; EO_S -> 32.93f + 21.94f;)
			EO_S = Min + (float)(Math.random() * ((Max - Min) + 1)); // Hill
			// Conversion Efficiency (L per Mg; CEO_S -> 200)
			CEO_S = Min + (float)(Math.random() * ((Max - Min) + 1)); // Hill
			// Production cost (PS_Cost -> 627)
			PS_Cost = Min + (float)(Math.random() * ((Max - Min) + 1)); // $ per hec cost for Soy
			// Price per tonne (P_Per_Soy -> 249)
			P_Per_Soy = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Tonnes per pixel
			Ethanol_S[y] = Yield_S[y] * CEO_S;
			// MJ per Ha
			//Net_Energy_S[y] = (Yield_S[y] * 0.40f * CEO_S * EO_S) - (EI_SF + EI_SP * Yield_S[y] * CEO_S);
			// Soy return $ per pixel
			Return_Amount = P_Per_Soy * Yield_S[y];
			// Net Income $ per pixel
			Net_Income_S[y] = Return_Amount - PS_Cost;
			
			// Alfalfa
			// Calculate yield (between 0 to 25)
			Yield_A[y] = 0 + (float)(Math.random() * ((25 - 0) + 1));
			// Energy Input at Farm (MJ per Ha; EI_AF -> 9075f)
			EI_AF = Min + (float)(Math.random() * ((Max - Min) + 1)); // Corn Grain Hill * 1/2
			// Energy output (MJ per L; EO_A -> 21.26f + 3.40f)
			EO_A = Min + (float)(Math.random() * ((Max - Min) + 1)); // EBAMM cellulosic
			// Conversion Efficiency (L per Mg; CEO_A -> 380)
			CEO_A = Min + (float)(Math.random() * ((Max - Min) + 1)); // EBAMM cellulosic
			// Production cost (PA_Cost -> 620)
			PA_Cost = Min + (float)(Math.random() * ((Max - Min) + 1)); // $ per hec cost for Alfalfa
			// Price per tonne (P_Per_Alfalfa -> 230)
			P_Per_Alfalfa = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Tonnes per pixel
			Ethanol_A[y] = Yield_A[y] * CEO_A;
			// MJ per Ha
			//Net_Energy_A[y] = (Yield_A[y] * CEO_A * EO_A) - (EI_AF + EI_AP * Yield_A[y] * CEO_A);
			// Alfalfa return $ per pixel
			Return_Amount = P_Per_Alfalfa * Yield_A[y];
			// Net Income $ per pixel
			Net_Income_A[y] = Return_Amount - PA_Cost;

			// Calculate habitat index (between 0 to 1)
			R_Prop_Ag = (float)(Math.random());
			R_Prop_Grass = (float)(Math.random());
			Lambda = -4.47f + (2.95f * R_Prop_Ag) + (5.17f * R_Prop_Grass); 
			Habitat_Index[y] = (float)((1.0f / (1.0f / Math.exp(Lambda) + 1.0f )) / 0.67f);
			// Calculate nitrogen and phophorous (between 0 to 1)
			R_Prop_Ag = (float)(Math.random());
			Nitrogen[y] = (float)Math.pow(10, 1.13f * R_Prop_Ag - 0.23f);
			Phosphorus[y] = (float)Math.pow(10, 0.79f * R_Prop_Ag - 1.44f);
			
			// Calculate N2O
			// Corn (Crop_Rotation_C -> 0.0f; Fert_Rate_C -> 168.0f)
			Crop_Rotation_C = 0;
			Fert_Rate_C = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Grass (Crop_Rotation_G -> -1.268f; Fert_Rate_G -> 56.0f)
			Crop_Rotation_G = Min + (float)(Math.random() * ((Max - Min) + 1));
			Fert_Rate_G = Min + (float)(Math.random() * ((Max - Min) + 1));
			// Soy (Crop_Rotation_S -> -1.023f; Fert_Rate_S -> 0.0f)
			Crop_Rotation_S = Min + (float)(Math.random() * ((Max - Min) + 1));
			Fert_Rate_S = 0;
			// Alfalfa (Crop_Rotation_A -> -1.023f; Fert_Rate_S -> 0.0f)
			Crop_Rotation_A = Min + (float)(Math.random() * ((Max - Min) + 1));
			Fert_Rate_A = 0;
			// Calculate outcome
			//Nitrous_Oxide_Emissions_C[y] = (float)(Math.exp(0.414f + 0.825f + Fert_Rate_C * 0.005f + Crop_Rotation_C + texture[y][x] + OM_SOC[y][x] + drainage[y][x] + pH[y][x]));
			//Nitrous_Oxide_Emissions_G[y] = (float)(Math.exp(0.414f + 0.825f + Fert_Rate_G * 0.005f + Crop_Rotation_G + texture[y][x] + OM_SOC[y][x] + drainage[y][x] + pH[y][x]));
			//Nitrous_Oxide_Emissions_S[y] = (float)(Math.exp(0.414f + 0.825f + Fert_Rate_S * 0.005f + Crop_Rotation_S + texture[y][x] + OM_SOC[y][x] + drainage[y][x] + pH[y][x]));
			//Nitrous_Oxide_Emissions_A[y] = (float)(Math.exp(0.414f + 0.825f + Fert_Rate_A * 0.005f + Crop_Rotation_A + texture[y][x] + OM_SOC[y][x] + drainage[y][x] + pH[y][x]));
			
			// Calculate pollinator and pest suppression
			R_Prop_Forest = (float)(Math.random());
			R_Prop_Grass = (float)(Math.random());
			Pollinator[y] = (float)(Math.pow(0.6617f + (2.98f * R_Prop_Forest) + (1.83f * R_Prop_Grass), 2.0f));
			Random CropType = new Random();
			R_CropType = CropType.nextInt(1);
			Pest_Suppression[y] = 0.25f + (0.19f * R_CropType) + (0.62f * R_Prop_Grass);	
			
			// Calculate soil carbon
			
		}
		
		// Calculate average and stantard deviation
		for (int y = 0; y < iteration; y++) 
		{
			
		}
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Stochastic Modeling is finished - timing: " + Float.toString(timeSec));

		return results;
	}
}

