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
public class Model_CropYield_New
{
	public float[][] run(int[][] rotationData, int width, int height) {
		
Logger.info(" >> Computing Yield");

		// Defining variables based on the selected layer
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Corn_Soy_Mask = 4; // 3
		int Alfalfa_Mask = 128; // 8	
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask | Corn_Soy_Mask;
		
		// Corn and Grass Yield
		float Corn_Y = 0;
		float Grass_Y = 0;
		float Soy_Y = 0;
		float Alfalfa_Y = 0;
		
		float Yield = 0;
		
		// Define separate arrays to keep corn and grass production
		// Crop Yield
		float[][] yield = new float[height][width];
Logger.info("  > Allocated memory for Yield");
		
		float slope[][] = Layer_Base.getLayer("Slope").getFloatData();
		float silt[][] = Layer_Base.getLayer("Silt").getFloatData();
		float depth[][] = Layer_Base.getLayer("Depth").getFloatData();
		float cec[][] = Layer_Base.getLayer("CEC").getFloatData();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				if ((rotationData[y][x] & TotalMask) > 0) {
					if ((rotationData[y][x] & Corn_Mask) > 0) {
						// Bushels per Ac
						Corn_Y = 22.000f - 1.05f * slope[y][x] + 0.190f * depth[y][x] + 0.817f * silt[y][x] + 1.32f * cec[y][x];
						// Correct for techno advances
						Corn_Y = Corn_Y * 1.30f;
						// add stover
						Corn_Y = Corn_Y + Corn_Y;
						// Tonnes per Hec
						Yield = Corn_Y * 0.053f;
						
					}
					else if ((rotationData[y][x] & Grass_Mask) > 0) {
						// short tons per Ac
						Grass_Y = 0.77f - 0.031f * slope[y][x] + 0.008f * depth[y][x] + 0.029f * silt[y][x] + 0.038f * cec[y][x];
						// Correct for techno advances
						Grass_Y = Grass_Y * 1.05f;
						// Tonnes per Hec
						Yield = Grass_Y * 1.91f;
						
					}
					else if ((rotationData[y][x] & Soy_Mask) > 0) {
						// Bushels per Ac
						Soy_Y = 6.37f - 0.34f * slope[y][x] + 0.065f * depth[y][x] + 0.278f * silt[y][x] + 0.437f * cec[y][x];
						// Correct for techno advances
						Soy_Y = Soy_Y * 1.2f;
						// Tonnes per Hec
						Soy_Y = Soy_Y * 0.0585f;
						// add residue
						Yield = Soy_Y + Soy_Y * 1.5f;
					}
					else if ((rotationData[y][x] & Corn_Soy_Mask) > 0) {
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
					}
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) {
						// Short tons per Acre
						Alfalfa_Y = 1.26f - 0.045f * slope[y][x] + 0.007f * depth[y][x] + 0.027f * silt[y][x] + 0.041f * cec[y][x];
						// Yield Correction Factor for modern yield
						Alfalfa_Y = Alfalfa_Y * 1.05f;
						// Tonnes per Hec
						Yield = Alfalfa_Y * 1.905f;
					}
					// Set Min and Max
					if(Yield < 0)
					{
						yield[y][x] = 0;
					}
					else if (Yield > 25) 
					{
						yield[y][x] = 25;
					}
					else 
					{
						yield[y][x] = Yield;
					}
				}
				else {
					yield[y][x] = -9999.0f;
				}
				
			}
		}
		
		return yield;
	}
}

