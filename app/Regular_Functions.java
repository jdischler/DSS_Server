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
public class Regular_Functions
{

	//--------------------------------------------------------------------------
	public void Regular_Functions(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		
		// $ per pixel cost for Corn
		float PC_Cost = 1124 * 0.0001f * 900;
		// $ per pixel cost for Grass
		float PG_Cost = 412  * 0.0001f * 900;
		// Price per pixel
		float P_Per_Corn = 300;
		float P_Per_Stover = 100;
		float P_Per_Grass = 100;
		
		float Max_E = -1000f;
		float Min_E =  1000f;
		float Max_NI = -1000f;
		float Min_NI =  1000f;
		float Max_NE = -1000f;
		float Min_NE =  1000f;
		float Min_Max[] = new float[6];
		
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
			Logger.info("Opening the files to write");
			
			// Raad Input Files
			BufferedReader br1 = new HeaderRead("slope-soil", width, height, "Inputs").getReader();
			BufferedReader br2 = new HeaderRead("depth", width, height, "Inputs").getReader();
			BufferedReader br3 = new HeaderRead("silt", width, height, "Inputs").getReader();
			BufferedReader br4 = new HeaderRead("cec", width, height, "Inputs").getReader();
			
			// Generate output file to write the model outcome
		
			// Ethanol Production
			PrintWriter out1 = new HeaderWrite("Ethanol", width, height, Output_Folder).getWriter();
			
			// Net Income
			PrintWriter out2 = new HeaderWrite("Net_Income", width, height, Output_Folder).getWriter();
			
			// Net Energy
			PrintWriter out3 = new HeaderWrite("Net_Energy", width, height, Output_Folder).getWriter();
			
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
				//Logger.info(Float.toString(0) + " " + Float.toString(0) + " " + Float.toString(Float.parseFloat(slope[0])) + " " + Float.toString(Float.parseFloat(soil[0])) + " " + Float.toString(Float.parseFloat(silt[0])) + " " + Float.toString(Float.parseFloat(cec[0])));
							
				// Outputs
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				StringBuffer sb3 = new StringBuffer();
				//StringBuffer sb4 = new StringBuffer();
				//StringBuffer sb5 = new StringBuffer();
				//StringBuffer sb6 = new StringBuffer();
				//StringBuffer sb7 = new StringBuffer();
				//StringBuffer sb8 = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{				
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data Value
						sb1.append(stringNoData);
						sb2.append(stringNoData);
						sb3.append(stringNoData);
						//sb4.append(stringNoData);
						//sb5.append(stringNoData);
						//sb6.append(stringNoData);
						//sb7.append(stringNoData);
						//sb8.append(stringNoData);
					}
					else if (selection.mSelection[y][x] == 1)
					{
						
						int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
						int Forest_Mask = 1024; // 11
						int Grass_Mask = 128 + 256; // 8 and 9
						int Corn_Mask = 1; // 1

						// Ethanol Production
						float Corn_Y = 0;
						float Corn_P = 0;
						float Grass_Y = 0;
						float Grass_P = 0;
						float Ethanol = 0;
						//float Ethanol_C = 0;
						//float Ethanol_G = 0;
						//float Ethanol_T = 0;
						//float Ethanol_TT = 0;
						
						
						// Calculate prodcution and gross cost 
						// $ per pixel for Corn
						//float PC_Cost = 1124 * 0.0001f * 900;
						// $ per pixel for Grass
						//float PG_Cost = 412 * 0.0001f * 900;
						float Return = 0;
						//float GC_Return = 0;
						//float GG_Return = 0;
						float Net_Income = 0;
						
						// Energy
						//float NetC_Energy = 0;
						//float NetG_Energy = 0;
						float Net_Energy = 0;

						if ((RotationT[y][x] & Corn_Mask) > 0)
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
							//NetC_Energy = (Corn_P * 0.5f * 0.00040f * 21.20f + Corn_P * 0.25f * 0.00038f * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Corn_P * 0.5f * 0.0004f + 1.71f * Corn_P * 0.25f * 0.00038f);
							//NetC_Energy = (Corn_P * 0.5f * 0.40f * 21.20f + Corn_P * 0.25f * 0.38f * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Corn_P * 0.25f * 0.38f * 1000);
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Grass_Y = 2.20f - 0.07f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.07f * Float.parseFloat(silt[x]) + 0.03f * Float.parseFloat(cec[x]);
							Grass_P = 0.0001f * 900 * Grass_Y;
							// Ethonal Calculation
							// Tonnes per pixel
							Ethanol = Grass_P * 0.38f * 1000;
							//Ethanol_G = (Grass_Y * 0.38f)/G_Max;
							//Ethanol_C = 0;
							//Count_G++;
							// Net_Income Calculation
							Return = P_Per_Grass * Grass_P;
							//Net_Income = (GG_Return - PG_Cost) / Max_NI_G;
							Net_Income = Return - PG_Cost;
							// Net_Energy Calculation (Mega Jul)
							Net_Energy = (Grass_P * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Grass_P * 0.38f * 1000);
						}

						// Ethonal Calculation
						// Write Habitat Index to The File
						sb1.append(String.format("%.4f", Ethanol));
						// Net Income
						sb2.append(String.format("%.4f", Net_Income));
						// Net Energy
						sb3.append(String.format("%.4f", Net_Energy));
	
					}
					if (x != width - 1) 
					{
						sb1.append(" ");
						sb2.append(" ");
						sb3.append(" ");
					//	sb4.append(" ");
					//	sb5.append(" ");
					//	sb6.append(" ");
					//	sb7.append(" ");
					//	sb8.append(" ");
					}
				}
				out1.println(sb1.toString());
				out2.println(sb2.toString());
				out3.println(sb3.toString());
				//out4.println(sb4.toString());
				//out5.println(sb5.toString());
				//out6.println(sb6.toString());
				//out7.println(sb5.toString());
				//out8.println(sb6.toString());
			}
			//Logger.info("Count C and G:" + Integer.toString(Count_C) + " " + Integer.toString(Count_G));
			//Logger.info("Closing the Files");
			// Close input files
			br1.close();
			br2.close();
			br3.close();
			br4.close();
			// Close output files
			out1.close();
			out2.close();
			out3.close();
			//out4.close();
			//out5.close();
			//out6.close();
			//out7.close();
			//out8.close();
			//Logger.info("Writting to the Files has finished");
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
		
		//return Min_Max;
	}

	public float Max(float a1, float a2)
	{
		if (a1 >= a2)
		{
			return a1;
		}
		else
			return a2;
	}
	
	public float Min(float a1, float a2)
	{
		if (a1 >= a2)
		{
			return a2;
		}
		else
			return a1;
	}

}
