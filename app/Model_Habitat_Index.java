package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses landscape proportion to calculate bird habitat index (Grass land habitat index) 
// using 390m rectangle buffers 
// Index can vary from 0 to 1
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are proportion of land cover particularly grass and Ag, selected cells in the raster map and crop rotation layer 
// ASCII map of habitat index
// Version 08/20/2013
//------------------------------------------------------------------------------
public class Model_Habitat_Index
{
	// Define habitat index function
	//--------------------------------------------------------------------------
	public void Habitat_Index(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		float Habitat_Index = 0;
							
		int Buffer = 390; // In Meter
		int Window_Size = Buffer / 30; // Number of Cells in Raster Map
		float Prop_Ag = 0;
		int Count_Ag = 0;
		int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		float Prop_Grass = 0;
		int Count_Grass = 0;
		int Grass_Mask = 128 + 256; // 8 and 9
		int Corn_Mask = 1; // 1
						
		// Retrive rotation layer from memory
		// int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		if (RotationT == null)
		{
			Logger.info("Fail Rotation");
			layer = new Layer_Raw("Rotation"); layer.init();
			//Rotation = Layer_Base.getLayer("Rotation").getIntData();
		}
			layer = Layer_Base.getLayer("Rotation");
			width = layer.getWidth();
			height = layer.getHeight();
		
		try 
		{
			// Creating ASCII file to ouput Bird Index value
			PrintWriter out_HI = new HeaderWrite("Habitat_Index", width, height, Output_Folder).getWriter();

			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				// Outputs
				StringBuffer sb_HI = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{				
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data Value
						sb_HI.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						
						// Calling the moving window class to initialize the boundary of moving window
						Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
						// Calling the moving window class to calculate landscape proportion
						float[] Proportion_AFG = mWin.Window_Operation(RotationT);
						
						// Split out the proportion
						Prop_Ag = Proportion_AFG[0];
						Prop_Grass = Proportion_AFG[2];
		
						// Bird Habitat
						// Lambda
						float Lambda = -4.47f + (2.95f * Prop_Ag) + (5.17f * Prop_Grass); 
						// Habitat Index
						Habitat_Index = (float)((1 / ( 1 / Math.exp(Lambda) + 1 ) ) / 0.67f);
						// Write Habitat Index to The File
						sb_HI.append(String.format("%.4f", Habitat_Index));
					}
					
					if (x != width - 1) 
					{
						sb_HI.append(" ");
					}
				}
				
				out_HI.println(sb_HI.toString());
			}
			// Close output files
			out_HI.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
	}	
	
}
