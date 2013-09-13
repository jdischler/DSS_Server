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
public class Model_Selection_N_P
{
	
	public JsonNode Selection_N_P(float[] Data, int[] Freq)
	//public JsonNode Selection(Selection selection, String Input_File, String Output_Folder)
	{
		
		// Defining variables based on the selected layer
		int i = 0;
		int j = 0;
		float Total = 0;
		int Value = 0;
		int Bin = 10;
		int[] CountBin = new int [Bin];
		int Total_Cells = 0;
		float Per_Cell = 0;
		
		// Load selection layer to the array
		float Min =  1000000;
		float Max = -1000000;
		
		try 
		{
			for (i = 0; i < Data.length; i++) 
			{
				Min = Min(Min, Data[i]);
				Max = Max(Max, Data[i]);
			}
		}
		
		catch(Exception err) 
		{
			Logger.info(err.toString());
			//Logger.info("I and J are: " + Integer.toString(i) + " " + Integer.toString(j) );
		}
								
		// Find the bins for data
		for (i = 0; i < Data.length; i++)
		//for (i = 0; i < Array_Selection.length; i++) 
		{
			Total_Cells = Total_Cells + Freq[i];
			Total = Total + Data[i] * Freq[i];
			Value = (int)((Data[i] - Min)/(Max - Min) * (Bin - 1));
			CountBin[Value] = Freq[i];
		}
		
		// Convert Bins to Array
		ArrayNode ArrayS = JsonNodeFactory.instance.arrayNode();
		for (i = 0; i < CountBin.length; i++) 
		{
			ArrayS.add(CountBin[i]);
		}
		
		// Calculate average between selected cells
		//Per_Cell = Total / (Total_Cells * Max);
		Per_Cell = Total / Total_Cells;
		
		// Define Json to store and return data to client
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		
		// Return to the client
		obj.put("Bins", ArrayS);
		obj.put("Min", Min);
		obj.put("Max", Max);
		obj.put("Average", Per_Cell);
		
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
