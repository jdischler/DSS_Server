package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses landscape proportion to calculate pollinator visitation index (Grass and Forest) 
// using 1500m rectangle buffers 
// Visitation Index can vary from 0 to 18, after normalization it goes 0 to 1
// This model is from unpublished work by Ashley Bennett from Michigan State University
// Inputs are proportion of land cover particularly grass and forest, selected cells in the raster map and crop rotation layer 
// ASCII map of visitation index
// Version 08/20/2013
//
//------------------------------------------------------------------------------
public class Model_PollinatorPestSuppression_New extends Model_Base
{
	private static int mWindowSizeMeters = 990;
	private static int mWindowSizeInCells;
	
	private static String mPollinatorModelFile = "pollinator";
	private static String mPestModelFile = "pest";
	
	//--------------------------------------------------------------------------
	public Model_PollinatorPestSuppression_New() {

		mWindowSizeInCells = mWindowSizeMeters / 30; // Number of Cells in Raster Map
	}
	
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario, String destFolder) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(">>> Computing Model Pest/ Pollinator");
long timeStart = System.currentTimeMillis();
		
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		// Grass
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Alfalfa_Mask = cdl.convertStringsToMask("alfalfa");
		int mGrassMask = Grass_Mask | Alfalfa_Mask;	
		// Forest
		int mForestMask = cdl.convertStringsToMask("woodland");
		// Ag
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int mAgMask = 1 + 2 + 8 + 32768 + 131072 + 262144;
		
		// full raster save process...
		float [][] pestData = new float[height][width];
		float [][] pollinatorData = new float[height][width];
		
		Moving_Z_Window zWin = new Moving_Z_Window(mWindowSizeInCells, rotationData, width, height);
		Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();

		boolean moreCells = true;
		while (moreCells) {
			point = zWin.getPoint();
			
			if (zWin.canGetProportions()) {
				float proportionForest = zWin.getProportionForest();
				float proportionGrass = zWin.getProportionGrass();
				
				// Calculate visitation index and normalize value by max
				float pollinatorIndex = (float)Math.pow(0.6617f + (2.98f * proportionForest) 
																+ (1.83f * proportionGrass), 2.0f);
				// Crop type is zero for Ag, Crop type is 1 for grass
				float cropType = 0.0f;
				if ((rotationData[point.mY][point.mX] & Grass_Mask) > 0) {
					cropType = 1.0f;
				}
					
				// Pest suppression calculation
				float pestSuppression = 0.25f + (0.19f * cropType) + (0.62f * proportionGrass);
	
				pollinatorData[point.mY][point.mX] = pollinatorIndex;
				pestData[point.mY][point.mX] = pestSuppression;
			}
			else {
				pollinatorData[point.mY][point.mX] = -9999.0f;
				pestData[point.mY][point.mX] = -9999.0f;
			}
			
			moreCells = zWin.advance();
		}		
	
		List<ModelResult> results = new ArrayList<ModelResult>();
		results.add(new ModelResult("pest", destFolder, pestData, width, height));
		results.add(new ModelResult("pollinator", destFolder, pollinatorData, width, height));
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model_PollinatorPestSuppression_New is finished - timing: " + Float.toString(timeSec));

		return results;
	}	
}

