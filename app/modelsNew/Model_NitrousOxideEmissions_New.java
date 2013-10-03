package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses to calculate nitrous oxide emissions(Tonnes per hec) 
// This model is from unpublished work by 
// Inputs are layers, selected cells in the raster map and crop rotation layer 
// Outputs are ASCII map of nitrous oxide emissions
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_NitrousOxideEmissions_New
{
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(">>> Computing Nitrous Oxide Index");
long timeStart = System.currentTimeMillis();
		
		float [][] nitrousOxideData = new float[height][width];
Logger.info("  > Allocated memory for N20");
		
		int Grass_Mask = 256; // 9
		int Corn_Mask = 1; // 1	
		int Soy_Mask = 2; // 2	
		int Corn_Soy_Mask = 4; // 3	
		int Alfalfa_Mask = 128; // 8	
		int Mask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask | Corn_Soy_Mask;
		
		// Input layers
		float texture[][] = Layer_Base.getLayer("Texture").getFloatData();
		float OM_SOC[][] = Layer_Base.getLayer("OM_SOC").getFloatData();
		float drainage[][] = Layer_Base.getLayer("Drainage").getFloatData();
		float pH[][] = Layer_Base.getLayer("PH").getFloatData();
			
		// Constant for input layers
		float cropRotation = 0;
		float fertRate = 0;
		
		// Calculate Nitrous Oxide Emissions
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				
				if (texture[y][x] > -9995.0f && OM_SOC[y][x] > -9995.0f &&
					drainage[y][x] > -9995.0f && pH[y][x] > -9995.0f)
				{
					fertRate = 0.0f;
					
					if ((rotationData[y][x] & Corn_Mask) > 0) // CORN
					{
						cropRotation = 0.0f;
						fertRate = 168.0f;
					}
					else if ((rotationData[y][x] & Grass_Mask) > 0) // GRASS
					{
						cropRotation = -1.268f;
						fertRate = 56.0f;
					}
					else if ((rotationData[y][x] & Soy_Mask) > 0) // SOY
					{
						cropRotation = -1.023f;
					}
					else if ((rotationData[y][x] & Alfalfa_Mask) > 0) // ALFALFA
					{
						cropRotation = -1.023f;
					}
					else if ((rotationData[y][x] & Corn_Soy_Mask) > 0) // Corn_Soy
					{
						cropRotation = -0.5115f;
						fertRate = 84.0f;
					}
					else // OTHER crops
					{
						cropRotation = 0.0f;
					}
					
					if ((rotationData[y][x] & Mask) > 0)
					{
						// Calculate Nitrous Oxide Emissions (Unit Kg/Ha per year)
						nitrousOxideData[y][x] = (float)(Math.exp(0.414f + 0.825f + fertRate * 0.005f + cropRotation +
							texture[y][x] + OM_SOC[y][x] + drainage[y][x] + pH[y][x]));
					}
					else
					{
						nitrousOxideData[y][x] = -9999.0f;
					}
				}
				else 
				{
					nitrousOxideData[y][x] = -9999.0f;
				}
			}
		}
	
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult("nitrous_oxide", scenario.mOutputDir, nitrousOxideData, width, height));

long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model Nitrous Oxide - timing: " + Float.toString(timeSec));

		return results;
	}
}

