package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses landscape proportion to calculate pollinator visitation index (Grass and Forest) 
// using 1500m rectangle buffers 
// Visitation Index can vary from 0 to 18, after normalization it goes 0 to 1
// This model is from unpublished work by Ashley Bennett from Michigan State University
// Inputs are proportion of land cover particularly grass and forest, selected cells in the raster map and crop rotation layer 
// ASCII map of visitation index
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Pollinator_Pest_Suppression
{
	
	//--------------------------------------------------------------------------
	//public ThreeArrays Pollinator_Pest_Suppression(Selection selection, String Output_Folder, int[][] RotationT)
	public TwoArrays Pollinator_Pest_Suppression(Selection selection, int[][] RotationT)
	{
		// This particular block will be removed once we can recycle moving window results
		
		// Define Variables
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		int Buffer = 990; // In Meter
		int Window_Size = Buffer / 30; // Number of Cells in Raster Map
		
		// Initialize default variables
		//float Poll = 0;
		//float Pest = 0;
		float Prop_Forest = 0;
		int Count_Forest = 0;
		int Forest_Mask = 1024; // 11
		float Prop_Grass = 0;
		int Count_Grass = 0;
		int Grass_Mask = 128 + 256; // 8 and 9
		int i = 0;
		
		// Pollinator
		float[] Poll = new float[Total_Cells];
		// Pest Suppression
		float[] Pest = new float[Total_Cells];
		
		float Min_P =  1000000;
		float Max_P = -1000000;
		float Min_PS =  1000000;
		float Max_PS = -1000000;
		
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();

		long timingStart = System.currentTimeMillis();
		if (true) { // use new z-win
			Moving_Z_Window zWin = new Moving_Z_Window(Window_Size, RotationT, width, height);
			
			boolean moreCells = true;
			while (moreCells) {
				Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();
				
				// calculate an index where to store this info since the pattern through the 
				//	array is actually the z-win zigzag path!
				int idx = point.mY * width + point.mX;
				
				Prop_Forest = zWin.getProportionForest();
				Prop_Grass = zWin.getProportionGrass();
				
				// Calculate visitation index and normalize value by max
//				Poll[idx] = (float)Math.pow(0.6617f + (2.98f * Prop_Forest) + (1.83f * Prop_Grass), 2.0f) / 18.0f;
				Poll[idx] = (float)Math.pow(0.6617f + (2.98f * Prop_Forest) + (1.83f * Prop_Grass), 2.0f);
				//Min_P = Min(Min_P, Poll[idx]);
				//Max_P = Max(Max_P, Poll[idx]);
					
				// Crop type is zero for Ag, Crop type is 1 for grass
				int Crop_Type = 0;
				if ((RotationT[point.mY][point.mX] & Grass_Mask) > 0) {
					Crop_Type = 1;
				}
					
				// Pest suppression calculation
				Pest[idx] = (float)(0.25f + (0.19f * Crop_Type) + (0.62f * Prop_Grass));
				//Min_PS = Min(Min_PS, Pest[idx]);
				//Max_PS = Max(Max_PS, Pest[idx]);

				moreCells = zWin.advance();
			}		
		}
		long timingEnd = System.currentTimeMillis();
		float timeSec = (timingEnd - timingStart) / 1000.0f;
		Logger.info("ZWin timing: " + Float.toString(timeSec));
		
		if (false) { // Old style window
			timingStart = System.currentTimeMillis();
			try 
			{
				int errors = 0;
				i = 0;
				for (int y = 0; y < height; y++) 
				{
					for (int x = 0; x < width; x++) 
					{				
						// Calculate the Boundary for Moving Window
						Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
						float[] Proportion_AFG = mWin.Window_Operation(RotationT);
						
						// Split out the proportion
						Prop_Forest = Proportion_AFG[1];
						Prop_Grass = Proportion_AFG[2];

						// Calculate visitation index and normalize value by max
						//Poll[i] = (float)Math.pow(0.6617f + (2.98f * Prop_Forest) + (1.83f * Prop_Grass), 2.0f) / 18.0f;
						Poll[i] = (float)Math.pow(0.6617f + (2.98f * Prop_Forest) + (1.83f * Prop_Grass), 2.0f);
						//Min_P = Min(Min_P, Poll[i]);
						//Max_P = Max(Max_P, Poll[i]);
							
						// Crop type is zero for Ag
						int Crop_Type = 0;
						// Crop type is 1 for grass
						if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Crop_Type = 1;
						}
							
						// Pest suppression calculation
						Pest[i] = (float)(0.25f + (0.19f * Crop_Type) + (0.62f * Prop_Grass));
						//Min_PS = Min(Min_PS, Pest[i]);
						//Max_PS = Max(Max_PS, Pest[i]);
	
						i = i + 1;
					}
				}
				
			}
			catch(Exception err) 
			{
				Logger.info(err.toString());
				Logger.info("Oops, something went wrong with writing to the files!");
			}
			timingEnd = System.currentTimeMillis();
			timeSec = (timingEnd - timingStart) / 1000.0f;
			
			Logger.info("SlowWin timing: " + Float.toString(timeSec));
		}
		
				
		Logger.info("Model_Pollinator_Pest_Suppression is finished");
		
		return new TwoArrays(Poll, Pest, Min_P, Max_P, Min_PS, Max_PS);

	}	
	
	// Min
	// public float Min(float Min, float Num)
	// { 
		// // Min
		// if (Num < Min)
		// {
			// Min = Num;
		// }
		// 
		// return Min;
	// }
	
	// Max
	// public float Max(float Max, float Num)
	// {
// 
		// // Max
		// if (Num > Max)
		// {
			// Max = Num;
		// }
		// 
		// return Max;
	// }
}
