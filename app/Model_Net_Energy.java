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
public class Model_Net_Energy
{
		
	//static float Net_Energy;
	//static float Net_Energy_C;
	//static float Net_Energy_G;
	//static float Net_Energy_T;
	
	//--------------------------------------------------------------------------
	public void Net_Energy(float[] Corn_P, float[] Grass_P, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		float Net_Energy = 0;
		//float Net_Energy_T = 0;
		//int Forest_Mask = 1024; // 11
		int Grass_Mask = 128 + 256; // 8 and 9
		int Corn_Mask = 1; // 1
		//int Value_NE;	
		//int Bin = 10;
		//int[] CountBin_NE = new int [Bin];
		
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
		//float Min_NE_C = (Min_Corn_P * 0.5f * 0.4f * 1000 * 21.20f + Min_Corn_P * 0.25f * 0.38f * 1000 * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Max_Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Max_Corn_P * 0.25f * 0.38f * 1000);
		//float Max_NE_C = (Max_Corn_P * 0.5f * 0.4f * 1000 * 21.20f + Max_Corn_P * 0.25f * 0.38f * 1000 * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Min_Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Min_Corn_P * 0.25f * 0.38f * 1000);
		//float Min_NE_G = (Min_Grass_P * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Max_Grass_P * 0.38f * 1000);
		//float Max_NE_G = (Max_Grass_P * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Min_Grass_P * 0.38f * 1000);
		// Net Energy
		//float NE_Min = 0;
		//float NE_Max = 0;
		
		// Min in Net Income
		//if (Min_NE_C <= Min_NE_G)
		//{
		//	NE_Min = Min_NE_C;
		//}
		//else 
		//{
		//	NE_Min = Min_NE_G;
		//}
		// Max in Net Income
		//if (Max_NE_G <= Max_NE_C)
		//{
		//	NE_Max = Max_NE_C;
		//}
		//else 
		//{
		//	NE_Max = Max_NE_G;
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
		
		try {
			
			// Net Energy
			PrintWriter out_NE = new HeaderWrite("Net_Energy", width, height, Output_Folder).getWriter();
			// Cron Ethanol
			//PrintWriter out_EC = HeaderWrite("Corn_Ethanol", width, height, Output_Folder);
			// Grass Ethanol
			//PrintWriter out_EG = HeaderWrite("Grass_Ethanol", width, height, Output_Folder);
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				// Outputs
				StringBuffer sb_NE = new StringBuffer();
				//StringBuffer sb_EC = new StringBuffer();
				//StringBuffer sb_EG = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_NE.append(stringNoData);
						//sb_EC.append(stringNoData);
						//sb_EG.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						Net_Energy = 0;
						//Value_NE = 0;
						
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Tonnes per Ha
							Net_Energy = (Corn_P[i] * 0.5f * 0.4f * 1000 * 21.20f + Corn_P[i] * 0.25f * 0.38f * 1000 * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Corn_P[i] * 0.5f * 0.4f * 1000 + 1.71f * Corn_P[i] * 0.25f * 0.38f * 1000);
							//Ethanol_C = Corn_P[i] * 0.5f * 0.4f * 1000 + Corn_P * 0.25f * 0.38f * 1000;
							//Value_NE = (int)((Net_Energy - NE_Min)/(NE_Max - NE_Min) * (Bin - 1));
							//CountBin_NE[Value_NE]++;
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							// Tonnes per pixel
							Net_Energy = (Grass_P[i] * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Grass_P[i] * 0.38f * 1000);
							//Ethanol_G = Grass_P[i] * 0.38f * 1000;
							//Value_NE = (int)((Net_Energy - NE_Min)/(NE_Max - NE_Min) * (Bin - 1));
							//CountBin_NE[Value_NE]++;
						}
						
						// Net Energy
						//Net_Energy_T += Net_Energy;
						//if (Value_NE < 0 || Value_NE >= Bin)
						//{
						//	Logger.info("Out of range NE:" + Float.toString(Net_Energy) + " " + Integer.toString(Value_NE));
						//}
						
						sb_NE.append(String.format("%.4f", Net_Energy));
						//sb_EC.append(Ethanol_C.toString());
						//sb_EG.append(Ethanol_G.toString());
						
						i = i + 1;
					}
					if (x != width - 1) 
					{
						sb_NE.append(" ");
						//sb_EC.append(" ");
						//sb_EG.append(" ");
					}
				}
				out_NE.println(sb_NE.toString());
				//out_EC.println(sb_EC.toString());
				//out_EG.println(sb_EG.toString());
			}
			// Close input files
			//br1.close();
			//br2.close();
			//br3.close();
			//br4.close();
			// Close output files
			out_NE.close();
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
		//ObjectNode N_E = JsonNodeFactory.instance.objectNode();
		//Total_Cells = 0;
		// Net Energy
		//ArrayNode NE = JsonNodeFactory.instance.arrayNode();
		//for (i = 0; i < CountBin_NE.length; i++) 
		//{
			//Total_Cells = CountBin_NE[i] + Total_Cells;
		//	NE.add(CountBin_NE[i]);
		//}
		// Average of Net_Energy per pixel
		//float Net_Energy_Per_Cell = Net_Energy_T / Total_Cells;
		
		// Net_Energy
		//N_E.put("Result", NE);
		//N_E.put("Min", String.format("%.4f", NE_Min));
		//N_E.put("Max", String.format("%.4f", NE_Max));
		//N_E.put("Net_Energy", String.format("%.4f", Net_Energy_Per_Cell));
		
		// Add branches to JSON Node
		//obj.put("Net_Energy", N_E);
		//Logger.info(N_E.toString());
		//return N_E;
	}
	
}
