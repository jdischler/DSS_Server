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
	public float[] Habitat_Index(Selection selection, int[][] RotationT)
	//public void Habitat_Index(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		//float Habitat_Index = 0;
							
		int Buffer = 390; // In Meter
		int Window_Size = Buffer / 30; // Number of Cells in Raster Map
		float Prop_Ag = 0;
		int Count_Ag = 0;
		int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		float Prop_Grass = 0;
		int Count_Grass = 0;
		int Grass_Mask = 128 + 256; // 8 and 9
		int Corn_Mask = 1; // 1
		int i = 0;
		
		// Net Energy
		float[] Habitat_Index = new float[Total_Cells];
		
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();

		if (true) { // use new z-win
			Moving_Z_Window zWin = new Moving_Z_Window(Window_Size, RotationT, width, height);
			
			boolean moreCells = true;
			while (moreCells) {
				Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();
				
				// calculate an index where to store this info since the pattern through the 
				//	array is actually the z-win zigzag path!
				int idx = point.mY * width + point.mX;
				
				Prop_Ag = zWin.getProportionAg();
				Prop_Grass = zWin.getProportionGrass();
				
				// Habitat Index
				float Lambda = -4.47f + (2.95f * Prop_Ag) + (5.17f * Prop_Grass); 
				Habitat_Index[idx] = (float)((1.0f / (1.0f / Math.exp(Lambda) + 1.0f )) / 0.67f);

				moreCells = zWin.advance();
			}		
		}
		if (false) {// OLD window		
			try 
			{
				for (int y = 0; y < height; y++) 
				{
					for (int x = 0; x < width; x++) 
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
						
						i = i + 1;
					}
				}
			}
			catch(Exception err) 
			{
				Logger.info(err.toString());
				Logger.info("Oops, something went wrong with writing to the files!");
			}
		}
			
		Logger.info("Model_Habitat_Index is finished");
				
		return Habitat_Index;
	}	
	
}
