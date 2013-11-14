package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

//------------------------------------------------------------------------------
public class Analyzer_Heatmap {
	
	public static class MinMax {
		
		public float mMin, mMax;
		public MinMax(float min, float max) {
			mMin = min; mMax = max;
		}
	}
	
	//--------------------------------------------------------------------------
	private static ObjectNode copyPaletteForClient(PngChunkPLTE palette, int numColors, ObjectNode sendBack) {
		
		ArrayNode colorArray = JsonNodeFactory.instance.arrayNode();
		
		int[] rgb = new int[3];
		
		for (int i=0; i < numColors; i++) {
			String color = "#";
			palette.getEntryRgb(i, rgb);
			for (int t=0; t<3; t++) {
				String hex = Integer.toHexString(rgb[t]);
				// prefill with zeroes..
				while(hex.length() < 2) {
					hex = "0" + hex;
				}
				color += hex;
			}
			colorArray.add(color);
		}
		
		sendBack.put("palette", colorArray);
		return sendBack;
	}

	//--------------------------------------------------------------------------
	private static ObjectNode copyQuantizedValuesForClient(float min, float max,
								int [] quantiledBins, int quantileBinCt,
								int entries, ObjectNode sendBack) {

		ArrayNode valueArray = JsonNodeFactory.instance.arrayNode();

		// we'll start at the min value and step along by the addPerBin.
		float addPerBin = (max - min) / quantileBinCt;
		
		// Once the palIndex increases in the quantiledBin, we know we've hit a transition point, so dump
		//	the running value out to the array
		int palIndex = 0;

		valueArray.add(min);
		
		for (int idx=0; idx < quantileBinCt; idx++) {
			if (quantiledBins[idx] > palIndex) {
				valueArray.add(min + addPerBin * idx);
				palIndex = quantiledBins[idx];
			}
		}
		
		valueArray.add(max);
		
		sendBack.put("values", valueArray);
		return sendBack;
	}

	//--------------------------------------------------------------------------
	private static ObjectNode copyValuesForClient(MinMax mm, int entries, ObjectNode sendBack) {
		
		float min = mm.mMin, max = mm.mMax;
		
		ArrayNode valueArray = JsonNodeFactory.instance.arrayNode();
		
		for (int i=0; i < entries; i++) {
			float value = (float)(min + i * ((max - min) / entries));
			valueArray.add(value);
		}
		
		valueArray.add(max);
		
		sendBack.put("values", valueArray);
		return sendBack;
	}
	
	// TWO FILE DELTA STYLE MAP
	// downsampleFactor: how much to scale the image down, e.g. 10 generates an image of:
	//	sourceWidth/10, sourceHeight/10
	//--------------------------------------------------------------------------
	public static ObjectNode runEqualInterval(File file1, File file2, File outputFile, int downsampleFactor) {
		
		final int numPaletteEntries = 7;
		
		Binary_Reader fileReader_1 = new Binary_Reader(file1);
		Binary_Reader fileReader_2 = new Binary_Reader(file2);
		if (!fileReader_1.readHeader() || !fileReader_2.readHeader()) {
			Logger.info("Heatmap generation unable to open one or the other files, and/or read the header");
			return null;
		}
		
		int width = fileReader_1.getWidth(), height = fileReader_1.getHeight();
		if (width != fileReader_2.getWidth() || height != fileReader_2.getHeight()) {
			Logger.info("Heatmap generation failed because the width and/or height" + 
					" of the input files does not match!");
			return null;
		}
		
		float heatmap[][] = new float[height][width];
		
		for (int y=0; y < height; y++) {
			ByteBuffer buff_1 = fileReader_1.readLine();
			ByteBuffer buff_2 = fileReader_2.readLine();
			if (buff_1 != null && buff_2 != null) {
				for (int x=0; x < width; x++) {
					float data_1 = buff_1.getFloat(x * 4),
							data_2 = buff_2.getFloat(x * 4);
					if (data_1 > -9999.0f && data_2 > -9999.0f) {
						heatmap[y][x] = data_2 - data_1;
					}
					else {
						heatmap[y][x] = -9999.0f;
					}
				}
			}
		}
			
		int newWidth = width/downsampleFactor;
		int newHeight = height/downsampleFactor;
		
		float resampled[][] = Downsampler.generateAveraged(heatmap, width, height, newWidth, newHeight);
		MinMax minMax = getMinMax(resampled, newWidth, newHeight);
		float min = minMax.mMin, max = minMax.mMax;
		
		byte[][] idx = convertToIndexed(resampled, newWidth, newHeight, minMax, numPaletteEntries);
	
		Logger.info("Creating png");
		Png png = new Png(newWidth, newHeight, 8, 1, outputFile.getPath());
	
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		sendBack = copyValuesForClient(minMax, numPaletteEntries, sendBack);
		sendBack = createPalette(png, numPaletteEntries, minMax, sendBack);
		
		png.mPngWriter.writeRowsByte(idx);
		png.mPngWriter.end();
		
		return sendBack;
	}

