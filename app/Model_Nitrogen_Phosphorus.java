package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import java.lang.reflect.Array;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses proportion of Ag at watershed scale to calculate Nitrogen and Phosphorus
// This model is from unpublished work by Tim University of Wisconsin Madison
// Input is crop rotation layer 
// Outputs are Nitrogen and Phosphorus for each watershed
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Nitrogen_Phosphorus
{

	//--------------------------------------------------------------------------
	public ThreeArrays Nitrogen_Phosphorus(Selection selection, int[][] RotationT, String Output_Folder)
	//public TwoArrays Nitrogen_Phosphorus(Selection selection, int[][] RotationT)
	//public void Nitrogen_Phosphorus(Selection selection, String Output_Folder, int[][] RotationT)
	{
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		
		// Number of watersheds in study area
		int Num_Watersheds = 31;
		float[] Prop_Ag = new float[Num_Watersheds];
		int[] Count_Ag = new int[Num_Watersheds];
		int[] Total = new int[Num_Watersheds];
		float[] Nitrogen = new float[Num_Watersheds];
		float[] Phosphorus = new float[Num_Watersheds];
		//int Int_W;
		
		float Min_N =  1000000;
		float Max_N = -1000000;
		float Min_P =  1000000;
		float Max_P = -1000000;
		float Min_T =  1000000;
		float Max_T = -1000000;
		
		// Rotation
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
			
		// Watersheds
		int[][] Watersheds = Layer_Base.getLayer("watersheds").getIntData();
			
		try 
		{
			
			// Raad input file for watershed layer
			//BufferedReader br1 =  new HeaderRead("watersheds", width, height, "Inputs").getReader();
			
			for (int y = 0; y < height; y++) 
			{
				
				// Read watershed layer line by line from ASCII files
				//String line1 = br1.readLine();
				// Split each line based on space between values
				//String watershed[] = line1.split("\\s+");
				
				for (int x = 0; x < width; x++) 
				{

					if (selection.mSelection[y][x] == 1 & Watersheds[y][x] != 0)
					{
					
						//Int_W = Integer.parseInt(watershed[x]);
											
						//if (Int_W != -9999) 
						//if (Watersheds[y][x] != 0) 
						//{
						
							// Count Ag cells within each watershed
							//Total[Int_W]++;
							Total[Watersheds[y][x] - 1]++;
							
							if ((RotationT[y][x] & Ag_Mask) > 0)
							{
								// Count Ag cells within each watershed from selected cells
								//Count_Ag[Int_W]++;
								Count_Ag[Watersheds[y][x] - 1]++;
							}
							
						//}
						
					}
		
				}

			}
			
			//br1.close();
			
			for (int i = 0; i < Num_Watersheds; i++)
			{
				Prop_Ag[i] = Count_Ag[i] / (float)Total[i];
				// Nitrogen
				Nitrogen[i] = (float)Math.pow(10, 1.13f * Prop_Ag[i] - 0.23f);
				// Phosphorus
				Phosphorus[i] = (float)Math.pow(10, 0.79f * Prop_Ag[i] - 1.44f);
				// Print out the results for each watershed
				//Logger.info("Count_Ag for watershed " + Integer.toString(i) + " is: " + Integer.toString(Count_Ag[i]));
				//Logger.info("Total Cells for watershed " + Integer.toString(i) + " is: " + Integer.toString(Total[i]));
				//Logger.info("Prop_Ag for watershed " + Integer.toString(i) + " is: " + Float.toString(Prop_Ag[i]));
				//Logger.info("Nitrogen for watershed " + Integer.toString(i) + " is: " + Float.toString(Nitrogen[i]));
				//Logger.info("Phosphorus for watershed " + Integer.toString(i) + " is: " + Float.toString(Phosphorus[i]));
				
				//Min_T = Min(Min_T, Total[i]);
				//Max_T = Max(Max_T, Total[i]);
				
				//Min_N = Min(Min_N, Nitrogen[i]);
				//Max_N = Max(Max_N, Nitrogen[i]);
				
				//Min_P = Min(Min_P, Phosphorus[i]);
				//Max_P = Max(Max_P, Phosphorus[i]);
			}
			
			// Raad input file for watershed layer
			//BufferedReader br2 =  new HeaderRead("watersheds", width, height, "Inputs").getReader();
			
			// Nitrogen
			//PrintWriter out_N = new HeaderWrite("Nitrogen", width, height, Output_Folder).getWriter();
			// Phosphorus
			//PrintWriter out_P = new HeaderWrite("Phosphorus", width, height, Output_Folder).getWriter();
				
			// Precompute this so we don't do it on every cell
			//String stringNoData = Integer.toString(NO_DATA);
				
			//for (int y = 0; y < height; y++) 
			//{
				
				// Read watershed layer line by line from ASCII files
				//String line2 = br2.readLine();
				// Split each line based on space between values
				//String watershedW[] = line2.split("\\s+");
				
				// Outputs
				//StringBuffer sb_N = new StringBuffer();
				//StringBuffer sb_P = new StringBuffer();
				
				//for (int x = 0; x < width; x++) 
				//{
						
					//Int_W = Integer.parseInt(watershedW[x]);
					//Logger.info(Integer.toString(Int_W));
				
					//if (Int_W == -9999) 
					//{
						// Check for No-Data Value
						//sb_N.append(stringNoData);
						//sb_P.append(stringNoData);
					//}
					//else
					//{
						// Write output
						//sb_N.append(String.format("%.4f", Nitrogen[Int_W]));
						//sb_P.append(String.format("%.4f", Phosphorus[Int_W]));
					//}
					//for 
					//if (x != width - 1) 
					//{
					//	sb_N.append(" ");
					//	sb_P.append(" ");
					//}

				//}
				
				//out_N.println(sb_N.toString());
				//out_P.println(sb_P.toString());
	
			//}
			
			//br2.close();
			// Close output files
			//out_N.close();
			//out_P.close();

		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with calculation!");
		}
			
		Logger.info("Model_Nitrogen_Phosphorus is finished");
		
		return new ThreeArrays(Nitrogen, Phosphorus, Total, Min_T, Max_T, Min_N, Max_N, Min_P, Max_P);
	}

	// Min
	// public float Min(float Min, float Num)
	// { 
		// // Min
		// if (Num < Min)
		// {
			// Min = Num;
		// }
		// 
		// return Min;
	// }
	
	// Max
	// public float Max(float Max, float Num)
	// {
		// 
		// // Max
		// if (Num > Max)
		// {
			// Max = Num;
		// }
		// 
		// return Max;
	// }	
	
}
