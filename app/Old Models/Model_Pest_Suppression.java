package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses landscape proportion and land cover type to calculate pest suppression index (Grass) 
// using 990m rectangle buffers 
// Pest suppression index can vary from 0 to 1
// This model is from unpublished work by Tim Meehan et al., (2012, PLOS one) from University of Wisconsin
// Inputs are proportion of land cover particularly grass, selected cells in the raster map and crop rotation layer 
// Output map of pest suppression index
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Pest_Suppression
{

	//--------------------------------------------------------------------------
	public void Pest_Suppression(Selection selection, String Output_Folder, int[][] RotationT)
	{
		// This particular block will be removed once we can recycle moving window results
		 
		// Define Variables
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		int Buffer = 990; // In Meter
		int Window_Size = Buffer / 30; // Number of Cells in Raster Map
		
		//float Prop_Forest = 0;
		//int Count_Forest = 0;
		//int Forest_Mask = 1024; // 11
		float Prop_Grass = 0;
		int Count_Grass = 0;
		int Grass_Mask = 128 + 256; // 8 and 9
		int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		float Pest = 0;
		
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
		
		try {
			
			// Open a ASCII file to write the output 
			PrintWriter out_P = new HeaderWrite("Pest_Suppression", width, height, Output_Folder).getWriter();

			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{			
				// Outputs
				StringBuffer sb_P = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{				
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data Value
						sb_P.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						// Filter out all land cover except grass and ag
						if ((RotationT[y][x] & Grass_Mask) > 0 || (RotationT[y][x] & Ag_Mask) > 0)
						{
							
							// Calculate the Boundary for Moving Window
							Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
							float[] Proportion_AFG = mWin.Window_Operation(RotationT);
							
							// Split out the proportion
							//Prop_Forest = Proportion_AFG[1];
							Prop_Grass = Proportion_AFG[2];
				
							// Crop type is zero for Ag
							int Crop_Type = 0;
							// Crop type is 1 for grass
							if ((RotationT[y][x] & Grass_Mask) > 0)
							{
								Crop_Type = 1;
							}
							
							// Pest suppression calculation
							Pest = (float)(0.25 + (0.19f * Crop_Type) + (0.62f * Prop_Grass));
	
							// Write Pest to The File
							sb_P.append(String.format("%.4f", Pest));
						}
						else 
						{
							sb_P.append(stringNoData);
						}
						
					}
					if (x != width - 1) 
					{
						sb_P.append(" ");
					}
				}
				
				out_P.println(sb_P.toString());
			}

			// Close output files
			out_P.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
	}	
	
}
