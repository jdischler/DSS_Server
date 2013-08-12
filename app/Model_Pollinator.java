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
public class Model_Pollinator
{
	//static int Window_Size;	
	//static int mWidth, mHeight;
	
	//--------------------------------------------------------------------------
	public void Pollinator(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		float Poll = 0;
		//float Pollinator_T = 0;
		//int Bin = 10;
		//int[] CountBin_Poll = new int [Bin];
		//int Value_Poll;
		
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
						
		// Pollinator
		//float Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
		//float Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);

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
			
			// Pollinator 
			PrintWriter out_P = new HeaderWrite("Pollinator", width, height, Output_Folder).getWriter();
			//PrintWriter out8 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Pollinator.asc")));

			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				
				StringBuffer sb_P = new StringBuffer();
				
				for (int x = 0; x < width; x++) {				
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

						// Pollinator
						//Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);
						//Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
						// Normalize using Max
						Poll = (float)Math.pow(0.6617f + 2.98 * Prop_Forest + 1.83 * Prop_Grass, 2);
						//float Poll = (float)Math.pow(0.6617f + 2.98 * Prop_Forest + 1.83 * Prop_Grass, 2) / Max_Poll;
						//Pollinator_T = Poll + Pollinator_T;
						// Summary of Pollinator
						//Value_Poll = 0;
						//Value_Poll = (int)((Poll - Min_Poll)/(Max_Poll - Min_Poll)* (Bin - 1));
						//if (Value_Poll < 0 || Value_Poll >= Bin)
						//{
						//	Logger.info("Out of range Poll:" + Float.toString(Poll) + " " + Integer.toString(Value_Poll));
						//}
						//CountBin_Poll[Value_Poll]++;
						// Write Pollinator to The File
						sb_P.append(String.format("%.4f", Poll));
					}
					if (x != width - 1) 
					{
						sb_P.append(" ");
					}
				}
				out_P.println(sb_P.toString());
			}
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
		//ObjectNode PollObj = JsonNodeFactory.instance.objectNode();

		//Total_Cells = 0;
		// Pollinator
		//ArrayNode Pollin = JsonNodeFactory.instance.arrayNode();
		//for (int i = 0; i < CountBin_Poll.length; i++) {
			//Total_Cells = CountBin_Poll[i] + Total_Cells;
		//	Pollin.add(CountBin_Poll[i]);
		//}
		// Average of Pollinator per pixel
		//float Pollinator_Per_Cell = Pollinator_T / Total_Cells;

		// Pollinator
		//PollObj.put("Result", Pollin);
		//PollObj.put("Min", String.format("%.4f", Min_Poll));
		//PollObj.put("Max", String.format("%.4f", Max_Poll));
		//PollObj.put("Pollinator", String.format("%.4f", Pollinator_Per_Cell));
		
		// Add branches to JSON Node 
		//obj.put("Pollinator", Poll);

		//Logger.info(PollObj.toString());
		//return PollObj;
	}	
	
}
