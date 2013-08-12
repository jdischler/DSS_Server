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
public class Model_Habitat_Index
{
		
	//static int Window_Size;
	//static int mWidth, mHeight;
	//static float Habitat_Index_T;
	
	//--------------------------------------------------------------------------
	public void Habitat_Index(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		float Habitat_Index = 0;
		//float Habitat_Index_T = 0;
		//int Value_H;
		//int Bin = 10;
		//int[] CountBin_H = new int [Bin];
		// Range of HI
		//float Min_H = 0;
		//float Max_H = 1;
		
								
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
		try 
		{
			// Bird Index
			PrintWriter out_HI = new HeaderWrite("Habitat_Index", width, height, Output_Folder).getWriter();
			//PrintWriter out4 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Bird_Index.asc")));

			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				
				// Outputs
				StringBuffer sb_HI = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{				
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data Value
						sb_HI.append(stringNoData);
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
		
						// Bird Habitat
						// Lambda
						float Lambda = -4.47f + 2.95f * Prop_Ag + 5.17f * Prop_Forest; 
						// Habitat Index
						Habitat_Index = (float)((1 / ( 1 / Math.exp(Lambda) + 1 ) ) / 0.67f);
						//Habitat_Index_T = Habitat_Index + Habitat_Index_T;
						//Value_H = (int)((Habitat_Index - Min_H)/(Max_H - Min_H)*(Bin-1));
						//Value_H = 0;
						//Value_H = (int)(Habitat_Index * (Bin - 1));
						//if (Value_H < 0 || Value_H >= Bin)
						//{
						//	Logger.info("Out of range HI:" + Float.toString(Habitat_Index) + " " + Integer.toString(Value_H));
						//}
						//CountBin_H[Value_H]++;
						// Summary of Habitat Index
						// Write Habitat Index to The File
						sb_HI.append(String.format("%.4f", Habitat_Index));
					}
					
					if (x != width - 1) 
					{
						sb_HI.append(" ");
					}
				}
				
				out_HI.println(sb_HI.toString());
			}
			// Close input files
			//br1.close();
			//br2.close();
			//br3.close();
			//br4.close();
			// Close output files
			out_HI.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}

		// Data to return to the client		
		//ObjectNode obj = JsonNodeFactory.instance.objectNode();
		//ObjectNode H_I = JsonNodeFactory.instance.objectNode();

		// Habitat Index
		//ArrayNode HI = JsonNodeFactory.instance.arrayNode();
		//for (int i = 0; i < CountBin_H.length; i++) 
		//{
			//Total_Cells = CountBin_H[i] + Total_Cells;
		//	HI.add(CountBin_H[i]);
		//}
		// Average of Habitat_Index per pixel
		//float HI_Per_Cell = Habitat_Index_T / Total_Cells;
		
		// Habitat_Index
		//H_I.put("Result", HI);
		//H_I.put("Min", String.format("%.4f", Min_H));
		//H_I.put("Max", String.format("%.4f", Max_H));
		//H_I.put("Average_HI", String.format("%.4f", HI_Per_Cell));
		
		// Add branches to JSON Node 
		//obj.put("Habitat_Index", H_I);

		//Logger.info(H_I.toString());
		//return H_I;
	}	
	
}
