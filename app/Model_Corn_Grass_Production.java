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
public class Model_Corn_Grass_Production
{
	
	static int mWidth, mHeight;
	
	//--------------------------------------------------------------------------
	public TwoArrays Corn_Grass_P(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		int Total_Cells = selection.countSelectedPixels();
		int Grass_Mask = 128 + 256; // 8 and 9
		int Corn_Mask = 1; // 1	
		//int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		//int Forest_Mask = 1024; // 11
		float Corn_Y = 0;
		float Grass_Y = 0;
		// Corn Production
		float[] Corn_P = new float[Total_Cells];
		// Grass Production
		float[] Grass_P = new float[Total_Cells];
		
		// Ton/ha
		//float Min_Corn_Y = 3.08f - 0.11f * 70;
		//float Max_Corn_Y = 3.08f + 0.02f * 210 + 0.10f * 75 + 0.04f * 200;
		// Tons per pixel
		//float Min_Corn_P = 0.0001f * 900 * Min_Corn_Y;
		//float Max_Corn_P = 0.0001f * 900 * Max_Corn_Y;
		// Ton/ha
		//float Min_Grass_Y = 2.20f - 0.07f * 70;
		//float Max_Grass_Y = 2.20f + 0.02f * 210 + 0.07f * 75 + 0.03f * 200;
		// Tons per pixel
		//float Min_Grass_P = 0.0001f * 900 * Min_Grass_Y;
		//float Max_Grass_P = 0.0001f * 900 * Max_Grass_Y;
		
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
		
		try {

			// Raad Input Files
			BufferedReader br1 =  new HeaderRead("slope-soil", width, height, "Inputs").getReader();
			BufferedReader br2 = new HeaderRead("depth", width, height, "Inputs").getReader();
			BufferedReader br3 = new HeaderRead("silt", width, height, "Inputs").getReader();
			BufferedReader br4 = new HeaderRead("cec", width, height, "Inputs").getReader();
			
			// Cron Production
			PrintWriter out_C = new HeaderWrite("Cron_Production", width, height, Output_Folder).getWriter();
			// Grass Production
			PrintWriter out_G = new HeaderWrite("Grass_Production", width, height, Output_Folder).getWriter();
			
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			
			for (int y = 0; y < height; y++) 
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
				StringBuffer sb_C = new StringBuffer();
				StringBuffer sb_G = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data
						sb_C.append(stringNoData);
						sb_G.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						// Ethanol Production
						Corn_Y = 0;
						Grass_Y = 0;
						
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Tonnes per Ha
							Corn_Y = 3.08f - 0.11f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.10f * Float.parseFloat(silt[x]) + 0.04f * Float.parseFloat(cec[x]);
							Corn_P[i] = 0.0001f * 900 * Corn_Y;

						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Grass_Y = 2.20f - 0.07f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.07f * Float.parseFloat(silt[x]) + 0.03f * Float.parseFloat(cec[x]);
							Grass_P[i] = 0.0001f * 900 * Grass_Y;
						}
						
						// Write To The File
						sb_C.append(String.format("%.4f",  Corn_P[i]));
						sb_G.append(String.format("%.4f", Grass_P[i]));
						
						i = i + 1;
					}
					if (x != width - 1) 
					{
						sb_C.append(" ");
						sb_G.append(" ");
					}
				}
				out_C.println(sb_C.toString());
				out_G.println(sb_G.toString());
			}
			// Close input files
			br1.close();
			br2.close();
			br3.close();
			br4.close();
			// Close output files
			out_C.close();
			out_G.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
		
		return new TwoArrays(Corn_P, Grass_P);
	}
	
}
