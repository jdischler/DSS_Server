package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
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
public class Model_Soil_Carbon
{
	
	//----------------------------------------------------------------------
	public void Soil_Carbon(Selection selection, int[][] RotationD, int[][] RotationT)
	//public OneArray Soil_Carbon(Selection selection, int[][] RotationD, int[][] RotationT)
	//public OneArray Soil_Carbon(float[] YI, Selection selection, int[][] RotationT)
	//public void Net_Energy(float[] Corn_Y, float[] Grass_Y, float[] Soy_Y, float[] Alfalfa_Y, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Alfalfa_Mask = 128; // 8
		
		// Constant for Root To Shoot Ratios
		//float RSR_Corn = 0.18f; // Continuous Corn
		//float RSR_Corn_Soy = 0.165f; // Corn and Soy Rotation
		//float RSR_Corn_Alpha = 0.525f; // Corn and alfalfa Rotation
		//float RSR_ = 0.345f; // Unknown but should be in same category like Corn
		//float RSR_Soy = 0.15f; // Continuous Soy
		//float RSR_Alpha = 0.87f; // Continuous Alfalfa
		//float RSR_D_Forest = 0.23f; // Deciduous Forest
		//float RSR_C_Forest = 0.18f; // Coniferous Forest
		//float RSR_Grass = 3.43f; // Grass
		
		// Raw Soil Carbon Change Factor (RSCCF) 
		//float RSCCF_Corn_Soy = 1.0f; // Continuous Corn to Soy
		float RSCCF_Corn_Grass = 1.63f; // Continuous Corn to Grass
		float RSCCF_Corn_Alfalfa = 1.37f; // Continuous Corn to Alfalfa
		float RSCCF_Soy_Grass = 1.63f; // Continuous Soy to Grass
		float RSCCF_Soy_Alfalfa = 1.37f; // Continuous Soy to Alfalfa
		float RSCCF_Grass_Alfalfa = 0.85f; // Continuous Grass to Alfalfa
		float factor = 1.0f;
		// Soil Carbon
		//int[] Soil_CarbonD = new float[Total_Cells];
		//int[] Soil_CarbonT = new float[Total_Cells];
		
		//float Min_SC =  1000000;
		//float Max_SC = -1000000;
		
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();
		
		int[][] SOC_D = Layer_Base.getLayer("SOC").getIntData();
		int[] SOC_T = new int[Total_Cells];
		
