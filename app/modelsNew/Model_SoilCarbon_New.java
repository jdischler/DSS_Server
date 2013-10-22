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
// This program uses corn, soy, grass and alfalfa production to calculate Soil Carbon 
// This model is from unpublished work by Tim University of Wisconsin Madison
// Inputs are corn, soy, grass and alfalfa layers and crop rotation layer 
// Outputs are ASCII map of Net Energy
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_SoilCarbon_New
{
	// Raw Soil Carbon Change Factor (RSCCF) 
	static final float RSCCF_Corn_Grass = 0.63f; // Continuous Corn to Grass
	static final float RSCCF_Corn_Alfalfa = 0.37f; // Continuous Corn to Alfalfa
	static final float RSCCF_Soy_Grass = 0.63f; // Continuous Soy to Grass
	static final float RSCCF_Soy_Alfalfa = 0.37f; // Continuous Soy to Alfalfa
	static final float RSCCF_Alfalfa_Grass = 0.59f; // Continuous Grass to Alfalfa
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationT_Data = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(">>> Computing Soil Carbon Index");
long timeStart = System.currentTimeMillis();
		
		float [][] soilCarbonData = new float[height][width];
Logger.info("  > Allocated memory for SOC");

		// Defining variables based on the selected layer
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int Alfalfa_Mask = cdl.convertStringsToMask("Alfalfa");
		
		int TotalMask = Grass_Mask | Corn_Mask | Soy_Mask | Alfalfa_Mask;
		float factor = 1.0f;
		float adjFactor = 1.0f;
		
		int [][] rotationD_Data = Layer_Base.getLayer("cdl_2012").getIntData();
		float[][] SOC = Layer_Base.getLayer("SOC").getFloatData();
		
		// Soil_Carbon
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if ((rotationD_Data[y][x] & TotalMask) > 0 /*&& scenario.mSelection.mSelection[y][x] >= 1*/)
				{
					if (rotationD_Data[y][x] == 0 || rotationT_Data[y][x] == 0 || SOC[y][x] <= 0.0f) {
						soilCarbonData[y][x] = -9999.0f;
					}
					else {
						factor = 0.0f;
						
						// ----- CORN to...
						if ((rotationD_Data[y][x] & Corn_Mask) > 0) {
							if ((rotationT_Data[y][x] & Grass_Mask) > 0) {
								factor = RSCCF_Corn_Grass; // ...GRASS
							}
							else if ((rotationT_Data[y][x] & Alfalfa_Mask) > 0) {
								factor = RSCCF_Corn_Alfalfa; // ...ALFALFA
							}
						} // ---- SOY to...
						else if ((rotationD_Data[y][x] & Soy_Mask) > 0) {
							if ((rotationT_Data[y][x] & Grass_Mask) > 0) {
								factor = RSCCF_Soy_Grass; // ...GRASS
							}
							else if ((rotationT_Data[y][x] & Alfalfa_Mask) > 0) {
								factor = RSCCF_Soy_Alfalfa; // ...ALFALFA
							}
						} // ---- ALFALFA to...
						else if ((rotationD_Data[y][x] & Alfalfa_Mask) > 0) {
							if ((rotationT_Data[y][x] & Grass_Mask) > 0) {
								factor = RSCCF_Alfalfa_Grass; // ...GRASS
							} 
							else if ((rotationT_Data[y][x] & Corn_Mask) > 0) {
								factor = -RSCCF_Corn_Alfalfa; // ...CORN
							}
							else if ((rotationT_Data[y][x] & Soy_Mask) > 0) {
								factor = -RSCCF_Soy_Alfalfa; // ...SOY
							}
						} // ---- GRASS to...
						else if ((rotationD_Data[y][x] & Grass_Mask) > 0) {
							if ((rotationT_Data[y][x] & Corn_Mask) > 0) {
								factor = -RSCCF_Corn_Grass; // ...CORN
							}
							else if ((rotationT_Data[y][x] & Soy_Mask) > 0) {
								factor = -RSCCF_Soy_Grass; // ...SOY
							}
							else if ((rotationT_Data[y][x] & Alfalfa_Mask) > 0) {
								factor = -RSCCF_Alfalfa_Grass; // ...ALFALFA
							}
						}
						
						// Calculate equation based on calculated factor and SOC layer
						adjFactor = -0.5938f * (float)(Math.log(SOC[y][x] * 0.1f)) + 1.6524f;
						
						if (adjFactor <= 0.2f) {
							adjFactor = 0.2f;
						}
						else if (adjFactor >= 1.2f) {
							adjFactor = 1.2f;
						}
						
						// Convert the change from 20 years to 1 year
						//soilCarbonData[y][x] = SOC[y][x] + (SOC[y][x] * factor * adjFactor) / 20.0f;
						//soilCarbonData[y][x] = SOC[y][x] * adjFactor + SOC[y][x] * factor * adjFactor;
						
						soilCarbonData[y][x] = SOC[y][x] * (1 + (factor * adjFactor));
						
												
						//if (adjFactor < 0 || factor < 0 || soilCarbonData[y][x] < SOC[y][x])
						//{
							//Logger.info(Float.toString(adjFactor));
							//Logger.info(Float.toString(factor));
							//Logger.info(Float.toString(soilCarbonData[y][x]) + " " + Float.toString(SOC[y][x]));
						//}
					}
				}
				else 
				{
					soilCarbonData[y][x] = -9999.0f;
				}
			}
		}
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		
		results.add(new ModelResult("soc", scenario.mOutputDir, soilCarbonData, width, height));

long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model Soil Carbon is finished - timing: " + Float.toString(timeSec));

		return results;
	}

}
