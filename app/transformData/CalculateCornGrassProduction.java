package util;

import play.*;
import java.io.*;

//------------------------------------------------------------------------------
public class CalculateCornGrassProduction{

	//--------------------------------------------------------------------------
	public void run() {

		// Open source data files...		
		Asc_Reader slope = new Asc_Reader("slope-soil", "Inputs");
		Asc_Reader depth = new Asc_Reader("depth", "Inputs");
		Asc_Reader silt = new Asc_Reader("silt", "Inputs");
		Asc_Reader cec = new Asc_Reader("cec", "Inputs");
		
		// vars to hold each line as an array of strings
		String [] slopeLine = null, depthLine = null, siltLine = null, cecLine = null;
		
		int width = slope.getWidth();
		int height = slope.getHeight();
		
		// Output files...
		PrintWriter cornOut = new HeaderWrite("corn_production", width, height, null).getWriter(); 
		PrintWriter grassOut = new HeaderWrite("grass_production", width, height, null).getWriter(); 

		for (int y = 0; y < height; y++) {
			try {
				slopeLine = slope.getSplitLine();
				depthLine = depth.getSplitLine();
				siltLine = silt.getSplitLine();
				cecLine = cec.getSplitLine();
			}
			catch(Exception e) {
				Logger.error(e.toString());
			}
			
			StringBuffer cornBuffer = new StringBuffer();
			StringBuffer grassBuffer = new StringBuffer();

			for (int x = 0; x < width; x++ ) {
				
				float cornYield = 3.08f - 0.11f * Float.parseFloat(slopeLine[x]) 
													+ 0.02f * Float.parseFloat(depthLine[x]) 
													+ 0.10f * Float.parseFloat(siltLine[x]) 
													+ 0.04f * Float.parseFloat(cecLine[x]);
				cornYield *= 0.0001f * 900.0f;
				
				cornBuffer.append(String.format("%.4f", cornYield));
				if (x < width - 1) { // Need to append a space between data items?
					cornBuffer.append(" ");
				}
				
				float grassYield = 2.20f - 0.07f * Float.parseFloat(slopeLine[x]) 
													+ 0.02f * Float.parseFloat(depthLine[x]) 
													+ 0.07f * Float.parseFloat(siltLine[x]) 
													+ 0.03f * Float.parseFloat(cecLine[x]);
				grassYield *= 0.0001f * 900.0f;
				
				grassBuffer.append(String.format("%.4f", grassYield));
				if (x < width - 1) { // Need to append a space between data items?
					grassBuffer.append(" ");
				}
			}
			
			cornOut.println(cornBuffer.toString());
			grassOut.println(grassBuffer.toString());
		}
		
		// output files closed...
		cornOut.close();
		grassOut.close();
		
		// input files closed...
		slope.close();
		depth.close();
		silt.close();
		cec.close();
	};
}