		try 
		{	
			// Soil_Carbon
			PrintWriter out_SC = new HeaderWrite("Soil_Carbon", width, height, "Client_ID").getWriter();
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				// Outputs
				StringBuffer sb_SC = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					//Logger.info(Integer.toString(SOC_D[y][x]));
					
					if (RotationD[y][x] == 0 || RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_SC.append(stringNoData);
					}
					else
					//else if (selection.mSelection[y][x] == 1)
					{
						// Corn to Grass
						if ((RotationD[y][x] & Corn_Mask) > 0 && (RotationT[y][x] & Grass_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = RSCCF_Corn_Grass;
							//Soil_Carbon[i] = YI[i] + SOC[y][x] * RSR_Corn * 0.0058f;
							// SOC_T[i] = (int)(SOC_D[y][x] * RSCCF_Corn_Grass * (-0.5938f * Math.log(SOC_D[y][x] * 0.1f) + 1.6524f));
							//Min_SC = Min(Min_SC, Soil_Carbon[i]);
							//Max_SC = Max(Max_SC, Soil_Carbon[i]);
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Corn to Alfalfa
						else if ((RotationD[y][x] & Corn_Mask) > 0 && (RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = RSCCF_Corn_Alfalfa;
							//SOC_T[i] = (int)(SOC_D[y][x] * RSCCF_Corn_Alfalfa * (-0.5938f * Math.log(SOC_D[y][x] * 0.1f) + 1.6524f));
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Soy to Grass
						else if ((RotationD[y][x] & Soy_Mask) > 0 && (RotationT[y][x] & Grass_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = RSCCF_Soy_Grass;
							//SOC_T[i] = (int)(SOC_D[y][x] * RSCCF_Soy_Grass * (-0.5938f * Math.log(SOC_D[y][x] * 0.1f) + 1.6524));
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Soy to Alfalfa
						else if ((RotationD[y][x] & Soy_Mask) > 0 && (RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = RSCCF_Soy_Alfalfa;
							//SOC_T[i] = SOC_D[y][x] * RSCCF_Soy_Alfalfa * (-0.5938f * Math.Log(SOC_D[y][x] * 0.1f) + 1.6524);
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Grass to Alfalfa
						else if ((RotationD[y][x] & Grass_Mask) > 0 && (RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = RSCCF_Grass_Alfalfa;
							//SOC_T[i] = SOC_D[y][x] * RSCCF_Grass_Alfalfa * (-0.5938f * Math.Log(SOC_D[y][x] * 0.1f) + 1.6524);
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Grass to Corn
						else if ((RotationD[y][x] & Grass_Mask) > 0 && (RotationT[y][x] & Corn_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = 1 / RSCCF_Corn_Grass;
							//SOC_T[i] = SOC_D[y][x] * (-0.5938f * Math.Log(SOC_D[y][x] * 0.1f) + 1.6524) / RSCCF_Corn_Grass;
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Alfalfa to Corn
						else if ((RotationD[y][x] & Alfalfa_Mask) > 0 && (RotationT[y][x] & Corn_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = 1 / RSCCF_Corn_Alfalfa;
							//SOC_T[i] = SOC_D[y][x] * (-0.5938f * Math.Log(SOC_D[y][x] * 0.1f) + 1.6524) / RSCCF_Corn_Alfalfa;
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Grass to Soy
						else if ((RotationD[y][x] & Grass_Mask) > 0 && (RotationT[y][x] & Soy_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = 1 / RSCCF_Soy_Grass;
							//SOC_T[i] = SOC_D[y][x] * (-0.5938f * Math.Log(SOC_D[y][x] * 0.1f) + 1.6524) / RSCCF_Soy_Grass;
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Alfalfa to Soy
						else if ((RotationD[y][x] & Alfalfa_Mask) > 0 && (RotationT[y][x] & Soy_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = 1 / RSCCF_Soy_Alfalfa;
							//SOC_T[i] = SOC_D[y][x] * (-0.5938f * Math.Log(SOC_D[y][x] * 0.1f) + 1.6524) / RSCCF_Soy_Alfalfa;
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						// Alfalfa to Grass
						else if ((RotationD[y][x] & Alfalfa_Mask) > 0 && (RotationT[y][x] & Grass_Mask) > 0)
						{
							
							// Tonnes per Ha
							factor = 1 / RSCCF_Grass_Alfalfa;
							//SOC_T[i] = SOC_D[y][x] * (-0.5938f * Math.Log(SOC_D[y][x] * 0.1f) + 1.6524) / RSCCF_Grass_Alfalfa;
							
							//i = i + 1;
							
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						else 
						{
							factor = 1;
							//SOC_T[i] = SOC_D[i];
							//sb_SC.append(String.format("%.4f", SOC_T[i]));
						}
						
						SOC_T[i] = (int)(SOC_D[y][x] * factor * (-0.5938f * Math.log10(SOC_D[y][x] * 0.1f) + 1.6524f));
						//sb_SC.append(String.format("%.4f", SOC_T[i]));
						sb_SC.append(Integer.toString(SOC_T[i]));
						
						i = i + 1;
					}
					if (x != width - 1) 
					{
						sb_SC.append(" ");
					}
				}
				
				out_SC.println(sb_SC.toString());
			}
			
			// Close output files
			out_SC.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
		
		Logger.info("Model_Soil_Carbon is finished");
				
		//return new OneArray(Soil_Carbon, Min_SC, Max_SC);
	}
	
	// Min
	public float Min(float Min, float Num)
	{ 
		// Min
		if (Num < Min)
		{
			Min = Num;
		}
		
		return Min;
	}
	
	// Max
	public float Max(float Max, float Num)
	{

		// Max
		if (Num > Max)
		{
			Max = Num;
		}
		
		return Max;
	}
	
}
