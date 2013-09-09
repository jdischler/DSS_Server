package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Selection Process
//
// This program selects cells in the raster map based on user request 
// Inputs are arbitrary raster file and selection
// Outputs are an array that hold the selected cells
// This program just works for default because the ASCII files for default have been calculated before
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_Selection
{
	
	public JsonNode Selection(Selection selection1, Selection selection2, float[] Data)
	//public JsonNode Selection(Selection selection, String Input_File, String Output_Folder)
	{
		
		// Defining variables based on the selected layer
		Layer_Base layer;
		int width, height;
		//float NO_DATA = -9999.0f;
		int i = 0;
		int j = 0;
		int Total_Cells = selection1.countSelectedPixels();	
		//int Grass_Mask = 256; // 9
		//int Corn_Mask = 1; // 1	
		//int Soy_Mask = 2; // 2	
		//int Alfalfa_Mask = 128; // 8
		
		//int size = Data.length;
		
		float Total = 0;
		int Value = 0;
		int Bin = 10;
		int[] CountBin = new int [Bin];
		float Per_Cell = 0;
		int Zero_Num = 0;
		float[] Selected_Cells = new float[Total_Cells];
		
		// Load selection layer to the array
		// Selection layer
		//float[] Array_Selection = new float[Total_Cells];
		float Min =  1000000;
		float Max = -1000000;
		
		// Retrive rotation layer from memory
		//int[][] RotationT = Layer_Base.getLayer("Rotation").getIntData();
		//if (RotationT == null)
		//{
			// Logger.info("Fail Rotation");
		//	layer = new Layer_Raw("Rotation"); layer.init();
		//	RotationT = Layer_Base.getLayer("Rotation").getIntData();
		//}
			layer = Layer_Base.getLayer("Rotation");
			width = layer.getWidth();
			height = layer.getHeight();
		
		try 
		{

			// Raad Input File
			//BufferedReader br1 =  new HeaderRead(Input_File, width, height, Output_Folder).getReader();
			
			for (int y = 0; y < height; y++) 
			{
				// Read slope, soil depth, silt and CEC layers line vy line from ASCII files
				//String line1 = br1.readLine();
				// Split each line based on space between values
				//String text[] = line1.split("\\s+");
				
				for (int x = 0; x < width; x++) 
				{
					if (selection1.mSelection[y][x] == 1)
					{
						
						if (selection2.mSelection[y][x] == 1)
						{
							if (Data[i] == 0)
							//if (Float.parseFloat(text[x]) != -9999.0f)
							//if (Float.isNaN(Data[i]))
							{
								Zero_Num++;
							}
							else
							{
								// Fill the array
								//Array_Selection[i] = Float.parseFloat(text[x]);
								// Find the Min and Max between selected data
								Min = Min(Min, Data[i]);
								Max = Max(Max, Data[i]);
								Selected_Cells[j] = Data[i];
								// Corn
								//if ((RotationT[y][x] & Corn_Mask) > 0)
								//{
								//}
								// Grass
								//else if ((RotationT[y][x] & Grass_Mask) > 0)
								//{
								//}
								// Soy
								//else if ((RotationT[y][x] & Soy_Mask) > 0)
								//{
								//}
								// Alfalfa
								//else if ((RotationT[y][x] & Alfalfa_Mask) > 0)
								//{
								//}
								//else 
								//{
								//}
								j++;
							}
						}
						
						i = i + 1;
						
					}
				
				}
			}
			// Close input files
			//br1.close();

		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("I and J are: " + Integer.toString(i) + " " + Integer.toString(j) );
		}
		
		Logger.info("Zero_Num is: " + Integer.toString(Zero_Num));
									
		// Find the bins for data
		for (i = 0; i < j; i++) 
		//for (i = 0; i < Array_Selection.length; i++) 
		{
			// Calculate total
			//Total = Total + Array_Selection[i];
			//if (Data[i] != 0)
			//{
				Total = Total + Selected_Cells[i];
				// Calculate value tp find the bin
				//Value = (int)((Array_Selection[i] - Min)/(Max - Min) * (Bin - 1));
				Value = (int)((Selected_Cells[i] - Min)/(Max - Min) * (Bin - 1));
				CountBin[Value]++;
				//j++;
			//}
		}
		
		// Convert Bins to Array
		ArrayNode ArrayS = JsonNodeFactory.instance.arrayNode();
		for (i = 0; i < CountBin.length; i++) 
		{
			ArrayS.add(CountBin[i]);
		}
		
		// Calculate average between selected cells
		Per_Cell = Total / (j * Max);
		
		// Define Json to store and return data to client
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		
		// Return to the client
		obj.put("Bins", ArrayS);
		obj.put("Min", Min);
		obj.put("Max", Max);
		obj.put("Average_Normalized", Per_Cell);
		
		return obj;
	}
	
	// Min
	public float Min(float Min, float Num)
	{ 
		// Min
		if (Num < Min)
		{
			Min = Num;
		}
		
		return Min;
	}
	
	// Max
	public float Max(float Max, float Num)
	{

		// Max
		if (Num > Max)
		{
			Max = Num;
		}
		
		return Max;
	}
	
}
