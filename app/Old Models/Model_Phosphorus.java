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
public class Model_Phosphorus
{
	//static int Window_Size;
	//static int mWidth, mHeight;

	//--------------------------------------------------------------------------
	public void Phosphorus(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		float Phosphorus = 0;
		//float Phosphorus_T = 0;
		//int Bin = 10;
		//int[] CountBin_P = new int [Bin];
		//int Value_P;
		
		int Buffer = 390; // In Meter
		int Window_Size = Buffer / 30; // Number of Cells in Raster Map
		float Prop_Ag = 0;
		int Count_Ag = 0;
		int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		float Prop_Forest = 0;
		int Count_Forest = 0;
		int Forest_Mask = 1024; // 11
		float Prop_Grass = 0;
		int Count_Grass = 0;
		int Grass_Mask = 128 + 256; // 8 and 9
		int Corn_Mask = 1; // 1
						
		// Phosphrous
		//float Min_P = (float)Math.pow(10, 0.79f * 0 - 1.44f);
		//float Max_P = (float)Math.pow(10, 0.79f * 1 - 1.44f);

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

		//Logger.info("About to output the model outcomes");
		try {
			//Logger.info("Opening the files to write");
			
			// Phosphorus
			PrintWriter out_P = new HeaderWrite("Phosphorus", width, height, Output_Folder).getWriter();
			//PrintWriter out6 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Phosphorus.asc")));

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
						
						Prop_Ag = 0;
						//Count_Ag = 0;
						Prop_Forest = 0;
						//Count_Forest = 0;
						Prop_Grass = 0;
						//Count_Grass = 0;
						
						// Calculate the Boundary for Moving Window
						Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
						float[] Proportion_AFG = mWin.Window_Operation(RotationT);
						
						//if (Prop_Ag < 0 || Prop_Ag > 1 || Prop_Forest < 0 || Prop_Forest > 1 || Prop_Grass < 0 || Prop_Grass > 1)
						//{
						//	Logger.info("Out of range:" + Float.toString(Prop_Ag) + " " + Float.toString(Prop_Forest) + " " + Float.toString(Prop_Grass));
						//}
						
						Prop_Ag = Proportion_AFG[0];
						Prop_Forest = Proportion_AFG[1];
						Prop_Grass = Proportion_AFG[2];

						// Phosphorus 
						//float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f);
						Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f);
						//float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f) / Max_P;
						//Phosphorus_T = Phosphorus + Phosphorus_T;
						// Summary of Phosphorus
						//Value_P = (int)((Phosphorus - Min_P)/(Max_P - Min_P)*(Bin-1));
						//Value_P = 0;
						//Value_P = (int)((Phosphorus - Min_P)/(Max_P - Min_P) * (Bin - 1));
						//if (Value_P < 0 || Value_P >= Bin)
						//{
						//	Logger.info("Out of range Pho:" + Float.toString(Phosphorus) + " " + Integer.toString(Value_P));
						//}
						//CountBin_P[Value_P]++;
						// Write Phosphorus to The File
						sb_P.append(String.format("%.4f", Phosphorus));

					}
					if (x != width - 1) 
					{
						sb_P.append(" ");
					}
				}
				out_P.println(sb_P.toString());
			}
			//Logger.info("Count C and G:" + Integer.toString(Count_C) + " " + Integer.toString(Count_G));
			//Logger.info("Closing the Files");
			// Close input files
			//br1.close();
			//br2.close();
			//br3.close();
			//br4.close();
			// Close output files
			out_P.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}

		// Data to return to the client		
		//ObjectNode obj = JsonNodeFactory.instance.objectNode();
		//ObjectNode Phos = JsonNodeFactory.instance.objectNode();

		// Phosphorus
		//ArrayNode P = JsonNodeFactory.instance.arrayNode();
		//for (int i = 0; i < CountBin_P.length; i++) 
		//{
			//Total_Cells = CountBin_P[i] + Total_Cells;
		//	P.add(CountBin_P[i]);
		//}
		// Average of Phosphorus per pixel
		//float Phos_Per_Cell = Phosphorus_T / Total_Cells;

		// Phosphorus
		//Phos.put("Result", P);
		//Phos.put("Min", String.format("%.4f", Min_P));
		//Phos.put("Max", String.format("%.4f", Max_P));
		//Phos.put("Phosphorus", Phosphorus_T * 900 / 1000000);
		//Phos.put("Phosphorus", String.format("%.4f", Phos_Per_Cell));

		// Add branches to JSON Node 
		//obj.put("Phosphorus", Phos);

		//Logger.info(Phos.toString());
		//return Phos;
	}	
	
}
