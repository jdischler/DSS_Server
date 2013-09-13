package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

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
public class Model_Crop_Yield
{
	public float[] Crop_Y(Selection selection, int[][] RotationT)
	//public OneArray Crop_Y(Selection selection, int[][] RotationT)
	//public FourArrays Crop_Y(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		//int j = 0;
		int Total_Cells = selection.countSelectedPixels();
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Alfalfa_Mask = 128; // 8	
		
		// Corn and Grass Yield
		float Corn_Y = 0;
		float Grass_Y = 0;
		float Soy_Y = 0;
		float Alfalfa_Y = 0;
		
		// Define separate arrays to keep corn and grass production
		// Crop Yield
		float[] YI = new float[Total_Cells];
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();
		
		float slope[][] = Layer_Base.getLayer("Slope").getFloatData();
		float silt[][] = Layer_Base.getLayer("Silt").getFloatData();
		float depth[][] = Layer_Base.getLayer("Depth").getFloatData();
		float cec[][] = Layer_Base.getLayer("CEC").getFloatData();
		
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				// Corn
				if ((RotationT[y][x] & Corn_Mask) > 0)
				{
					// Calculate Corn Yield 
					// Bushels per Ac
					// Corn_Y = 3.08f - 0.11f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.10f * Float.parseFloat(silt[x]) + 0.04f * Float.parseFloat(cec[x]);
					//Corn_Y = -62.27f - (1.08f * Float.parseFloat(slope[x])) + 23.08f * (float)(Math.log((Double.parseDouble(soil[x])))) + 0.80f * Float.parseFloat(silt[x]) + 1.39f * Float.parseFloat(cec[x]);
					Corn_Y = 22.000f - 1.05f * slope[y][x] + 0.190f * depth[y][x] + 0.817f * silt[y][x] + 1.32f * cec[y][x];
					// Correct for techno advances
					Corn_Y = Corn_Y * 1.30f;
					// add stover
					Corn_Y = Corn_Y + Corn_Y;
					// Tonnes per Hec
					//Corn_YI[i] = Corn_Y * 0.053f;
					YI[i] = Corn_Y * 0.053f;
					// Tonnes per pixel
					//Corn_P[i] = 0.0001f * 900 * Corn_Y;
				}
				// Grass
				else if ((RotationT[y][x] & Grass_Mask) > 0)
				{
					// Calculate Grass Yield 
					// short tons per Ac
					// Grass_Y = 2.20f - 0.07f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.07f * Float.parseFloat(silt[x]) + 0.03f * Float.parseFloat(cec[x]);
					Grass_Y = 0.77f - 0.031f * slope[y][x] + 0.008f * depth[y][x] + 0.029f * silt[y][x] + 0.038f * cec[y][x];
					// Correct for techno advances
					Grass_Y = Grass_Y * 1.05f;
					// Tonnes per Hec
					//Grass_YI[i] = Grass_Y * 1.91f;
					YI[i] = Grass_Y * 1.91f;
					// Tonnes per pixel
					//Grass_P[i] = 0.0001f * 900 * Grass_Y;
				}
				// Soy
				else if ((RotationT[y][x] & Soy_Mask) > 0)
				{
					// Calculate Soy Yield 
					// Bushels per Ac
					//Soy_Y = -22.76f - (0.35f * Float.parseFloat(slope[x])) + 7.99f * (float)(Math.log((Double.parseDouble(soil[x])))) + 0.27f * Float.parseFloat(silt[x]) + 0.46f * Float.parseFloat(cec[x]);
					Soy_Y = 6.37f - 0.34f * slope[y][x] + 0.065f * depth[y][x] + 0.278f * silt[y][x] + 0.437f * cec[y][x];
					// Correct for techno advances
					Soy_Y = Soy_Y * 1.2f;
					// Tonnes per Hec
					Soy_Y = Soy_Y * 0.0585f;
					// add residue
					//Soy_YI[i] = Soy_Y + Soy_Y * 1.5f;
					YI[i] = Soy_Y + Soy_Y * 1.5f;
					// Tonnes per pixel
					//Soy_P[i] = 0.0001f * 900 * Soy_Y;
				}
				// Alfalfa
				else if ((RotationT[y][x] & Alfalfa_Mask) > 0)
				{
					// Calculate Alfalfa Yield 
					// Short tons per Acre
					// Alfalfa_Y = -1.98f - (0.04f * Float.parseFloat(slope[x])) + 0.88f * (float)(Math.log((Double.parseDouble(soil[x])))) + 0.03f * Float.parseFloat(silt[x]) + 0.04f * Float.parseFloat(cec[x]);
					Alfalfa_Y = 1.26f - 0.045f * slope[y][x] + 0.007f * depth[y][x] + 0.027f * silt[y][x] + 0.041f * cec[y][x];
					// Yield Correction Factor for modern yield
					Alfalfa_Y = Alfalfa_Y * 1.05f;
					// Tonnes per Hec
					//Alfalfa_YI[i] = Alfalfa_Y * 1.905f;
					YI[i] = Alfalfa_Y * 1.905f;
					//Tonnes per pixel
					//Alfalfa_P[i] = 0.0001f * 900 * Alfalfa_Y;
				}
				else 
				{
					YI[i] = 0;
				}
				
				i = i + 1;
			}
		}
		
		Logger.info("Model_Crop_Yield is finished");
		Logger.info("Number of selected cells are: " + Integer.toString(i));
		
		return YI;
	}
}

