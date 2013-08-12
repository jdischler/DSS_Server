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
public class Model_Pest_Suppression
{
	//static int Window_Size;
	//static int mWidth, mHeight;

	//--------------------------------------------------------------------------
	public void Pest_Suppression(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		float Total_Cells = selection.countSelectedPixels();
		float Pest = 0;
		//float Pest_T = 0;
		//int Bin = 10;
		//int Value_Pest;
		//int[] CountBin_Pest = new int [Bin];
		
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
						
		// Pest_Suppression
		//float Min_Pest = (float)(0.25 + 0.19f * 0 + 0.62f * 0);
		//float Max_Pest = (float)(0.25 + 0.19f * 1 + 0.62f * 1);
		

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
			
			// Pest_Suppression
			PrintWriter out_P = new HeaderWrite("Pest_Suppression", width, height, Output_Folder).getWriter();
			//PrintWriter out7 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Pest.asc")));

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

						// Pest_Suppression
						int Crop_Type = 0;
						if ((RotationT[y][x] & Ag_Mask) > 0)
						{
							Crop_Type = 0;	
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Crop_Type = 1;
						}
						// Maximum is not really 1
						//Max_Pest = (float)(0.25 + 0.19f * 1 + 0.62f * 1);
						//Min_Pest = (float)(0.25 + 0.19f * 0 + 0.62f * 0);
						// Normalize using Max
						Pest = (float)(0.25 + 0.19f * Crop_Type + 0.62f * Prop_Forest);
						//float Pest = (float)(0.25 + 0.19f * Crop_Type + 0.62f * Prop_Forest) / Max_Pest;
						//Pest_T = Pest + Pest_T;
						// Summary of Pest
						//Value_Pest = 0;
						//Value_Pest = (int)((Pest - Min_Pest)/(Max_Pest - Min_Pest)* (Bin - 1));
						//if (Value_Pest < 0 || Value_Pest >= Bin)
						//{
						//	Logger.info("Out of range Pest:" + Float.toString(Pest) + " " + Integer.toString(Value_Pest));
						//}
						//CountBin_Pest[Value_Pest]++;
						// Write Pest to The File
						sb_P.append(String.format("%.4f", Pest));
						
					}
					if (x != width - 1) 
					{
						sb_P.append(" ");
					}
				}
				
				out_P.println(sb_P.toString());
			}

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
		//ObjectNode PestObj = JsonNodeFactory.instance.objectNode();

		// Pest
		//ArrayNode PestS = JsonNodeFactory.instance.arrayNode();
		//for (int i = 0; i < CountBin_Pest.length; i++) {
			//Total_Cells = CountBin_Pest[i] + Total_Cells;
		//	PestS.add(CountBin_Pest[i]);
		//}
		// Average of Pest per pixel
		//float Pest_Per_Cell = Pest_T / Total_Cells;
		
		// Pest
		//PestObj.put("Result", PestS);
		//PestObj.put("Min", String.format("%.4f", Min_Pest));
		//PestObj.put("Max", String.format("%.4f", Max_Pest));
		//PestObj.put("Pest", String.format("%.4f", Pest_Per_Cell));

		// Add branches to JSON Node 
		//obj.put("Pest", Pest);

		//Logger.info(PestObj.toString());
		//return PestObj;
	}	
	
}
