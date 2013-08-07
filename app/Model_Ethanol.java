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
public class Model_Ethanol
{
		
	//static float Ethanol;
	//static float Ethanol_C;
	//static float Ethanol_G;
	//static float Ethanol_T;
	
	//--------------------------------------------------------------------------
	public JsonNode Ethanol(float[] Corn_P, float[] Grass_P, JsonNode requestBody, Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		//Logger.info("Total_Cells: " + Float.toString(Total_Cells));
		int Grass_Mask = 128 + 256; // 8 and 9
		int Corn_Mask = 1; // 1
		float Ethanol = 0;
		float Ethanol_T = 0;
		int Value_E;
		int Bin = 10;
		int[] CountBin_E = new int [Bin];
		
		// Ton/ha
		float Min_Corn_Y = 3.08f - 0.11f * 70;
		float Max_Corn_Y = 3.08f + 0.02f * 210 + 0.10f * 75 + 0.04f * 200;
		// Tons per pixel
		float Min_Corn_P = 0.0001f * 900 * Min_Corn_Y;
		float Max_Corn_P = 0.0001f * 900 * Max_Corn_Y;
		
		// Ton/ha
		float Min_Grass_Y = 2.20f - 0.07f * 70;
		float Max_Grass_Y = 2.20f + 0.02f * 210 + 0.07f * 75 + 0.03f * 200;
		// Tons per pixel
		float Min_Grass_P = 0.0001f * 900 * Min_Grass_Y;
		float Max_Grass_P = 0.0001f * 900 * Max_Grass_Y;
		
		// Lit per pixel
		float C_E_Min = Min_Corn_P * 0.5f * 0.4f * 1000 + Min_Corn_P * 0.25f * 0.38f * 1000;
		float C_E_Max = Max_Corn_P * 0.5f * 0.4f * 1000 + Max_Corn_P * 0.25f * 0.38f * 1000;
		float G_E_Min = Min_Grass_P * 0.38f * 1000;
		float G_E_Max = Max_Grass_P * 0.38f * 1000;
		float E_Min = 0;
		float E_Max = 0;
		
		// Min in E
		if (G_E_Min <= C_E_Min)
		{
			E_Min = G_E_Min;
		}
		else 
		{
			E_Min = C_E_Min;
		}
		// Max in E
		if (G_E_Max <= C_E_Max)
		{
			E_Max = C_E_Max;
		}
		else 
		{
			E_Max = G_E_Max;
		}
		
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
			// Ethanol
			PrintWriter out_E = HeaderWrite("Ethanol", width, height, Output_Folder);
			// Cron Ethanol
			//PrintWriter out_EC = HeaderWrite("Corn_Ethanol", width, height, Output_Folder);
			// Grass Ethanol
			//PrintWriter out_EG = HeaderWrite("Grass_Ethanol", width, height, Output_Folder);
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				
				// Outputs
				StringBuffer sb_E = new StringBuffer();
				//StringBuffer sb_EC = new StringBuffer();
				//StringBuffer sb_EG = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_E.append(stringNoData);
						//sb_EC.append(stringNoData);
						//sb_EG.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						Ethanol = 0;
						Value_E = 0;
						
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Tonnes per Ha
							Ethanol = Corn_P[i] * 0.5f * 0.4f * 1000 + Corn_P[i] * 0.25f * 0.38f * 1000;
							//Ethanol_T += Ethanol;
							//Ethanol_C = Corn_P[i] * 0.5f * 0.4f * 1000 + Corn_P * 0.25f * 0.38f * 1000;
							Value_E = (int)((Ethanol - E_Min)/(E_Max - E_Min) * (Bin - 1));
							CountBin_E[Value_E]++;

						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							// Tonnes per pixel
							Ethanol = Grass_P[i] * 0.38f * 1000;
							//Ethanol_T += Ethanol;
							//Ethanol_G = Grass_P[i] * 0.38f * 1000;
							Value_E = (int)((Ethanol - E_Min)/(E_Max - E_Min) * (Bin - 1));
							CountBin_E[Value_E]++;
						}
						
						Ethanol_T += Ethanol;
												
						if (Value_E < 0 || Value_E >= Bin)
						{
							Logger.info("Out of range E: " + Float.toString(Ethanol) + " " + Integer.toString(Value_E));
						}
						
						sb_E.append(String.format("%.4f", Ethanol));
						//sb_EC.append(Ethanol_C.toString());
						//sb_EG.append(Ethanol_G.toString());
						
						i = i + 1;
					}
					if (x != width - 1) 
					{
						sb_E.append(" ");
						//sb_EC.append(" ");
						//sb_EG.append(" ");
					}
				}
				out_E.println(sb_E.toString());
				//out_EC.println(sb_EC.toString());
				//out_EG.println(sb_EG.toString());
			}
			// Close input files
			//br1.close();
			//br2.close();
			//br3.close();
			//br4.close();
			// Close output files
			out_E.close();
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
		ObjectNode E_C_G = JsonNodeFactory.instance.objectNode();
		// Ethonal
		ArrayNode E = JsonNodeFactory.instance.arrayNode();
		for (i = 0; i < CountBin_E.length; i++) 
		{
			E.add(CountBin_E[i]);
		}
		// Average of Ethanol per pixel
		float E_Per_Cell = Ethanol_T / Total_Cells;
		
		// Ethonal
		E_C_G.put("Result", E);
		E_C_G.put("Min", String.format("%.4f", E_Min));
		E_C_G.put("Max", String.format("%.4f", E_Max));
		//E_C_G.put("Ethanol", Ethanol_T / 1000);
		E_C_G.put("Ethanol", String.format("%.4f", E_Per_Cell));
		
		// Add branches to JSON Node 
		//obj.put("Ethanol", E_C_G);

		Logger.info(E_C_G.toString());
		
		return E_C_G;
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
