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
public class Models_Default
{
	
	//--------------------------------------------------------------------------
	public void Calculate(String Output_Folder)
	{
		
		Layer_Base layer;
		int NO_DATA = -9999;
		// Columns
		int width = 6150;
		// Rows
		int height = 4557;
			
		// Net Income
		// $ per pixel cost for Corn
		float PC_Cost = 1124 * 0.0001f * 900;
		// $ per pixel cost for Grass
		float PG_Cost = 412  * 0.0001f * 900;
		// Price per pixel
		float P_Per_Corn = 300;
		float P_Per_Stover = 100;
		float P_Per_Grass = 100;	
		
		try 
		{
			// Raad Input Files
			BufferedReader br1 = HeaderRead("slope-soil", width, height, "Inputs");
			BufferedReader br2 = HeaderRead("depth", width, height, "Inputs");
			BufferedReader br3 = HeaderRead("silt", width, height, "Inputs");
			BufferedReader br4 = HeaderRead("cec", width, height, "Inputs");
			BufferedReader br5 = HeaderRead("rotation", width, height, "Inputs");
		
			// Ethanol 
			PrintWriter out1 = HeaderWrite("Ethanol", width, height, Output_Folder);
			// Net Income
			PrintWriter out2 = HeaderWrite("Net_Income", width, height, Output_Folder);
			// Net Energy
			PrintWriter out3 = HeaderWrite("Net_Energy", width, height, Output_Folder);
			// Bird Index
			PrintWriter out4 = HeaderWrite("Bird_Index", width, height, Output_Folder);
			// Nitrogen
			PrintWriter out5 = HeaderWrite("Nitrogen", width, height, Output_Folder);
			// Phosphorus
			PrintWriter out6 = HeaderWrite("Phosphorus", width, height, Output_Folder);
			// Pest
			PrintWriter out7 = HeaderWrite("Pest_Suppression", width, height, Output_Folder);
			// Pollinator 
			PrintWriter out8 = HeaderWrite("Pollinator", width, height, Output_Folder);
			// Corn_Production 
			PrintWriter out9 = HeaderWrite("Corn_Production", width, height, Output_Folder);
			// Grass_Production 
			PrintWriter out10 = HeaderWrite("Grass_Production", width, height, Output_Folder);
			
			// Size of Window
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
			
			float Rotation[][] = new float[height][width];
			
			for (int y = 0; y < height; y++) 
			{
				// Inputs
				String line5 = br5.readLine();
				String rot[] = line5.split("\\s+");
				
				for (int x = 0; x < width; x++) 
				{
					Rotation[y][x] = rot[x];
				}
			}			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (y = 0; y < height; y++) 
			{
				// Inputs
				String line1 = br1.readLine();
				String slope[] = line1.split("\\s+");
				String line2 = br2.readLine();
				String soil[] = line2.split("\\s+");
				String line3 = br3.readLine();
				String silt[] = line3.split("\\s+");
				String line4 = br4.readLine();
				String cec[] = line4.split("\\s+");
			
				// Outputs
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				StringBuffer sb3 = new StringBuffer();
				StringBuffer sb4 = new StringBuffer();
				StringBuffer sb5 = new StringBuffer();
				StringBuffer sb6 = new StringBuffer();
				StringBuffer sb7 = new StringBuffer();
				StringBuffer sb8 = new StringBuffer();
				StringBuffer sb9 = new StringBuffer();
				StringBuffer sb10 = new StringBuffer();
				
				for (x = 0; x < width; x++) 
				{				
					//if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					if (Rotation[y][x] == -9999)
					{
						// Check for No-Data Value
						sb1.append(stringNoData);
						sb2.append(stringNoData);
						sb3.append(stringNoData);
						sb4.append(stringNoData);
						sb5.append(stringNoData);
						sb6.append(stringNoData);
						sb7.append(stringNoData);
						sb8.append(stringNoData);
						sb9.append(stringNoData);
						sb10.append(stringNoData);
					}
					//else if (selection.mSelection[y][x] == 1)
					else
					{
						// Ethanol Production
						float Corn_Y = 0;
						float Corn_P = 0;
						float Grass_Y = 0;
						float Grass_P = 0;
						float Ethanol = 0;
						float Return = 0;
						float Net_Income = 0;
						float Net_Energy = 0;
						
						if ((Rotation[y][x] & Corn_Mask) > 0)
						{
							// Tonnes per Ha
							Corn_Y = 3.08f - 0.11f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.10f * Float.parseFloat(silt[x]) + 0.04f * Float.parseFloat(cec[x]);
							Corn_P = 0.0001f * 900 * Corn_Y;
							// Ethonal Calculation
							// Tonnes per pixel
							Ethanol = Corn_P * 0.5f * 0.4f * 1000 + Corn_P * 0.25f * 0.38f * 1000;
							// Net_Income Calculation
							Return = P_Per_Corn * 0.5f * Corn_P + P_Per_Stover * 0.25f * Corn_P;
							//Net_Income = (GC_Return  - PC_Cost) / Max_NI_C;
							Net_Income = Return  - PC_Cost;
							// Net_Energy Calculation (Mega Jul)
							Net_Energy = (Corn_P * 0.5f * 0.4f * 1000 * 21.20f + Corn_P * 0.25f * 0.38f * 1000 * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Corn_P * 0.25f * 0.38f * 1000);
						}
						else if ((Rotation[y][x] & Grass_Mask) > 0)
						{
							Grass_Y = 2.20f - 0.07f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.07f * Float.parseFloat(silt[x]) + 0.03f * Float.parseFloat(cec[x]);
							Grass_P = 0.0001f * 900 * Grass_Y;
							// Ethonal Calculation
							// Tonnes per pixel
							Ethanol = Grass_P * 0.38f * 1000;
							// Net_Income Calculation
							Return = P_Per_Grass * Grass_P;
							//Net_Income = (GG_Return - PG_Cost) / Max_NI_G;
							Net_Income = Return - PG_Cost;
							// Net_Energy Calculation (Mega Jul)
							Net_Energy = (Grass_P * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Grass_P * 0.38f * 1000);
						}

						sb1.append(String.format("%.4f", Ethanol));
						sb2.append(String.format("%.4f", Net_Income));
						sb3.append(String.format("%.4f", Net_Energy));
						sb9.append(String.format("%.4f", Corn_P));
						sb10.append(String.format("%.4f", Grass_P));
						
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
								if (Rotation[j][i] != -9999)
								{
									mWin.Total++;
									if ((Rotation[j][i] & Ag_Mask) > 0)
									{
										Count_Ag = Count_Ag + 1;	
									}
									else if ((Rotation[j][i] & Forest_Mask) > 0 )
									{
										Count_Forest = Count_Forest + 1;
									}
									else if ((Rotation[j][i] & Grass_Mask) > 0 )
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
						float Habitat_Index = (float)((1 / ( 1 / Math.exp(Lambda) + 1 ) ) / 0.67f);
						// Write Habitat Index to The File
						sb4.append(String.format("%.4f", Habitat_Index));

						// Nitrogen
						float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						// Write Nitrogen to The File
						sb5.append(String.format("%.4f", Nitrogen));

						// Phosphorus 
						float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f);
						// Write Phosphorus to The File
						sb6.append(String.format("%.4f", Phosphorus));

						// Pest 
						int Crop_Type = 0;
						if ((RotationT[y][x] & Ag_Mask) > 0)
						{
							Crop_Type = 0;	
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Crop_Type = 1;
						}
						// Normalize using Max
						float Pest = (float)(0.25 + 0.19f * Crop_Type + 0.62f * Prop_Forest);
						// Write Pest to The File
						sb7.append(String.format("%.4f", Pest));

						// Pollinator
						float Poll = (float)Math.pow(0.6617f + 2.98 * Prop_Forest + 1.83 * Prop_Grass, 2);
						// Write Pollinator to The File
						sb8.append(String.format("%.4f", Poll));
					}
					if (x != width - 1) 
					{
						sb1.append(" ");
						sb2.append(" ");
						sb3.append(" ");
						sb4.append(" ");
						sb5.append(" ");
						sb6.append(" ");
						sb7.append(" ");
						sb8.append(" ");
						sb9.append(" ");
						sb10.append(" ");
					}
				}
				out1.println(sb1.toString());
				out2.println(sb2.toString());
				out3.println(sb3.toString());
				out4.println(sb4.toString());
				out5.println(sb5.toString());
				out6.println(sb6.toString());
				out7.println(sb5.toString());
				out8.println(sb6.toString());
				out9.println(sb5.toString());
				out10.println(sb6.toString());
			}
			// Close input files
			br1.close();
			br2.close();
			br3.close();
			br4.close();
			// Close output files
			out1.close();
			out2.close();
			out3.close();
			out4.close();
			out5.close();
			out6.close();
			out7.close();
			out8.close();
			out9.close();
			out10.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
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
	
	// Read The Header of The File
	public BufferedReader HeaderRead(String name, int W, int H, String Input_Folder) 
	{
		BufferedReader br = null;
		
		try 
		{
			br = new BufferedReader(new FileReader("./layerData/" + Input_Folder + "/" + name + ".asc"));
			String line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
		} 
		catch (Exception e) 
		{
			Logger.info(e.toString());
		}
		
		return br;
	}
	
}
