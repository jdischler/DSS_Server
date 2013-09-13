package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses corn, soy, grass and alfalfa production to calculate Net Income 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Outputs are ASCII map of Net Income
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Net_Income
{
	
	//--------------------------------------------------------------------------
	//public void Net_Income(float[] Corn_Y, float[] Grass_Y, float[] Soy_Y, float[] Alfalfa_Y, Selection selection, String Output_Folder, int[][] RotationT)
	public void Net_Income(float[] YI, Selection selection, String Output_Folder, int[][] RotationT)
	{
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		float Net_Income = 0;
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Alfalfa_Mask = 128; // 8	
		
		// Gross return
		float Return;
		
		// $ per hec cost for Corn
		float PC_Cost = 1135;
		// $ per hec cost for Corn Stover
		float PCS_Cost = 412;
		// $ per hec cost for Grass
		float PG_Cost = 412;
		// $ per hec cost for Soy
		float PS_Cost = 627;
		// $ per hec cost for Alfalfa
		float PA_Cost = 620;
		
		// Price per tonne
		float P_Per_Corn = 274;
		float P_Per_Stover = 70;
		float P_Per_Grass = 107;
		float P_Per_Soy = 249;
		float P_Per_Alfalfa = 230;
		
		// Proportion of Stover 
		float Prop_Stover_Harvest = 0.38f;
		
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
			// Net Energy
			PrintWriter out_NI = new HeaderWrite("Net_Income", width, height, Output_Folder).getWriter();
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				// Outputs
				StringBuffer sb_NI = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_NI.append(stringNoData);
					}
					else
					//else if (selection.mSelection[y][x] == 1)
					{
						// Corn
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Gross return $ per hec
							Return = P_Per_Corn * 0.5f * YI[i] + P_Per_Stover * Prop_Stover_Harvest * 0.5f * YI[i];
							// Net Income $ per hec
							Net_Income = Return - PC_Cost - PCS_Cost;
							sb_NI.append(String.format("%.4f", Net_Income));
						}
						// Grass
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							// Gross return $ per pixel
							Return = P_Per_Grass * YI[i];
							// Net Income $ per pixel
							Net_Income = Return  - PG_Cost;
							sb_NI.append(String.format("%.4f", Net_Income));
						}
						// Soy
						else if ((RotationT[y][x] & Soy_Mask) > 0)
						{
							// Soy return $ per pixel
							Return = P_Per_Soy * YI[i];
							// Net Income $ per pixel
							Net_Income = Return  - PS_Cost;
							sb_NI.append(String.format("%.4f", Net_Income));
						}
						// Alfalfa
						else if ((RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							// Alfalfa return $ per pixel
							Return = P_Per_Alfalfa * YI[i];
							// Net Income $ per pixel
							Net_Income = Return  - PA_Cost;
							sb_NI.append(String.format("%.4f", Net_Income));
						}
						else 
						{
							sb_NI.append(stringNoData);
						}
						
						i = i + 1;
					}
						
					if (x != width - 1) 
					{
						sb_NI.append(" ");
					}
				}
				out_NI.println(sb_NI.toString());
			}
			// Close output files
			out_NI.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
	}
	
}
