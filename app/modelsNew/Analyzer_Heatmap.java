package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

//------------------------------------------------------------------------------
public class Analyzer_Heatmap {
	
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
	private static ObjectNode copyValuesForClient(float max, int entries, ObjectNode sendBack) {
		
		ArrayNode valueArray = JsonNodeFactory.instance.arrayNode();
		
		int mid = entries / 2;
		for (int i=0; i < entries; i++) {
			float value = (float)(i - mid) / (float)mid * max;
			valueArray.add(value);
		}
		
		sendBack.put("values", valueArray);
		return sendBack;
	}

	//--------------------------------------------------------------------------
	private static ObjectNode copyAbsoluteValuesForClient(float min, float max, int entries, ObjectNode sendBack) {
		
		ArrayNode valueArray = JsonNodeFactory.instance.arrayNode();
		
		for (int i=0; i < entries; i++) {
			float value = (float)(min + i * ((max - min) / entries));
			valueArray.add(value);
		}
		
		sendBack.put("values", valueArray);
		return sendBack;
	}
	
	// TWO FILE DELTA STYLE MAP
	// downsampleFactor: how much to scale the image down, e.g. 10 generates an image of:
	//	sourceWidth/10, sourceHeight/10
	//--------------------------------------------------------------------------
	public static ObjectNode run(File file1, File file2, File outputFile, int downsampleFactor) {
		
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
			
		Downsampler downSample = new Downsampler(heatmap, width, height);
		
		int newWidth = width/downsampleFactor;
		int newHeight = height/downsampleFactor;
		
		float resampled[][] = downSample.generateMax(newWidth, newHeight);
		float max = 0;
		boolean mbHasMax = false;
		
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				float data = resampled[y][x];
				if (data > -9999.0f) {
					if (!mbHasMax) {
						max = data;
						mbHasMax = true;
					}
					if (Math.abs(data) > max) {
						max = Math.abs(data);
					}
				}
			}
		}
		
		Logger.info(" Max: " + Float.toString(max));
		Logger.info("Generating IDX array");

		byte[][] idx = new byte[newHeight][newWidth];

		// Now generate the heatmap image
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				if (resampled[y][x] > -9999.0f) {
					float delta = (resampled[y][x] / max) * 3.0f + 3.5f; // round
					if (delta < 0) delta = 0;
					else if (delta > 6) delta = 6;
					idx[y][x] = (byte)(delta);
				}
				else {
					idx[y][x] = numPaletteEntries; // last color is transparent color
				}
			}
		}
	
		Logger.info("Creating png");
//		File path = new File(outputFile.getPath());
//		path.mkdirs(); // make any necessary directories...
		Png png = new Png(newWidth, newHeight, 8, 1, outputFile.getPath());
	
		Logger.info("Creating palette");
		PngChunkPLTE palette = null;
		try {
			palette = png.createPalette(numPaletteEntries + 1); // extra one for the transparent color
		}
		catch(Exception e) {
			Logger.info(e.toString());
		}
		Logger.info("Setting palette entries");
		
		/*Png.interpolatePaletteEntries(palette, 
				0, 0, 102, 190,		// aqua blue
				3, 255, 255, 255);	// white
		Png.interpolatePaletteEntries(palette, 
				3, 255, 255, 255,	// white
				6, 190, 81, 10);	// brown
		*/
		palette.setEntry(0, 240, 16, 116); // magenta
		palette.setEntry(1, 255, 136, 187);
		palette.setEntry(2, 255, 210, 229);
		palette.setEntry(3, 255, 255, 255); // white
		palette.setEntry(4, 212, 255, 164);
		palette.setEntry(5, 155, 245, 90);
		palette.setEntry(6, 90, 178, 0); // lime green
		palette.setEntry(7, 255, 0, 0); // transparent color
