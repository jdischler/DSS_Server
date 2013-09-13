package util;

import play.*;
import java.util.*;
import java.io.*;

//------------------------------------------------------------------------------
public class Model_Biomass // Ethanol?
{
	private PrintWriter mFileOut;
	
	private int mCornMask, mGrassMask;
	private int mWidth, mHeight;
	private String mNoDataString;
	
	//--------------------------------------------------------------------------
	public Model_Biomass() {
		
		mCornMask = Layer_Integer.convertIndicesToMask(1);
		mGrassMask = Layer_Integer.convertIndicesToMask(8, 9);
		
		mWidth = Layer_Base.getLayer("Rotation").getWidth();
		mHeight = Layer_Base.getLayer("Rotation").getHeight();

		int NO_DATA = -9999;
		mNoDataString = Integer.toString(NO_DATA);
	}
	
/* 	// Only used for models that do not require some sort of intermediate or pre-processed
	// results. I.e., this function should only be used if the model calculation is
	//	truly stand-alone.
	public void run(int[][] rotationData) {
		
	}
*/
	//--------------------------------------------------------------------------
	public void initForLineProcessing(String folderOut) {
		
		Logger.info("Preparing to write Ethanol file");
		mFileOut = new HeaderWrite("Ethanol", mWidth, mHeight, folderOut).getWriter();
	}
	
	//--------------------------------------------------------------------------
	public void processLine(int lineY, int width, int[][] rotationData, float[][] providedData)
	{
		StringBuffer out = new StringBuffer();
		
		for (int x = 0; x < width; x++) {				
			if (rotationData[lineY][x] > 0) {
				float eth = 0;

				if ((rotationData[lineY][x] & mCornMask) > 0) {
					eth = providedData[DataProvider_CornGrass.CORN_IDX][x] * 0.5f * 0.4f * 1000.0f 
						+ providedData[DataProvider_CornGrass.CORN_IDX][x] * 0.25f * 0.38f * 1000.0f;
				}
				else if ((rotationData[lineY][x] & mGrassMask) > 0) {
					eth = providedData[DataProvider_CornGrass.GRASS_IDX][x] * 0.38f * 1000.0f;
				}

//				out.append(String.format("%.3f", eth));
			}
			else {
				// Emit NO DATA value
				out.append(mNoDataString);
			}
				
			if (x != width - 1) {
				out.append(" ");
			}
		}
		mFileOut.println(out.toString());
	}
	
	//--------------------------------------------------------------------------
	public void finishLineProcessing() {

		mFileOut.close();
		Logger.info("Finished writing Ethanol file");
	}
}

