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
		int i = 0;
		int j = 0;
		int Total_Cells = selection1.countSelectedPixels();	
		
		float Total = 0;
		int Value = 0;
		int Bin = 10;
		int[] CountBin = new int [Bin];
		float Sum = 0;
		int Zero_Num = 0;
		
		// Load selection layer to the array
		// Selection layer
		//float[] Array_Selection = new float[Total_Cells];
		float Min =  1000000;
		float Max = -1000000;
		
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();
		
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				if (selection1.mSelection[y][x] == 1 && selection2.mSelection[y][x] == 1)
				{
					//if (Data[i] != 0)
					if (Data[i] > 0 || Data[i] < 0)
					{
						// Fill the array
						//Array_Selection[i] = Float.parseFloat(text[x]);
						// Find the Min and Max between selected data
						Min = Min(Min, Data[i]);
						Max = Max(Max, Data[i]);
					}
					else {
						Zero_Num++;
					}
					
					i++;
				}
			}
		}
		
		Logger.info("Zero_Num is: " + Integer.toString(Zero_Num));
		i = 0;
		int totalCount = 0;
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				if (selection1.mSelection[y][x] == 1 && selection2.mSelection[y][x] == 1)
				{
					//if (Data[i] != 0)
					if (Data[i] > 0 || Data[i] < 0)
					{
						Total = Total + Data[i];
						// Calculate value tp find the bin
						Value = (int)((Data[i] - Min)/(Max - Min) * (Bin - 1));
						CountBin[Value]++;
						totalCount++;
					}
					i++;
				}
			}
		}
	
		// Convert Bins to Array
		ArrayNode ArrayS = JsonNodeFactory.instance.arrayNode();
		for (j = 0; j < CountBin.length; j++) 
		{
			ArrayS.add(CountBin[j]);
		}
		
		// Calculate average between selected cells
//		Per_Cell = Total / (totalCount * Max);
		//Per_Cell = Total / totalCount;
		Sum = Total;
		
		// Define Json to store and return data to client
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		
		// Return to the client
		obj.put("Bins", ArrayS);
		obj.put("Min", Min);
		obj.put("Max", Max);
		obj.put("Sum", Sum);
		obj.put("Count", totalCount);
		
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

