package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

// FIXME: TODO: The min/max range is currently only calculated based on the SELECTION...
//	The entire landscape variant should find a min/max range for the whole landscape....

//Returned Json
/*
 results {
 	landscape {
 		range {
 			min
 			max
 		}
 		file1 {
 			sum
 			count
 			graph
 		}
 		file2 {
 			sum
 			count
 			graph
 		}
 	}
 	selection {
 		range {
 			min
 			max
 		}
 		file1 {
 			sum
 			count
 			graph
 		}
 		file2 {
 			sum
 			count
 			graph
 		}
 	}
*/

//------------------------------------------------------------------------------
// Analyzer_Histogram
//
// This program selects cells in the raster map based on user request 
// Inputs are a selection and ONE of the following data sources:
//	1) two FILES to compare - results are read into memory
//	2) one FILE and one IN-MEMORY dataset to compare
//	3) two IN-MEMORY datasets to compare
// 
// Outputs are an array that hold the selected cells
//
//------------------------------------------------------------------------------
public class Analyzer_HistogramNew
{
	private final static int DEFAULT_histogramSize = 15;
	
	// Internal helper class...
	//--------------------------------------------------------------------------
	private class MinMax {
		public float mMin, mMax;

		public MinMax() {
			reset();
		}
		
		public MinMax(float min, float max) {
			mMin = min;
			mMax = max;
		}
		
		public void reset() {
			mMin = Float.MAX_VALUE;
			mMax = -Float.MAX_VALUE;
		}
		
		public void combine(float min, float max) {
			mMin = min(mMin, min);
			mMax = max(mMax, max);		
		}
		
		public JsonNode toJson() {
			ObjectNode obj = JsonNodeFactory.instance.objectNode();
			obj.put("min", mMin);
			obj.put("max", mMax);
			return obj;
		}
	}

	// Internal helper class...
	//--------------------------------------------------------------------------
	private class Histogram {
		
		public int mSize;
		public int mTotalCountFile;
		public double mTotalSumFile;
		public int [] mGraph;
		
		public Histogram(int size) {
			mSize = size;
			mGraph = new int [size];
			mTotalCountFile = 0;
			mTotalSumFile = 0.0;
		}
		
		public void reset() {
			mTotalCountFile = 0;
			mTotalSumFile = 0.0;
			for (int i = 0; i < mSize; i++) {
				mGraph[i] = 0;
			}
		}
		
		public JsonNode toJson() {
			ObjectNode obj = JsonNodeFactory.instance.objectNode();
			
			// Convert java Histogram bins to JSON Array Node
			ArrayNode jsonHistogramFile = JsonNodeFactory.instance.arrayNode();
			for (int i = 0; i < mSize; i++) {
				jsonHistogramFile.add(mGraph[i]);
			}
			
			obj.put("graph", jsonHistogramFile);
			obj.put("sum", mTotalSumFile);
			obj.put("count", mTotalCountFile);
			
			return obj;
		}
	}

	private MinMax mSelectionMinMax, mLandscapeMinMax;
	
	private Selection mSelection1, mSelection2;
	private Histogram mSelection_File1, mSelection_File2;

	private Histogram mLandscape_File1, mLandscape_File2;

	// temp internal scratch memory as needed...
	private float [][] mFile1Data, mFile2Data;
	
	// Create histogram that generates our typical binning size	
	//--------------------------------------------------------------------------
	public Analyzer_HistogramNew() {
	
		this(DEFAULT_histogramSize);
	}
	
	//--------------------------------------------------------------------------
	public Analyzer_HistogramNew(int histogramBinCount) {
		mLandscape_File1 = new Histogram(histogramBinCount);
		mLandscape_File2 = new Histogram(histogramBinCount);
		mSelection_File1 = new Histogram(histogramBinCount);
		mSelection_File2 = new Histogram(histogramBinCount);
		
		mFile1Data = null; mFile2Data = null;
		mSelectionMinMax = new MinMax();
		mLandscapeMinMax = new MinMax();
	}
	
	//--------------------------------------------------------------------------
	private final static float min(float min, float num) { 
		if (num < min) {
			return num;
		}
		return min;
	}
	
	//--------------------------------------------------------------------------
	private final static float max(float max, float num) {
		if (num > max) {
			return num;
		}
		return max;
	}

	//--------------------------------------------------------------------------
	private void reset() {
		mLandscape_File1.reset();
		mLandscape_File2.reset();
		mSelection_File1.reset();
		mSelection_File2.reset();
		mSelectionMinMax.reset();
		mLandscapeMinMax.reset();
	}
	
	//--------------------------------------------------------------------------
	private void loadFileData(File file, Selection selectionForFile, boolean assignToFile1_Data) {
		
		Binary_Reader fileReader = new Binary_Reader(file);
		if (fileReader.readHeader()) {
			int width = fileReader.getWidth(),
				height = fileReader.getHeight();
				
			float [][] rasterData = null;
			if (assignToFile1_Data) {
				if (mFile1Data == null) {			
					mFile1Data = new float[height][width];
				}
				rasterData = mFile1Data;
			} else {
				if (mFile2Data == null) {			
					mFile2Data = new float[height][width];
				}
				rasterData = mFile2Data;
			}
		
			for (int y = 0; y < height; y++) {
				ByteBuffer buff = fileReader.readLine();
				if (buff != null) {
					for (int x=0; x < width; x++) {
						float data = buff.getFloat(x * 4); // blah, 4 = size of float, ie 32bit
						rasterData[y][x] = data;
					}
				}
			}
			fileReader.close();
			findRange(width, height, rasterData, selectionForFile);
		}
	}
	
