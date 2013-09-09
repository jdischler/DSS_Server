package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses corn, soy, grass and alfalfa production to calculate Net Energy 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Outputs are ASCII map of Net Energy
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Net_Energy
{
	
	//--------------------------------------------------------------------------
	public void Net_Energy(float[] YI, Selection selection, String Output_Folder, int[][] RotationT)
	//public void Net_Energy(float[] Corn_Y, float[] Grass_Y, float[] Soy_Y, float[] Alfalfa_Y, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		float Net_Energy = 0;
		float Net_Energy_C = 0;
		float Net_Energy_S = 0;
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Alfalfa_Mask = 128; // 8
		
		// Proportion of Stover 
		float Prop_Stover_Harvest = 0.38f;
		
		// Energy Input at Farm (MJ per Ha)
		float EI_CF = 18151f; // HILL
		float EI_CSF = 2121f; // HILL 1/4 fuel use for stover harvest
		float EI_GF = 7411f; // EBAMM
		float EI_SF = 6096f; // Hill
		float EI_AF = 9075f; // Corn Grain Hill * 1/2
		
		// Energy Input in Processing (MJ per L)
		float EI_CP = 13.99f; // HILL
		float EI_CSP = 1.71f; // EBAMM cellulosic
		float EI_GP = 1.71f; // EBAMM cellulosic
		float EI_SP = 10.39f; // Hill
		float EI_AP = 1.71f; // Corn Grain Hill * 1/2
		
		// Energy output (MJ per L)
		float EO_C = 21.26f + 4.31f; // HILL
		float EO_CS = 21.26f + 3.40f; // EBAMM cellulosic
		float EO_G = 21.26f + 3.40f; // EBAMM cellulosic
		float EO_S = 32.93f + 21.94f; // Hill
		float EO_A = 21.26f + 3.40f; // EBAMM cellulosic
		
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
			layer = new Layer_Raw("Rotation"); layer.init();
			RotationT = Layer_Base.getLayer("Rotation").getIntData();
		}
			layer = Layer_Base.getLayer("Rotation");
			width = layer.getWidth();
			height = layer.getHeight();
		
		try 
		{	
			// Net Energy
			PrintWriter out_NE = new HeaderWrite("Net_Energy", width, height, Output_Folder).getWriter();
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				// Outputs
				StringBuffer sb_NE = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_NE.append(stringNoData);
					}
					else
					//else if (selection.mSelection[y][x] == 1)
					{
						// Corn
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// MJ per Ha
							// Net_Energy
							Net_Energy_C = (YI[i] * 0.5f * CEO_C * EO_C) - (EI_CF + EI_CP * YI[i] * 0.5f * CEO_C);
							Net_Energy_S = (YI[i] * Prop_Stover_Harvest * 0.5f * CEO_CS * EO_CS) - (EI_CSF + EI_CSP * YI[i] * Prop_Stover_Harvest * 0.5f * CEO_CS);
							Net_Energy = Net_Energy_C + Net_Energy_S;
							sb_NE.append(String.format("%.4f", Net_Energy));
						}
						// Grass
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							// MJ per Ha
							Net_Energy = (YI[i] * CEO_G * EO_G) - (EI_GF + EI_GP * YI[i] * CEO_G);
							sb_NE.append(String.format("%.4f", Net_Energy));
						}
						// Soy
						else if ((RotationT[y][x] & Soy_Mask) > 0)
						{
							// MJ per Ha
							Net_Energy = (YI[i] * 0.40f * CEO_S * EO_S) - (EI_SF + EI_SP * YI[i] * CEO_S);
							sb_NE.append(String.format("%.4f", Net_Energy));
						}
						// Alfalfa
						else if ((RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							// MJ per Ha
							Net_Energy = (YI[i] * CEO_A * EO_A) - (EI_AF + EI_AP * YI[i] * CEO_A);
							sb_NE.append(String.format("%.4f", Net_Energy));
						}
						else 
						{
							sb_NE.append(stringNoData);
						}
						
						i = i + 1;
					}
					if (x != width - 1) 
					{
						sb_NE.append(" ");
					}
				}
				out_NE.println(sb_NE.toString());
			}
			// Close output files
			out_NE.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
	}
	
}