	// ONE FILE ABSOLUTE STYLE MAP
	// downsampleFactor: how much to scale the image down, e.g. 10 generates an image of:
	//	sourceWidth/10, sourceHeight/10
	//--------------------------------------------------------------------------
	public static ObjectNode runAbsolute(File file, File outputFile, int downsampleFactor) {
		
		final int numPaletteEntries = 7;
		
		Binary_Reader fileReader = new Binary_Reader(file);
		if (!fileReader.readHeader()) {
			Logger.info("Heatmap generation unable to open the file and/or read the header");
			return null;
		}
		
		int width = fileReader.getWidth(), height = fileReader.getHeight();
		
		float heatmap[][] = new float[height][width];
		
		for (int y=0; y < height; y++) {
			ByteBuffer buff = fileReader.readLine();
			if (buff != null) {
				for (int x=0; x < width; x++) {
					heatmap[y][x] = buff.getFloat(x * 4);
				}
			}
		}
			
		int newWidth = width/downsampleFactor;
		int newHeight = height/downsampleFactor;
		
		float resampled[][] = Downsampler.generateAveraged(heatmap, width, height, newWidth, newHeight);
		MinMax minMax = getMinMax(resampled, newWidth, newHeight);
		float min = minMax.mMin, max = minMax.mMax;
		
		byte[][] idx = convertToIndexed(resampled, newWidth, newHeight, minMax, numPaletteEntries);
	
		Logger.info("Creating png");
		Png png = new Png(newWidth, newHeight, 8, 1, outputFile.getPath());
	
		Logger.info("Creating palette");
		PngChunkPLTE palette = null;
		try {
			palette = png.createPalette(numPaletteEntries + 1); // +1 for transparent color
		}
		catch(Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info("Setting palette entries");
		palette.setEntry(0, 255, 210, 170);	// orangish
		Png.interpolatePaletteEntries(palette, 
				1, 255, 255, 204,			// yellowish
				3, 160, 218, 180);			// greenish
		palette.setEntry(4, 65, 182, 196); 	// greenish blue
		palette.setEntry(5, 34, 94, 168); 	// blueish
		palette.setEntry(6, 106, 21, 160);	// blueish purple
		palette.setEntry(7, 255, 0, 0);		// red, but transparent

		Logger.info(" Setting transparent");
		int[] alpha = new int[numPaletteEntries + 1];
		
		for (int i=0; i < numPaletteEntries; i++) {
			alpha[i] = 255;
		}
		// last color is always transparent
		alpha[numPaletteEntries] = 0;
		png.setTransparentArray(alpha);
		
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		sendBack = copyPaletteForClient(palette, numPaletteEntries, sendBack);
		sendBack = copyValuesForClient(minMax, numPaletteEntries, sendBack);
		
		png.mPngWriter.writeRowsByte(idx);
		png.mPngWriter.end();
		
		return sendBack;
	}

	// TWO FILE DELTA STYLE MAP -- QUANTILED
	// downsampleFactor: how much to scale the image down, e.g. 10 generates an image of:
	//	sourceWidth/10, sourceHeight/10
	//--------------------------------------------------------------------------
	public static ObjectNode runQuantile(File file1, File file2, File outputFile, int downsampleFactor) {
		
		final int numPaletteEntries = 7;
		
		Binary_Reader fileReader_1 = new Binary_Reader(file1);
		Binary_Reader fileReader_2 = new Binary_Reader(file2);
		if (!fileReader_1.readHeader() || !fileReader_2.readHeader()) {
			Logger.info("Heatmap generation unable to open one or the other files, and/or read the header");
			return null;
		}
		
		int width = fileReader_1.getWidth(), height = fileReader_1.getHeight();
		if (width != fileReader_2.getWidth() || height != fileReader_2.getHeight()) {
			Logger.info("Heatmap generation failed because the width and/or height" + 
					" of the input files does not match!");
			return null;
		}
		
		float heatmap[][] = new float[height][width];
		
		for (int y=0; y < height; y++) {
			ByteBuffer buff_1 = fileReader_1.readLine();
			ByteBuffer buff_2 = fileReader_2.readLine();
			if (buff_1 != null && buff_2 != null) {
				for (int x=0; x < width; x++) {
					float data_1 = buff_1.getFloat(x * 4),
							data_2 = buff_2.getFloat(x * 4);
					if (data_1 > -9999.0f && data_2 > -9999.0f) {
						heatmap[y][x] = data_2 - data_1;
					}
					else {
						heatmap[y][x] = -9999.0f;
					}
				}
			}
		}
			
		int newWidth = width/downsampleFactor;
		int newHeight = height/downsampleFactor;
		
		float resampled[][] = Downsampler.generateMax(heatmap, width, height, newWidth, newHeight);
		MinMax minMax = getMinMax(resampled, newWidth, newHeight);
		float min = minMax.mMin, max = minMax.mMax;
		
		Logger.info(" ...Calculating Quantile...");
		int binCount = 200; // higher values should (theoretically) yield more accurate divisions...
		
		// create and zero bins...
		int [] bins = new int[binCount]; 
		int totalCells = 0;
		
		for (int i=0; i < binCount; i++) {
			bins[i] = 0;
		}
		// count values for each bin so we get a histogram...
		for (int y=0; y < newHeight; y++) {
			for (int x=0; x < newWidth; x++) {
				float data = resampled[y][x];
				if (data > -9999.0f) { // FIXME: NoData check...
					int binIndex = (int)((data - min)/(max - min) * binCount);
					if (binIndex > binCount - 1) binIndex = binCount - 1;
					bins[binIndex]++;
					totalCells++;
				}
			}
		}

		// What is an even distribution?..divide and round...
		int desiredCountPerQuantile = (int)((float)totalCells / numPaletteEntries + 0.5f);
		
		// Now clump our bins together so the sum of a string of adjacent bins will be 
		//	roughly equal to our even distribution..
		// stuff a remapped index to a color palette as we go so we can easily figure out color assignments...
		int countTracker = 0;
		int indexRemapperTracker = 0;
		for (int i=0; i < binCount; i++) {
			countTracker += bins[i];
			bins[i] = indexRemapperTracker;// overwrite the count in the bin, reusing it to store a palette index remapper
			// does the current running count for bins put us where we need to be?
			// we should also check if adding the next bin will put us over by too far...
			// We do a look-ahead by one bin so we don't get stuck adding way too many counts in case that
			//	next bin were much bigger than normal...
			if (countTracker >= desiredCountPerQuantile ||  
				(i < binCount - 1 && countTracker + bins[i+1] / 2 > desiredCountPerQuantile)) {
					// step to next index for the remapper...
					indexRemapperTracker++;
					countTracker -= desiredCountPerQuantile; // track leftovers/remainders..
			}
		}

		Logger.info(" ...Generating IDX array");

		byte[][] idx = new byte[newHeight][newWidth];

		// Now generate the heatmap image
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				float data = resampled[y][x];
				if (data > -9999.0f) {
					int index = (int)((data - min)/(max - min) * binCount);
					if (index > binCount - 1) index = binCount - 1;
					int palIndex = bins[index];
					if (palIndex < 0) palIndex = 0;
					else if (palIndex > numPaletteEntries - 1) palIndex = numPaletteEntries - 1; 
					idx[y][x] = (byte)(palIndex);
				}
				else {
					idx[y][x] = numPaletteEntries; // last color is transparent color
				}
			}
		}