	// Finds the min/max (range of values) for the selected pixels in rasterData AND
	//	then also for the entire landscape...
	//--------------------------------------------------------------------------
	private void findRange(int width, int height, float [][] rasterData, Selection selection) {
	
		float selMin = Float.MAX_VALUE, selMax = -Float.MAX_VALUE;
		float landMin = Float.MAX_VALUE, landMax = -Float.MAX_VALUE; 

		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				float data = rasterData[y][x];
				if (data > -9999.0f || data < -9999.1f) { // FIXME: NoData check...
					if (selection.mRasterData[y][x] >= 1) {
						selMin = min(selMin, data);
						selMax = max(selMax, data);
					}
					landMin = min(landMin, data);
					landMax = max(landMax, data);
				}
			}
		}
		
		mSelectionMinMax.combine(selMin, selMax);
		mLandscapeMinMax.combine(landMin, landMax);
	}
	
	// Creates bins for both the selected cells and the entire landscape... 
	//--------------------------------------------------------------------------
	private void binify(float [][] rasterData, Selection selection, 
							Histogram histForSelection, Histogram histForLandscape) {
		
		int width = selection.mWidth, height = selection.mHeight;
		int binIndex = 0;
		
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
				float data = rasterData[y][x];
				if (data > -9999.0f || data < -9999.1f) { // FIXME: NoData check...
					
					if (selection.mRasterData[y][x] >= 1) {
						// handle custom, but usually rare, case of data being MAX in the above formula
						binIndex = (int)((data - mSelectionMinMax.mMin) /
											(mSelectionMinMax.mMax - mSelectionMinMax.mMin) * 
											histForSelection.mSize);
						if (binIndex > histForSelection.mSize - 1) { 
							binIndex = histForSelection.mSize - 1; // clamp index to max to prevent overflow
						}
						histForSelection.mGraph[binIndex]++;
						histForSelection.mTotalCountFile++;
						histForSelection.mTotalSumFile += data;
					}
					// handle custom, but usually rare, case of data being MAX in the above formula
					binIndex = (int)((data - mLandscapeMinMax.mMin) /
										(mLandscapeMinMax.mMax - mLandscapeMinMax.mMin) * 
										histForSelection.mSize);
					if (binIndex > histForSelection.mSize - 1) { 
						binIndex = histForSelection.mSize - 1; // clamp index to max to prevent overflow
					}
					histForLandscape.mGraph[binIndex]++;
					histForLandscape.mTotalCountFile++;
					histForLandscape.mTotalSumFile += data;
				}
			}
		}
	}

	//--------------------------------------------------------------------------
	private JsonNode prepareReturnResults() {
		
		// 3rd step, package up the data into a JSON object to ship back to the client
		//	Define Json to store and return data to client
		//--------------------------------
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		ObjectNode landscape = JsonNodeFactory.instance.objectNode();
		ObjectNode sel = JsonNodeFactory.instance.objectNode();
		
		landscape.put("file1", mLandscape_File1.toJson());
		landscape.put("file2", mLandscape_File2.toJson());
		landscape.put("range", mLandscapeMinMax.toJson());
		obj.put("landscape", landscape);
		
		sel.put("file1", mSelection_File1.toJson());
		sel.put("file2", mSelection_File2.toJson());
		sel.put("range", mSelectionMinMax.toJson());
		obj.put("selection", sel);
		
		return obj;
	}
	
	// COMPARE a FILE to another FILE - NOTE: sel1 and sel2 can be the same selection if needed
	//--------------------------------------------------------------------------
	public JsonNode run(File file1, Selection sel1, File file2, Selection sel2) {

		reset();

		loadFileData(file1, sel1, true);
		loadFileData(file2, sel2, false);
		
		binify(mFile1Data, sel1, mSelection_File1, mLandscape_File1);
		binify(mFile2Data, sel2, mSelection_File2, mLandscape_File2);
		
		return prepareReturnResults();
	}
	
	// COMPARE a FILE to in-memory data - NOTE: sel1 and sel2 can be the same selection if needed
	//--------------------------------------------------------------------------
	public JsonNode run(File file1, Selection sel1, 
							int width, int height, float [][] data2, Selection sel2) {
		reset();
		
		loadFileData(file1, sel1, true);
		findRange(width, height, data2, sel2);
		
		binify(mFile1Data, sel1, mSelection_File1, mLandscape_File1);
		binify(data2, sel2, mSelection_File2, mLandscape_File2);
		
		return prepareReturnResults();
	}

	// COMPARE in-memory results to in-memory results - NOTE: sel1 and sel2 can be the same selection if needed
	//--------------------------------------------------------------------------
	public JsonNode run(int width, int height, float [][] data1, Selection sel1,
							float [][] data2, Selection sel2) {
		reset();
		
		findRange(width, height, data1, sel1);
		findRange(width, height, data2, sel2);
		
		binify(data1, sel1, mSelection_File1, mLandscape_File1);
		binify(data2, sel2, mSelection_File2, mLandscape_File2);
		
		return prepareReturnResults();
	}
}

