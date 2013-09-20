package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses to calculate nitrous oxide emissions(Tonnes per hec) 
// This model is from unpublished work by 
// Inputs are layers, selected cells in the raster map and crop rotation layer 
// Outputs are ASCII map of nitrous oxide emissions
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Nitrous_Oxide_Emissions
{
	public float[] Nitrous_Oxide_Emissions(Selection selection, int[][] RotationT, String Output_Folder)
	//public OneArray Crop_Y(Selection selection, int[][] RotationT)
	//public FourArrays Crop_Y(Selection selection, String Output_Folder, int[][] RotationT)
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
		
		// Model Nitrous Oxide Emissions
		float[] NOE = new float[Total_Cells];
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();
		
		// Input layers
		float Texture[][] = Layer_Base.getLayer("Texture").getFloatData();
		float OM_SOC[][] = Layer_Base.getLayer("OM_SOC").getFloatData();
		float Drainage[][] = Layer_Base.getLayer("Drainage").getFloatData();
		float pH[][] = Layer_Base.getLayer("PH").getFloatData();
			
		// Constant for input layers
		float Crop_Rotation = 0;
		float Soil_Texture = 0;
		float Soil_SOC = 0;
		float Soil_Drain = 0;
		float Soil_pH = 0;
		float FertRate = 0;
		
		try
		{
			// Nitrous Oxide Emissions(N2O)
			PrintWriter out_NOE = new HeaderWrite("N2O_Emissions", width, height, Output_Folder).getWriter();
				
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
				
			// Calculate Nitrous Oxide Emissions
			for (int y = 0; y < height; y++) 
			{
				
				// Outputs
				StringBuffer sb_NOE = new StringBuffer();
					
				for (int x = 0; x < width; x++) 
				{
					
					if (selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_NOE.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						
						// Update constants for each cell
						// Drainage
						// Good
						// if (Drainage[y][x] == 1)
						// {
							// Soil_Drain = -0.420f;
						// }
						// // Poor
						// else
						// {
							// Soil_Drain = 0;
						// }
						 
						// // Crop
						// Cron 
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							Crop_Rotation = 0;
							FertRate = 168;
						}
						// // Grass
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Crop_Rotation = -1.268f;
							FertRate = 56;
						}
						// Soy
						else if ((RotationT[y][x] & Soy_Mask) > 0)
						{
							Crop_Rotation = -1.023f;
							FertRate = 0;
						}
						// Alfalfa
						else if ((RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							Crop_Rotation = -1.023f;
							FertRate = 0;
						}
						// Other crops
						else 
						{
							Crop_Rotation = 0;
							FertRate = 0;
						}
						 
						// // Soil Texture
						// // Coarse
						// if (Texture[y][x] == 1)
						// {
							// Soil_Texture = -0.008f;
						// }
						// // Medium
						// else if (Texture[y][x] == 2)
						// {
							// Soil_Texture = -0.472f;
						// }
						// // Fine
						// else
						// {
							// Soil_Texture = 0;
						// }
						
						// // SOC
						// // Less than 1
						// if (SOC[y][x] < 1)
						// {
							// Soil_SOC = 0;
						// }
						// // Between 1 and 3
						// else if (SOC[y][x] >= 1 && SOC[y][x] < 3)
						// {
							// Soil_SOC = 0.140f;
						// }
						// // Between 3 and 6
						// else if (SOC[y][x] >= 3 && SOC[y][x] < 6)
						// {
							// Soil_SOC = 0.580f;
						// }
						// else if (SOC[y][x] >= 6)
						// {
							// Soil_SOC = 1.045f;
						// }
						
						// // Soil pH
						// // Less than 5.5
						// if (pH[y][x] < 5.5)
						// {
							// Soil_pH = 0;
						// }
						// // Between 1 and 3
						// else if (pH[y][x] >= 5.5 && pH[y][x] < 7.3)
						// {
							// Soil_pH = 0.109f;
						// }
						// // Greater than 7.3
						// else if (pH[y][x] >= 7.3)
						// {
							// Soil_pH = -0.352f;
						// }
						if (Texture[y][x] > -9999.0 && OM_SOC[y][x] > -9999.0 && Drainage[y][x] > -9999.0 && pH[y][x] > -9999.0)
						{
							// Calculate Nitrous Oxide Emissions
							NOE[i] = (float)(Math.exp(0.414f + 0.825f + FertRate * 0.005f + Crop_Rotation + Texture[y][x] + OM_SOC[y][x] + Drainage[y][x] + pH[y][x]));
							sb_NOE.append(NOE[i]);
						}
						else 
						{
							NOE[i] = -9999;
							sb_NOE.append(stringNoData);
						}
						
						i = i + 1;
					}
					if (x != width - 1) 
					{
						sb_NOE.append(" ");
					}
				}
				
				out_NOE.println(sb_NOE.toString());
			}
			
			// Close output files
			out_NOE.close();
	}
	catch(Exception err) 
	{
		Logger.info(err.toString());
		Logger.info("Oops, something went wrong with writing to the files!");
	}
		
	Logger.info("Model_Nitrous_Oxide_Emissions is finished");
	Logger.info("Number of selected cells are: " + Integer.toString(i));
		
	return NOE;
	}
}

