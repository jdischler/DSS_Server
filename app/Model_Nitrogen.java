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
public class Model_Nitrogen
{
	//static int Window_Size;
	//static int mWidth, mHeight;

	//--------------------------------------------------------------------------
	public void Nitrogen(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		float Nitrogen = 0;
		//float Nitrogen_T = 0;
		//int Bin = 10;
		//int[] CountBin_N = new int [Bin];
		//int Value_N;
		
		// Nitrogen
		//float Min_N = (float)Math.pow(10, 1.13f * 0 - 0.23f);
		//float Max_N = (float)Math.pow(10, 1.13f * 1 - 0.23f);
		
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
			
			// Nitrogen
			PrintWriter out_N = new HeaderWrite("Nitrogen", width, height, Output_Folder).getWriter();
			//PrintWriter out5 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Nitrogen.asc")));
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{			
				// Outputs
				StringBuffer sb_N = new StringBuffer();
				
				for (int x = 0; x < width; x++) {				
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data Value
						sb_N.append(stringNoData);
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

						// Nitrogen
						//float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						//float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f) / Max_N;
						//Nitrogen_T = Nitrogen + Nitrogen_T;
						// Summary of Nitrogen
						//Value_N = (int)((Nitrogen - Min_N)/(Max_N - Min_N)*(Bin-1));
						//Value_N = 0;
						//Value_N = (int)((Nitrogen - Min_N) / (Max_N - Min_N) * (Bin - 1));
						//if (Value_N < 0 || Value_N >= Bin)
						//{
						//	Logger.info("Out of range N:" + Float.toString(Nitrogen) + " " + Integer.toString(Value_N));
						//}
						//CountBin_N[Value_N]++;
						// Write Nitrogen to The File
						sb_N.append(String.format("%.2f", Nitrogen));

					}
					if (x != width - 1) 
					{
						sb_N.append(" ");
					}
				}
				out_N.println(sb_N.toString());
			}
			// Close input files
			//br1.close();
			//br2.close();
			//br3.close();
			//br4.close();
			// Close output files
			out_N.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}

		// Data to return to the client		
		//ObjectNode obj = JsonNodeFactory.instance.objectNode();
		//ObjectNode Nitr = JsonNodeFactory.instance.objectNode();

		// Nitrogen
		//ArrayNode N = JsonNodeFactory.instance.arrayNode();
		//for (int i = 0; i < CountBin_N.length; i++) 
		//{
			//Total_Cells = CountBin_N[i] + Total_Cells;
		//	N.add(CountBin_N[i]);
		//}
		// Average of Nitrogen per pixel
		//float N_Per_Cell = Nitrogen_T / Total_Cells;
		
		// Nitrogen
		//Nitr.put("Result", N);
		//Nitr.put("Min", String.format("%.2f", Min_N));
		//Nitr.put("Max", String.format("%.2f", Max_N));
		//Nitr.put("Nitrogen", Nitrogen_T * 900 / 1000000);
		//Nitr.put("Nitrogen", String.format("%.2f", N_Per_Cell));

		// Add branches to JSON Node 
		//obj.put("Nitrogen", Nitr);

		//Logger.info(Nitr.toString());
		//return Nitr;
	}	
	
}