		Logger.info("Creating png");
		Png png = new Png(newWidth, newHeight, 8, 1, outputFile.getPath());
	
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		sendBack = copyQuantizedValuesForClient(min, max, bins, binCount, numPaletteEntries, sendBack);
		sendBack = createPalette(png, numPaletteEntries, minMax, sendBack);
	
		png.mPngWriter.writeRowsByte(idx);
		png.mPngWriter.end();
		
		return sendBack;
	}

	//--------------------------------------------------------------------------
	private static MinMax getMinMax(float data[][], int width, int height) {
	
		MinMax minMax = new MinMax(0,0);
		boolean bHasMinMax = false;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = data[y][x];
				if (val > -9999.0f) {
					if (!bHasMinMax) {
						bHasMinMax = true;
						minMax.mMax = val;
						minMax.mMin = val;
					}
					if (val > minMax.mMax) {
						minMax.mMax = val;
					}
					else if (val < minMax.mMin) {
						minMax.mMin = val;
					}
				}
			}
		}
		
		Logger.info(" Min: " + Float.toString(minMax.mMin) + " Max: " + Float.toString(minMax.mMax));
		return minMax;
	}
	
	//--------------------------------------------------------------------------
	private static byte[][] convertToIndexed(float data[][], int width, int height, MinMax mm, int numVals) {
		
		Logger.info("Generating IDX array");
		
		byte[][] idxs = new byte[height][width];
		float min = mm.mMin, max = mm.mMax;
		
		// Now generate the heatmap image
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				float val = data[y][x];
				if (val > -9999.0f) {
					int bin = (int)((val - min) / (max - min) * numVals);
					if (bin < 0) bin = 0;
					else if (bin > numVals - 1) bin = numVals - 1;
					idxs[y][x] = (byte)(bin);
				}
				else {
					idxs[y][x] = (byte)numVals; // transparent
				}
			}
		}
		
		return idxs;
	}

	//--------------------------------------------------------------------------
	private static ObjectNode createPalette(Png png, int numColors, MinMax minMax, ObjectNode sendBack) {
		
		Logger.info("Creating palette");
		PngChunkPLTE palette = null;
		try {
			palette = png.createPalette(numColors + 1); // extra one for the transparent color
		}
		catch(Exception e) {
			Logger.info(e.toString());
		}
		Logger.info("Setting palette entries");
		
		if (minMax.mMax < 0.1f) {		// entire value range negative?
			palette.setEntry(0, 60, 0, 22); // dark magenta
			palette.setEntry(1, 100, 8, 50); 
			palette.setEntry(2, 170, 12, 88); // magenta
			palette.setEntry(3, 240, 32, 116); 
			palette.setEntry(4, 255, 136, 187); // pink
			palette.setEntry(5, 255, 200, 220);
			palette.setEntry(6, 255, 255, 255); // white
		}
		else if (minMax.mMin > -0.1f) {	// entire value range positive?
			palette.setEntry(0, 255, 255, 255); // white
			palette.setEntry(1, 210, 255, 160);
			palette.setEntry(2, 145, 235, 80); // lime green
			palette.setEntry(3, 90, 178, 10); 
			palette.setEntry(4, 50, 110, 10); // green
			palette.setEntry(5, 30, 70, 40); 
			palette.setEntry(6, 15, 30, 90); // dark blue
		}
		else {
			Png.RGB [] colors = new Png.RGB[13]; 
			int whiteIndex = 6;
			int zeroIndex = 0; // bin where the zero value hides...
			JsonNode node = sendBack.get("values");
			if (!node.isArray()) {
				Logger.info("blah, fixme....");
			}
			ArrayNode array = (ArrayNode)node;
			for (int i=0; i < array.size(); i++) {
				zeroIndex = i;
				double res1 = array.get(i).doubleValue();
				if (res1 >= 0) {
					break;
				}
				if (i < array.size() - 1) {
					double res2 = array.get(i+1).doubleValue();
					if (res2 > 0) {
						break;
					}
				}
			}
			
			colors[0] = new Png.RGB(60, 0, 22); // dark magenta
			colors[1] = new Png.RGB(100, 8, 50); 
			colors[2] = new Png.RGB(170, 12, 88); // magenta
			colors[3] = new Png.RGB(240, 32, 116); 
			colors[4] = new Png.RGB(255, 136, 187); // pink
			colors[5] = new Png.RGB(255, 200, 220);
			colors[whiteIndex] = new Png.RGB(255, 255, 255); // white
			colors[7] = new Png.RGB(210, 255, 160);
			colors[8] = new Png.RGB(145, 235, 80); // lime green
			colors[9] = new Png.RGB(90, 178, 10); 
			colors[10] = new Png.RGB(50, 110, 10); // green
			colors[11] = new Png.RGB(30, 70, 40); 
			colors[12] = new Png.RGB(15, 30, 90); // dark blue
			
			for (int i=0; i < numColors; i++) {
				png.setColor(palette, i, colors[i + (whiteIndex - zeroIndex)]);
			}
		}

		// last color is always transparent color...doesn't matter what color here, we set alpha zero below...
		palette.setEntry(numColors, 255, 0, 0);
		sendBack = copyPaletteForClient(palette, numColors, sendBack);
		
		Logger.info(" Setting transparent");
		int[] alpha = new int[numColors + 1];
		
		for (int i=0; i < numColors; i++) {
			alpha[i] = 255;
		}
		// last color is always transparent
		alpha[numColors] = 0;
		png.setTransparentArray(alpha);
		
		return sendBack;
	}

}

