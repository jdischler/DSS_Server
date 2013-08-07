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
	static int Window_Size;
	static int mWidth, mHeight;

	//--------------------------------------------------------------------------
	public JsonNode Nitrogen(JsonNode requestBody, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		float Nitrogen = 0;
		float Nitrogen_T = 0;
		int Bin = 10;
		int[] CountBin_N = new int [Bin];
		int Value_N;
		
		// Nitrogen
		float Min_N = (float)Math.pow(10, 1.13f * 0 - 0.23f);
		float Max_N = (float)Math.pow(10, 1.13f * 1 - 0.23f);
		
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
			PrintWriter out_N = HeaderWrite("Nitrogen", width, height, Output_Folder);
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
						Count_Ag = 0;
						Prop_Forest = 0;
						Count_Forest = 0;
						Prop_Grass = 0;
						Count_Grass = 0;

						// Calculate the Boundary for Moving Window
						Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
						
						// I to Width and J to Height
						for (int j = mWin.ULY; j <= mWin.LRY; j++) {
							for (int i = mWin.ULX; i <= mWin.LRX; i++) {
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
						if (Prop_Ag < 0 || Prop_Ag > 1 || Prop_Forest < 0 || Prop_Forest > 1 || Prop_Grass < 0 || Prop_Grass > 1)
						{
							Logger.info("Out of range: " + Float.toString(Prop_Ag) + " " + Float.toString(Prop_Forest) + " " + Float.toString(Prop_Grass));
						}

						// Nitrogen
						//float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						//float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f) / Max_N;
						Nitrogen_T = Nitrogen + Nitrogen_T;
						// Summary of Nitrogen
						//Value_N = (int)((Nitrogen - Min_N)/(Max_N - Min_N)*(Bin-1));
						Value_N = 0;
						Value_N = (int)((Nitrogen - Min_N) / (Max_N - Min_N) * (Bin - 1));
						if (Value_N < 0 || Value_N >= Bin)
						{
							Logger.info("Out of range N:" + Float.toString(Nitrogen) + " " + Integer.toString(Value_N));
						}
						CountBin_N[Value_N]++;
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
		ObjectNode Nitr = JsonNodeFactory.instance.objectNode();

		// Nitrogen
		ArrayNode N = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_N.length; i++) 
		{
			//Total_Cells = CountBin_N[i] + Total_Cells;
			N.add(CountBin_N[i]);
		}
		// Average of Nitrogen per pixel
		float N_Per_Cell = Nitrogen_T / Total_Cells;
		
		// Nitrogen
		Nitr.put("Result", N);
		Nitr.put("Min", String.format("%.2f", Min_N));
		Nitr.put("Max", String.format("%.2f", Max_N));
		//Nitr.put("Nitrogen", Nitrogen_T * 900 / 1000000);
		Nitr.put("Nitrogen", String.format("%.2f", N_Per_Cell));

		// Add branches to JSON Node 
		//obj.put("Nitrogen", Nitr);

		Logger.info(Nitr.toString());
		return Nitr;
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
