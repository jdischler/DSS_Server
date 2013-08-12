package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses crop rotation layers to assess the impact of crop rotation on other things
//
//------------------------------------------------------------------------------
public class Model_Net_Income
{
		
	//static float Net_Income;
	//static float Net_Income_C;
	//static float Net_Income_G;
	//static float Net_Income_T;
	
	//--------------------------------------------------------------------------
	public void Net_Income(float[] Corn_P, float[] Grass_P, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		float Net_Income = 0;
		//float Net_Income_T = 0;
		//int Forest_Mask = 1024; // 11
		int Grass_Mask = 128 + 256; // 8 and 9
		int Corn_Mask = 1; // 1
		//int Bin = 10;
		//int Value_NI = 0;
		//int[] CountBin_NI = new int [Bin];
		
		// Ton/ha
		//float Min_Corn_Y = 3.08f - 0.11f * 70;
		//float Max_Corn_Y = 3.08f + 0.02f * 210 + 0.10f * 75 + 0.04f * 200;
		// Tons per pixel
		//float Min_Corn_P = 0.0001f * 900 * Min_Corn_Y;
		//float Max_Corn_P = 0.0001f * 900 * Max_Corn_Y;
		
		// Ton/ha
		//float Min_Grass_Y = 2.20f - 0.07f * 70;
		//float Max_Grass_Y = 2.20f + 0.02f * 210 + 0.07f * 75 + 0.03f * 200;
		// Tons per pixel
		//float Min_Grass_P = 0.0001f * 900 * Min_Grass_Y;
		//float Max_Grass_P = 0.0001f * 900 * Max_Grass_Y;
		
		// Net Income
		float Return;
		// $ per pixel cost for Corn
		float PC_Cost = 1124 * 0.0001f * 900;
		// $ per pixel cost for Grass
		float PG_Cost = 412  * 0.0001f * 900;
		// Price per pixel
		float P_Per_Corn = 300;
		float P_Per_Stover = 100;
		float P_Per_Grass = 100;
		// Calculation
		//float Min_NI_C = P_Per_Corn * 0.5f * Min_Corn_P + P_Per_Stover * 0.25f * Min_Corn_P - PC_Cost;
		//float Max_NI_C = P_Per_Corn * 0.5f * Max_Corn_P + P_Per_Stover * 0.25f * Max_Corn_P - PC_Cost;
		//Logger.info("Max_NI_C:" + Float.toString(Max_NI_C));
		//float Min_NI_G = P_Per_Grass * Min_Grass_P - PG_Cost;
		//float Max_NI_G = P_Per_Grass * Max_Grass_P - PG_Cost;
		// Net Income
		//float NI_Min = 0;
		//float NI_Max = 0;
		
		// Min in Net Income
		//if (Min_NI_C <= Min_NI_G)
		//{
		//	NI_Min = Min_NI_C;
		//}
		//else 
		//{
		//	NI_Min = Min_NI_G;
		//}
		// Max in Net Income
		//if (Max_NI_C <= Max_NI_G)
		//{
		//	NI_Max = Max_NI_G;
		//}
		//else 
		//{
		//	NI_Max = Max_NI_C;
		//}
		
		// Rotation
		int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		if (Rotation == null)
		{
			Logger.info("Fail Rotation");
			layer = new Layer_Raw("Rotation"); layer.init();
			Rotation = Layer_Base.getLayer("Rotation").getIntData();
		}
			layer = Layer_Base.getLayer("Rotation");
			width = layer.getWidth();
			height = layer.getHeight();
		
		try 
		{
			// Net Energy
			PrintWriter out_NI = new HeaderWrite("Net_Income", width, height, Output_Folder).getWriter();
			// Cron Ethanol
			//PrintWriter out_EC = HeaderWrite("Corn_Ethanol", width, height, Output_Folder);
			// Grass Ethanol
			//PrintWriter out_EG = HeaderWrite("Grass_Ethanol", width, height, Output_Folder);
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				// Outputs
				StringBuffer sb_NI = new StringBuffer();
				//StringBuffer sb_EC = new StringBuffer();
				//StringBuffer sb_EG = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_NI.append(stringNoData);
						//sb_EC.append(stringNoData);
						//sb_EG.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						Net_Income = 0;
						//Value_NI = 0;
						
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Tonnes per Ha
							Return = P_Per_Corn * 0.5f * Corn_P[i] + P_Per_Stover * 0.25f * Corn_P[i];
							Net_Income = Return - PC_Cost;
							//Ethanol_C = Corn_P[i] * 0.5f * 0.4f * 1000 + Corn_P * 0.25f * 0.38f * 1000;
							//Value_NI = (int)((Net_Income - NI_Min)/(NI_Max - NI_Min) * (Bin - 1));
							//CountBin_NI[Value_NI]++;
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							// Tonnes per pixel
							Return = P_Per_Grass * Grass_P[i];
							Net_Income = Return  - PC_Cost;
							//Ethanol_G = Grass_P[i] * 0.38f * 1000;
							//Value_NI = (int)((Net_Income - NI_Min)/(NI_Max - NI_Min) * (Bin - 1));
							//CountBin_NI[Value_NI]++;
						}
						
						sb_NI.append(String.format("%.4f", Net_Income));
						//sb_EC.append(Ethanol_C.toString());
						//sb_EG.append(Ethanol_G.toString());
						
						i = i + 1;
					}
					
					// Net Income
					//Net_Income_T += Net_Income;
					//if (Value_NI < 0 || Value_NI >= Bin)
					//{
					//	Logger.info("Out of range NI:" + Float.toString(Net_Income) + " " + Integer.toString(Value_NI));
					//}
						
					if (x != width - 1) 
					{
						sb_NI.append(" ");
						//sb_EC.append(" ");
						//sb_EG.append(" ");
					}
				}
				out_NI.println(sb_NI.toString());
				//out_EC.println(sb_EC.toString());
				//out_EG.println(sb_EG.toString());
			}
			// Close input files
			//br1.close();
			//br2.close();
			//br3.close();
			//br4.close();
			// Close output files
			out_NI.close();
			//out_EC.close();
			//out_EG.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
		
		// Data to return to the client		
		//ObjectNode obj = JsonNodeFactory.instance.objectNode();
		//ObjectNode N_I = JsonNodeFactory.instance.objectNode();
		
		//Total_Cells = 0;
		// Net_Income
		//ArrayNode NI = JsonNodeFactory.instance.arrayNode();
		//for (i = 0; i < CountBin_NI.length; i++) 
		//{
			//Total_Cells = CountBin_NI[i] + Total_Cells;
		//	NI.add(CountBin_NI[i]);
		//}
		// Average of Net_Income per pixel
		//float Net_Income_Per_Cell = Net_Income_T / Total_Cells;
		
		// Net_Income
		//N_I.put("Result", NI);
		//N_I.put("Min", String.format("%.4f", NI_Min));
		//N_I.put("Max", String.format("%.4f", NI_Max));
		//N_I.put("Net_Income", String.format("%.4f", Net_Income_Per_Cell));
		
		// Add branches to JSON Node 
		//obj.put("Net_Income", N_I);
		
		//Logger.info(N_I.toString());
		//return N_I;
	}
	
}
