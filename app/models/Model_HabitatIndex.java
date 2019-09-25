package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//------------------------------------------------------------------------------
public class Model_HabitatIndex extends Model_Base
{
	private static int mWindowSizeMeters = 390;
	private static int mWindowSizeInCells;
	private static String mModelFile = "habitat_index";
	
	//--------------------------------------------------------------------------
	public Model_HabitatIndex() {
		
		mWindowSizeInCells = mWindowSizeMeters / 30; // Number of Cells in Raster Map
	}
	
	// Define habitat index function
	//--------------------------------------------------------------------------
	public List<ModelResult> run(Scenario scenario) {

		int[][] rotationData = scenario.mNewRotation;
		int width = scenario.getWidth(), height = scenario.getHeight();
		
Logger.info(">>> Computing Model Habitat Index");
		
		float [][] habitatData = new float[height][width];
Logger.info("  > Allocated memory for Habitat Index");
		
		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Alfalfa_Mask = cdl.convertStringsToMask("alfalfa");
		int mGrassMask = Grass_Mask | Alfalfa_Mask;	
		// Forest
		int mForestMask = cdl.convertStringsToMask("woodland");
		// Ag
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		int mAgMask = Corn_Mask | Soy_Mask;
		
		int TotalMask = mAgMask | mGrassMask;
		
		// --- Model specific code starts here
		Moving_Z_Window zWin = new Moving_Z_Window(mWindowSizeInCells, rotationData, width, height);
		
		boolean moreCells = true;
		while (moreCells) {
			Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();
			
			// If proportions are zero, don't try to get them because we'd divide by zero in doing that.
			if ((rotationData[point.mY][point.mX] & TotalMask) > 0 && zWin.canGetProportions()) {
				float proportionAg = zWin.getProportionAg();
				float proportionGrass = zWin.getProportionGrass();
				
				// Habitat Index
				float lambda = -4.47f + (2.95f * proportionAg) + (5.17f * proportionGrass); 
				float habitatIndex = (float)((1.0f / (1.0f / Math.exp(lambda) + 1.0f )) / 0.67f);

				habitatData[point.mY][point.mX] = habitatIndex;
			}
			else {
				habitatData[point.mY][point.mX] = -9999.0f; // NO DATA
			}
			
			moreCells = zWin.advance();
		}		
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		results.add(new ModelResult("habitat_index", scenario.mOutputDir, habitatData, width, height));
		
		return results;
	}	
}
