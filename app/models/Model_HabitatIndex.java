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
public class Model_HabitatIndex
{
		
	static int Window_Size;
	static int mWidth, mHeight;
	//static float Habitat_Index_T;
	
	//--------------------------------------------------------------------------
	public JsonNode Habitat_Index(JsonNode requestBody, Selection selection, String Output_Folder, int[][] RotationT)
	{
		Logger.info("Computing Habitat Index");
		
//		selection = new Selection(selection.mWidth, selection.mHeight);
		
		Layer_Base layer;
		int width = selection.mWidth, height = selection.mHeight;
		int NO_DATA = -9999;
		int Total_Cells = width * height;//countSelectedPixels();
		float Habitat_Index = 0;
		float Habitat_Index_T = 0;
		int Value_H;
		int Bin = 10;
		int[] CountBin_H = new int [Bin];
		// Range of HI
		float Min_H = 0;
		float Max_H = 1;
		
								
		int Buffer = 390; // In Meter
		Window_Size = Buffer / 30; // Number of Cells in Raster Map
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
						
		//Logger.info("About to output the model outcomes");
		try 
		{
			// Bird Index
			PrintWriter out_HI = HeaderWrite("Bird_Index", width, height, Output_Folder);
			//PrintWriter out4 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Bird_Index.asc")));

			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				
				// Outputs
				StringBuffer sb_HI = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{				
					if (RotationT[y][x] == 0)// || selection.mSelection[y][x] == 0) 
					{
						sb_HI.append(stringNoData);
					}
					else// if (selection.mSelection[y][x] == 1)
					{
						Prop_Ag = 0;
						Count_Ag = 0;
						Prop_Forest = 0;
						Count_Forest = 0;
						Prop_Grass = 0;
						Count_Grass = 0;
						
						// Calculate the Boundary for Moving Window
						Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
						
						// I to Width and J to Height
						for (int j = mWin.ULY; j <= mWin.LRY; j++) 
						{
							for (int i = mWin.ULX; i <= mWin.LRX; i++) 
							{
								if (RotationT[j][i] != 0)
								{
									mWin.Total++;
									if ((RotationT[j][i] & Ag_Mask) > 0)
									{
										Count_Ag = Count_Ag + 1;	
									}
									else if ((RotationT[j][i] & Forest_Mask) > 0 )
									{
										Count_Forest = Count_Forest + 1;
									}
									else if ((RotationT[j][i] & Grass_Mask) > 0 )
									{
										Count_Grass = Count_Grass + 1;
									}
								}
							}
						}

						// Compute Ag, forest and grass proportion
						// Agriculture Proportion
						Prop_Ag = (float)Count_Ag / mWin.Total;
						// Forest Proportion
						Prop_Forest = (float)Count_Forest / mWin.Total;
						// Grass Proportion
						Prop_Grass = (float)Count_Grass / mWin.Total;
						
						// Bird Habitat
						// Lambda
						float Lambda = -4.47f + 2.95f * Prop_Ag + 5.17f * Prop_Forest; 
						// Habitat Index
						Habitat_Index = (float)((1 / ( 1 / Math.exp(Lambda) + 1 ) ) / 0.67f);
						// Write Habitat Index to The File
						sb_HI.append(String.format("%.2f", Habitat_Index));
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
		ObjectNode H_I = JsonNodeFactory.instance.objectNode();

		// Habitat Index
		ArrayNode HI = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_H.length; i++) 
		{
			//Total_Cells = CountBin_H[i] + Total_Cells;
			HI.add(CountBin_H[i]);
		}
		// Average of Habitat_Index per pixel
		float HI_Per_Cell = Habitat_Index_T / Total_Cells;
		
		// Habitat_Index
		H_I.put("Result", HI);
		H_I.put("Min", String.format("%.4f", Min_H));
		H_I.put("Max", String.format("%.4f", Max_H));
		H_I.put("Average_HI", String.format("%.4f", HI_Per_Cell));
		
		// Add branches to JSON Node 
		//obj.put("Habitat_Index", H_I);

		Logger.info(H_I.toString());
		return H_I;
	}	

	public class Moving_Window
	{
		public int ULX, ULY, LRX, LRY, Total;
		public Moving_Window(int x, int y, int wsz, int w, int h)
		{
			ULX = x - wsz/2;
			ULY = y - wsz/2;
			LRX = x + wsz/2;
			LRY = y + wsz/2;
			
			// Left
			if (ULX < 0)
			{
				ULX = 0;
			}
			// Up
			if (ULY < 0)
			{
				ULY = 0;
			}
			// Right
			if (LRX > w - 1)
			{
				LRX = w - 1;
			}
			// Low
			if (LRY > h - 1)
			{
				LRY = h - 1;
			}

			Total = 0;	
		}
	}
	
	// Write Header To The File
	public PrintWriter HeaderWrite(String name, int W, int H, String Output_Folder) 
	{
		PrintWriter out = null;
		
		try 
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/" + Output_Folder + "/" + name + ".asc")));
		} 
		catch (Exception e) 
		{
			Logger.info(e.toString());
		}
		
		out.println("ncols         " + Integer.toString(W));
		out.println("nrows         " + Integer.toString(H));
		out.println("xllcorner     -10062652.65061");
		out.println("yllcorner     5249032.6922889");
		out.println("cellsize      30");
		out.println("NODATA_value  -9999");
		
		return out;
	}
	
}
