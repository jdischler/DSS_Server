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

public class Moving_Window_Functions
{
		
	//static int Window_Size;
	//static int mWidth, mHeight;

	//--------------------------------------------------------------------------
	public void Moving_Window_Functions(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		//float Total_Cells = selection.countSelectedPixels();

		// Rotation
		int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		if (Rotation == null)
		{
			Logger.info("Fail Rotation");
			layer = new Layer_Integer("Rotation"); layer.init();
			Rotation = Layer_Base.getLayer("Rotation").getIntData();
		}
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();

		try 
		{
			
			// Bird Index
			PrintWriter out4 = new HeaderWrite("Habitat_Index", width, height, Output_Folder).getWriter();
			
			// Nitrogen
			PrintWriter out5 = new HeaderWrite("Nitrogen", width, height, Output_Folder).getWriter();
			
			// Phosphorus
			PrintWriter out6 = new HeaderWrite("Phosphorus", width, height, Output_Folder).getWriter();
			
			// Pest
			PrintWriter out7 = new HeaderWrite("Pest_Suppression", width, height, Output_Folder).getWriter();
			
			// Pollinator 
			PrintWriter out8 = new HeaderWrite("Pollinator", width, height, Output_Folder).getWriter();
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
			
				// Outputs
				//StringBuffer sb1 = new StringBuffer();
				//StringBuffer sb2 = new StringBuffer();
				//StringBuffer sb3 = new StringBuffer();
				StringBuffer sb4 = new StringBuffer();
				StringBuffer sb5 = new StringBuffer();
				StringBuffer sb6 = new StringBuffer();
				StringBuffer sb7 = new StringBuffer();
				StringBuffer sb8 = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{				
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data Value
						//sb1.append(stringNoData);
						//sb2.append(stringNoData);
						//sb3.append(stringNoData);
						sb4.append(stringNoData);
						sb5.append(stringNoData);
						sb6.append(stringNoData);
						sb7.append(stringNoData);
						sb8.append(stringNoData);
					} 
					else if (selection.mSelection[y][x] == 1)
					{
						
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
						float Habitat_Index = (float)((1 / ( 1 / Math.exp(Lambda) + 1 ) ) / 0.67f);
						// Write Habitat Index to The File
						sb4.append(String.format("%.4f", Habitat_Index));

						// Nitrogen
						//float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						// Write Nitrogen to The File
						sb5.append(String.format("%.4f", Nitrogen));

						// Phosphorus 
						//float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f);
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
						sb8.append(String.format("%.3f", Poll));
					}
					if (x != width - 1) 
					{
					//	sb1.append(" ");
					//	sb2.append(" ");
					//	sb3.append(" ");
						sb4.append(" ");
						sb5.append(" ");
						sb6.append(" ");
						sb7.append(" ");
						sb8.append(" ");
					}
				}
				//out1.println(sb1.toString());
				//out2.println(sb2.toString());
				//out3.println(sb3.toString());
				out4.println(sb4.toString());
				out5.println(sb5.toString());
				out6.println(sb6.toString());
				out7.println(sb5.toString());
				out8.println(sb6.toString());
			}
			// Close input files
			//br1.close();
			//br2.close();
			//br3.close();
			//br4.close();
			// Close output files
			//out1.close();
			//out2.close();
			//out3.close();
			out4.close();
			out5.close();
			out6.close();
			out7.close();
			out8.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}

		
	}	
}