/*		Png.interpolatePaletteEntries(palette, 
				3, 255, 255, 255,	// white
				6, 128, 255, 32);	// limey-green
/
/*		Png.interpolatePaletteEntries(palette, 
			0, 128, 0, 255,		// purple
			2, 255, 0, 0);		// red
		Png.interpolatePaletteEntries(palette, 
			2, 255, 0, 0,		// red
			4, 255, 255, 0);	// yellow
		Png.interpolatePaletteEntries(palette, 
			3, 255, 255, 0,		// yellow
			4, 0, 255, 0);		// green
		Png.interpolatePaletteEntries(palette, 
			4, 0, 255, 0,		// green
			6, 0, 64, 255);		// blue
*/
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		sendBack = copyPaletteForClient(palette, numPaletteEntries, sendBack);
		sendBack = copyValuesForClient(max, numPaletteEntries, sendBack);
		
		Logger.info("Setting transparent");
		// set index 4 as transparent
		int[] alpha = new int[numPaletteEntries + 1];
		alpha[0] = 255; alpha[1] = 255; alpha[2] = 255;
		alpha[3] = 255;
		alpha[4] = 255; alpha[5] = 255; alpha[6] = 255;
		alpha[7] = 0; // transparent color
		png.setTransparentArray(alpha);
		
		png.mPngWriter.writeRowsByte(idx);
		png.mPngWriter.end();
		
		return sendBack;
	}

	// ONE FILE ABSOLUTE STYLE MAP
	// downsampleFactor: how much to scale the image down, e.g. 10 generates an image of:
	//	sourceWidth/10, sourceHeight/10
	//--------------------------------------------------------------------------
	public static ObjectNode run(File file, File outputFile, int downsampleFactor) {
		
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
			
		Downsampler downSample = new Downsampler(heatmap, width, height);
		
		int newWidth = width/downsampleFactor;
		int newHeight = height/downsampleFactor;
		
		float resampled[][] = downSample.generateAveraged(newWidth, newHeight);
		float min = 0, max = 0;
		boolean mbHasMinMax = false;
		
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				float data = resampled[y][x];
				if (data > -9999.0f) {
					if (!mbHasMinMax) {
						mbHasMinMax = true;
						max = data;
						min = data;
					}
					if (data > max) {
						max = data;
					}
					else if (data < min) {
						min = data;
					}
				}
			}
		}
		
		Logger.info(" Min: " + Float.toString(min) + "   Max: " + Float.toString(max));
		Logger.info("Generating IDX array");

		byte[][] idx = new byte[newHeight][newWidth];

		// Now generate the heatmap image
		for (int y = 0; y < newHeight; y++) {
			for (int x = 0; x < newWidth; x++) {
				float data = resampled[y][x];
				if (data > -9999.0f) {
					float delta = (data - min) / (max - min) * (numPaletteEntries - 1);
					if (delta < 0) delta = 0;
					else if (delta > numPaletteEntries - 1) delta = numPaletteEntries - 1;
					idx[y][x] = (byte)(delta);
				}
				else {
					idx[y][x] = numPaletteEntries; // transparent
				}
			}
		}
	
		Logger.info("Creating png");
//		File path = new File(outputFile.getPath());
//		path.mkdirs(); // make any necessary directories...
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
				1, 255, 255, 204,	// yellowish
				3, 160, 218, 180);	// greenish
		palette.setEntry(4, 65, 182, 196); // greenish blue
		palette.setEntry(5, 34, 94, 168); // blueish
		palette.setEntry(6, 106, 21, 160);//blueish purple
		palette.setEntry(7, 255, 0, 0);// red, but transparent

		Logger.info("Setting transparent");
		// set index 4 as transparent
		int[] alpha = new int[numPaletteEntries + 1];
		alpha[0] = 255; alpha[1] = 255; alpha[2] = 255;
		alpha[3] = 255;
		alpha[4] = 255; alpha[5] = 255; alpha[6] = 255;
		alpha[7] = 0; // transparent color
		png.setTransparentArray(alpha);
		
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		sendBack = copyPaletteForClient(palette, numPaletteEntries, sendBack);
		sendBack = copyAbsoluteValuesForClient(min, max, numPaletteEntries, sendBack);
		
		png.mPngWriter.writeRowsByte(idx);
		png.mPngWriter.end();
		
		return sendBack;
	}
	
}

