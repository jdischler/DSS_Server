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
	static int Window_Size;	
	static int mWidth, mHeight;


	//--------------------------------------------------------------------------
	public JsonNode Pollinator(JsonNode requestBody, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Total_Cells = selection.countSelectedPixels();
		float Poll = 0;
		float Pollinator_T = 0;
		int Bin = 10;
		int[] CountBin_Poll = new int [Bin];
		int Value_Poll;
		
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
						
		// Pollinator
		float Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
		float Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);

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
			PrintWriter out_P = HeaderWrite("Pollinator", width, height, Output_Folder);
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
						if (Prop_Ag < 0 || Prop_Ag > 1 || Prop_Forest < 0 || Prop_Forest > 1 || Prop_Grass < 0 || Prop_Grass > 1)
						{
							Logger.info("Out of range:" + Float.toString(Prop_Ag) + " " + Float.toString(Prop_Forest) + " " + Float.toString(Prop_Grass));
						}

						// Pollinator
						//Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);
						//Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
						// Normalize using Max
						Poll = (float)Math.pow(0.6617f + 2.98 * Prop_Forest + 1.83 * Prop_Grass, 2);
						//float Poll = (float)Math.pow(0.6617f + 2.98 * Prop_Forest + 1.83 * Prop_Grass, 2) / Max_Poll;
						Pollinator_T = Poll + Pollinator_T;
						// Summary of Pollinator
						Value_Poll = 0;
						Value_Poll = (int)((Poll - Min_Poll)/(Max_Poll - Min_Poll)* (Bin - 1));
						if (Value_Poll < 0 || Value_Poll >= Bin)
						{
							Logger.info("Out of range Poll:" + Float.toString(Poll) + " " + Integer.toString(Value_Poll));
						}
						CountBin_Poll[Value_Poll]++;
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
		ObjectNode PollObj = JsonNodeFactory.instance.objectNode();

		//Total_Cells = 0;
		// Pollinator
		ArrayNode Pollin = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_Poll.length; i++) {
			//Total_Cells = CountBin_Poll[i] + Total_Cells;
			Pollin.add(CountBin_Poll[i]);
		}
		// Average of Pollinator per pixel
		float Pollinator_Per_Cell = Pollinator_T / Total_Cells;

		// Pollinator
		PollObj.put("Result", Pollin);
		PollObj.put("Min", String.format("%.4f", Min_Poll));
		PollObj.put("Max", String.format("%.4f", Max_Poll));
		PollObj.put("Pollinator", String.format("%.4f", Pollinator_Per_Cell));
		
		// Add branches to JSON Node 
		//obj.put("Pollinator", Poll);

		Logger.info(PollObj.toString());
		return PollObj;
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
