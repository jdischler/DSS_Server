package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses corn and grass production to calculate Ethanol
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn and grass production layers, selected cells in the raster map and crop rotation layer 
// Outputs are ASCII map of Ethanol
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Ethanol
{
	
	//--------------------------------------------------------------------------
	public void Ethanol(float[] Corn_Y, float[] Grass_Y, float[] Soy_Y, float[] Alfalfa_Y, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		float Ethanol = 0;
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Alfalfa_Mask = 128; // 8
		
		// Conversion Efficiency (L per Mg)
		float CEO_C = 400; // HILL
		float CEO_CS = 380; // EBAMM cellulosic
		float CEO_G = 380; // EBAMM cellulosic
		float CEO_S = 200; // Hill
		float CEO_A = 380; // EBAMM cellulosic
		
		// Retrive rotation layer from memory
		//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		if (RotationT == null)
		{
			Logger.info("Fail Rotation");
			layer = new Layer_Integer("Rotation"); layer.init();
			RotationT = Layer_Base.getLayer("Rotation").getIntData();
		}
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();
		
		try 
		{
			// Creating ASCII file to ouput Ethanol value
			PrintWriter out_E = new HeaderWrite("Ethanol", width, height, Output_Folder).getWriter();
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				
				// Make buffer to write outputs
				StringBuffer sb_E = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_E.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Calculate Ethanol bsaed on Corn Production 
							// Tonnes per Ha
							Ethanol = Corn_Y[i] * 0.5f * CEO_C + Corn_Y[i] * 0.25f * CEO_CS;
							sb_E.append(String.format("%.4f", Ethanol));
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							// Calculate Ethanol bsaed on Grass Production 
							// Tonnes per pixel
							Ethanol = Grass_Y[i] * CEO_G;
							sb_E.append(String.format("%.4f", Ethanol));
						}
						// Soy
						else if ((RotationT[y][x] & Soy_Mask) > 0)
						{
							// Calculate Ethanol bsaed on Soy Production 
							// Tonnes per pixel
							Ethanol = Soy_Y[i] * CEO_S;
							sb_E.append(String.format("%.4f", Ethanol));
						}
						// Alfalfa
						else if ((RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							// Calculate Ethanol bsaed on Alfalfa Production 
							// Tonnes per pixel
							Ethanol = Alfalfa_Y[i] * CEO_A;
							sb_E.append(String.format("%.4f", Ethanol));
						}
						else 
						{
							sb_E.append(stringNoData);
						}
					
						
						i = i + 1;
					}
					if (x != width - 1) 
					{
						sb_E.append(" ");
					}
				}
				out_E.println(sb_E.toString());
			}
			// Close output files
			out_E.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
	}
	
}
