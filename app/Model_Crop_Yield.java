package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses slope, soil depth, silt and CEC to calculate corn, soy, grass and alfalfa yield (Tonnes per hec) 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are slope, soil depth, silt and CEC layers, selected cells in the raster map and crop rotation layer 
// Outputs are ASCII map of corn, soy, grass and alfalfa yield
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Crop_Yield
{
	public float[] Crop_Y(Selection selection, int[][] RotationT)
	//public OneArray Crop_Y(Selection selection, int[][] RotationT)
	//public FourArrays Crop_Y(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int i = 0;
		//int j = 0;
		int Total_Cells = selection.countSelectedPixels();
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Alfalfa_Mask = 128; // 8	
		
		// Corn and Grass Yield
		float Corn_Y = 0;
		float Grass_Y = 0;
		float Soy_Y = 0;
		float Alfalfa_Y = 0;
		
		// Define separate arrays to keep corn and grass production
		// Crop Yield
		float[] YI = new float[Total_Cells];
		// Corn Yield
		//float[] Corn_YI = new float[Total_Cells];
		// Grass Yield
		//float[] Grass_YI = new float[Total_Cells];
		// Soy Yield
		//float[] Soy_YI = new float[Total_Cells];
		// Alfalfa Yield
		//float[] Alfalfa_YI = new float[Total_Cells];
		// Corn Production
		//float[] Corn_P = new float[Total_Cells];
		// Grass Production
		//float[] Grass_P = new float[Total_Cells];
		// Soy Production
		//float[] Soy_P = new float[Total_Cells];
		// Alfalfa Production
		//float[] Alfalfa_P = new float[Total_Cells];
		
		// Retrive rotation layer from memory
		//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		//if (RotationT == null)
		//{
		//	Logger.info("Fail Rotation");
		//	layer = new Layer_Raw("Rotation"); layer.init();
		//	RotationT = Layer_Base.getLayer("Rotation").getIntData();
		//}
			layer = Layer_Base.getLayer("Rotation");
			width = layer.getWidth();
			height = layer.getHeight();
		
		try 
		{

			// Raad Input Files for slope, soil depth, silt and CEC layers
			BufferedReader br1 =  new HeaderRead("slope-soil", width, height, "Inputs").getReader();
			BufferedReader br2 = new HeaderRead("depth", width, height, "Inputs").getReader();
			BufferedReader br3 = new HeaderRead("silt", width, height, "Inputs").getReader();
			BufferedReader br4 = new HeaderRead("cec", width, height, "Inputs").getReader();
			
			// Creating ASCII file to ouput corn and grass production value
			// Cron Yield
			//PrintWriter out_C = new HeaderWrite("Corn_Yield", width, height, Output_Folder).getWriter();
			// Grass Yield
			//PrintWriter out_G = new HeaderWrite("Grass_Yield", width, height, Output_Folder).getWriter();
			// Soy Yield
			//PrintWriter out_S = new HeaderWrite("Soy_Yield", width, height, Output_Folder).getWriter();
			// Alfalfa Yield
			//PrintWriter out_A = new HeaderWrite("Alfalfa_Yield", width, height, Output_Folder).getWriter();
			
			// Precompute this so we don't do it on every cell
			//String stringNoData = Integer.toString(NO_DATA);
			
			
			for (int y = 0; y < height; y++) 
			{
				// Read slope, soil depth, silt and CEC layers line by line from ASCII files
				String line1 = br1.readLine();
				// Split each line based on space between values
				String slope[] = line1.split("\\s+");
				String line2 = br2.readLine();
				String soil[] = line2.split("\\s+");
				String line3 = br3.readLine();
				String silt[] = line3.split("\\s+");
				String line4 = br4.readLine();
				String cec[] = line4.split("\\s+");
				
				// Make buffer to write outputs
				//StringBuffer sb_C = new StringBuffer();
				//StringBuffer sb_G = new StringBuffer();
				//StringBuffer sb_S = new StringBuffer();
				//StringBuffer sb_A = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					//if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					//{
						// Check for No-Data
					//	sb_C.append(stringNoData);
					//	sb_G.append(stringNoData);
					//	sb_S.append(stringNoData);
					//	sb_A.append(stringNoData);
					//}
					if (selection.mSelection[y][x] == 1)
					{
						// Corn
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Calculate Corn Yield 
							// Bushels per Ac
							// Corn_Y = 3.08f - 0.11f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.10f * Float.parseFloat(silt[x]) + 0.04f * Float.parseFloat(cec[x]);
							//Corn_Y = -62.27f - (1.08f * Float.parseFloat(slope[x])) + 23.08f * (float)(Math.log((Double.parseDouble(soil[x])))) + 0.80f * Float.parseFloat(silt[x]) + 1.39f * Float.parseFloat(cec[x]);
							Corn_Y = 22.000f - 1.05f * Float.parseFloat(slope[x]) + 0.190f * Float.parseFloat(soil[x]) + 0.817f * Float.parseFloat(silt[x]) + 1.32f * Float.parseFloat(cec[x]);
							// Correct for techno advances
							Corn_Y = Corn_Y * 1.30f;
							// add stover
							Corn_Y = Corn_Y + Corn_Y;
							// Tonnes per Hec
							//Corn_YI[i] = Corn_Y * 0.053f;
							YI[i] = Corn_Y * 0.053f;
							// Tonnes per pixel
							//Corn_P[i] = 0.0001f * 900 * Corn_Y;
							//if (Corn_YI[i] >= 9 && Corn_YI[i] <= 25)
							//{
								//sb_C.append(String.format("%.4f",  Corn_YI[i]));
							//	sb_C.append(String.format("%.4f",  YI[i]));
							//}
							//else 
							//{
							//	sb_C.append(stringNoData);
							//}
							//i++;
						}
						//else 
						//{
						//	sb_C.append(stringNoData);
						//}
						// Grass
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							// Calculate Grass Yield 
							// short tons per Ac
							// Grass_Y = 2.20f - 0.07f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.07f * Float.parseFloat(silt[x]) + 0.03f * Float.parseFloat(cec[x]);
							Grass_Y = 0.77f - 0.031f * Float.parseFloat(slope[x]) + 0.008f * Float.parseFloat(soil[x]) + 0.029f * Float.parseFloat(silt[x]) + 0.038f * Float.parseFloat(cec[x]);
							// Correct for techno advances
							Grass_Y = Grass_Y * 1.05f;
							// Tonnes per Hec
							//Grass_YI[i] = Grass_Y * 1.91f;
							YI[i] = Grass_Y * 1.91f;
							// Tonnes per pixel
							//Grass_P[i] = 0.0001f * 900 * Grass_Y;
							//if (Grass_YI[i] >= 3 && Grass_YI[i] <= 12)
							//{
								//sb_G.append(String.format("%.4f", Grass_YI[i]));
							//	sb_G.append(String.format("%.4f", YI[i]));
							//}
							//else
							//{
							//	sb_G.append(stringNoData);
							//}
							//i++;
						}
						//else 
						//{
						//	sb_G.append(stringNoData);
						//}	
						// Soy
						else if ((RotationT[y][x] & Soy_Mask) > 0)
						{
							// Calculate Soy Yield 
							// Bushels per Ac
							//Soy_Y = -22.76f - (0.35f * Float.parseFloat(slope[x])) + 7.99f * (float)(Math.log((Double.parseDouble(soil[x])))) + 0.27f * Float.parseFloat(silt[x]) + 0.46f * Float.parseFloat(cec[x]);
							Soy_Y = 6.37f - 0.34f * Float.parseFloat(slope[x]) + 0.065f * (Float.parseFloat(soil[x])) + 0.278f * Float.parseFloat(silt[x]) + 0.437f * Float.parseFloat(cec[x]);
							// Correct for techno advances
							Soy_Y = Soy_Y * 1.2f;
							// Tonnes per Hec
							Soy_Y = Soy_Y * 0.0585f;
							// add residue
							//Soy_YI[i] = Soy_Y + Soy_Y * 1.5f;
							YI[i] = Soy_Y + Soy_Y * 1.5f;
							// Tonnes per pixel
							//Soy_P[i] = 0.0001f * 900 * Soy_Y;
							//if (Soy_YI[i] >= 2 && Soy_YI[i] <= 10)
							//{
								//sb_S.append(String.format("%.4f",  Soy_YI[i]));
							//	sb_S.append(String.format("%.4f",  YI[i]));
							//}
							//else
							//{
							//	sb_S.append(stringNoData);
							//}
							//i++;
						}
						//else 
						//{
						//	sb_S.append(stringNoData);
						//}
						// Alfalfa
						else if ((RotationT[y][x] & Alfalfa_Mask) > 0)
						{
							// Calculate Alfalfa Yield 
							// Short tons per Acre
							// Alfalfa_Y = -1.98f - (0.04f * Float.parseFloat(slope[x])) + 0.88f * (float)(Math.log((Double.parseDouble(soil[x])))) + 0.03f * Float.parseFloat(silt[x]) + 0.04f * Float.parseFloat(cec[x]);
							Alfalfa_Y = 1.26f - 0.045f * Float.parseFloat(slope[x]) + 0.007f * (Float.parseFloat(soil[x])) + 0.027f * Float.parseFloat(silt[x]) + 0.041f * Float.parseFloat(cec[x]);
							// Yield Correction Factor for modern yield
							Alfalfa_Y = Alfalfa_Y * 1.05f;
							// Tonnes per Hec
							//Alfalfa_YI[i] = Alfalfa_Y * 1.905f;
							YI[i] = Alfalfa_Y * 1.905f;
							//Tonnes per pixel
							//Alfalfa_P[i] = 0.0001f * 900 * Alfalfa_Y;
							//if (Alfalfa_YI[i] >= 4 && Alfalfa_YI[i] <= 12)
							//{
								//sb_A.append(String.format("%.4f",  Alfalfa_YI[i]));
							//	sb_A.append(String.format("%.4f",  YI[i]));
							//}
							//else
							//{
							//	sb_A.append(stringNoData);
							//}
							//i++;
						}
						//else 
						//{
						//	sb_A.append(stringNoData);
						//}
						else 
						{
							YI[i] = 0;
						//	YI[i] = NO_DATA;
						//	YI[i] = Float.NaN;
						//	j++;
						}
						
						i = i + 1;
					}
					//if (x != width - 1) 
					//{
					//	sb_C.append(" ");
					//	sb_G.append(" ");
					//	sb_S.append(" ");
					//	sb_A.append(" ");
					//}
				}
				//out_C.println(sb_C.toString());
				//out_G.println(sb_G.toString());
				//out_S.println(sb_S.toString());
				//out_A.println(sb_A.toString());
			}
			// Close input files
			br1.close();
			br2.close();
			br3.close();
			br4.close();
			// Close output files
			//out_C.close();
			//out_G.close();
			//out_S.close();
			//out_A.close();
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with array!");
			//Logger.info("Oops, something went wrong with writing to the files!");
		}
		
		Logger.info("Model_Crop_Yield is finished");
		Logger.info("Number of selected cells are: " + Integer.toString(i));
		//Logger.info("Number of cells that are Corn, Soy, Grass or Alphaalpha are: " + Integer.toString(i));
		
		//return new FourArrays(Corn_YI, Grass_YI, Soy_YI, Alfalfa_YI);
		//return new OneArray(YI, i);
		return YI;
	}
	
}
