package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses proportion of Ag at watershed scale to calculate Nitrogen
// This model is from unpublished work by Tim University of Wisconsin Madison
// Input is crop rotation layer 
// Output is ASCII map of Nitrogen
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Nitrogen
{

	//--------------------------------------------------------------------------
	public void Nitrogen(Selection selection, String Output_Folder, int[][] RotationT)
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
		int[] Total_Ag = new int[Num_Watersheds];
		float[] Nitrogen = new float[Num_Watersheds];
		
		// Rotation
		//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		if (RotationT == null)
		{
			Logger.info("Fail Rotation");
			layer = new Layer_Integer("Rotation"); layer.init();
			RotationT = Layer_Base.getLayer("Rotation").getIntData();
		}
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();
		
		try 
		{
			
			// Raad input file for watershed layer
			BufferedReader br1 =  new HeaderRead("watersheds", width, height, "Inputs").getReader();
			
			// Nitrogen
			//PrintWriter out_N = new HeaderWrite("Nitrogen", width, height, Output_Folder).getWriter();
			
			// Precompute this so we don't do it on every cell
			//String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				
				// Read watershed layer line by line from ASCII files
				String line1 = br1.readLine();
				// Split each line based on space between values
				String watershed[] = line1.split("\\s+");
				
				// Outputs
				//StringBuffer sb_N = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{
					
					//if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					//{
						// Check for No-Data Value
						//sb_N.append(stringNoData);
					//}

					if ((RotationT[y][x] & Ag_Mask) > 0)
					{
						// Count Ag cells within each watershed
						Total_Ag[Integer.parseInt(watershed[x])]++;
						
						if (selection.mSelection[y][x] == 1)
						{
						// Count Ag cells within each watershed from selected cells
							Count_Ag[Integer.parseInt(watershed[x])]++;
						}
					}
					
					//if (x != width - 1) 
					//{
					//	sb_N.append(" ");
					//}
					
				}
				//out_N.println(sb_N.toString());
			}
			// Close output files
			//out_N.close();
		}

		// Write Nitrogen to The File
		//sb_N.append(String.format("%.2f", Nitrogen));
		
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
		
				for (int i = 0; i < Num_Watersheds; i++)
		{
			Prop_Ag[i] = Count_Ag[i] / Total_Ag[i];
			// Nitrogen
			Nitrogen[i] = (float)Math.pow(10, 1.13f * Prop_Ag[i] - 0.23f);
			// Print out the results
			Logger.info("Count_Ag:" + Integer.toString(Count_Ag[i]));
			Logger.info("Total_Ag:" + Integer.toString(Total_Ag[i]));
			Logger.info("Prop_Ag:" + Float.toString(Nitrogen[i]));
			Logger.info("Nitrogen:" + Float.toString(Nitrogen[i]));
		}
	}	
	
}
