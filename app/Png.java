package util;

import play.*;
import java.util.*;
import java.io.*;

// PNGJ api docs....
// http://pngj.googlecode.com/git/pnjg/doc/api/index.html
import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

// Provides a wrapper and helpers around the PNGJ library
//------------------------------------------------------------------------------
public class Png {

	//------------------------	
	public static class RGB {
		public int mR, mG, mB;
		
		public RGB(int r, int g, int b) {
			mR = r;
			mG = g;
			mB = b;
		}
	}

	protected ImageInfo mImageInfo;
	protected PngWriter mPngWriter;
	
	// Creates indexed image if channels == 1 
	//	Creates RGB image if channels == 3 
	//	Creates RGBA image if channels == 4
	// OutputFile is the filename with path to write
	//--------------------------------------------------------------------------
	public Png(int width, int height, int bitDepth, int channels, String outputFile) {
		
		if (channels == 1) {
			// width, height, bitDepth, alpha, grayscale, indexed
			mImageInfo = new ImageInfo(width, height, bitDepth, false, false, true);
		}
		else if (channels == 3) {
			// width, height, bitDepth, alpha
			mImageInfo = new ImageInfo(width, height, bitDepth, false);
		}
		else if (channels == 4) {
			// width, height, bitDepth, alpha
			mImageInfo = new ImageInfo(width, height, bitDepth, true);
		}
		
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(outputFile);
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		mPngWriter = new PngWriter(outputStream, mImageInfo);

		// set default compression and dpi?		
		//		pngW.setCompLevel(9); // compression level not working?
		mPngWriter.getMetadata().setDpi(306.98);
	}

	// Not sure if we generally need to set a DPI for the DSS?	
	//--------------------------------------------------------------------------
	public void setDPI(float dpi) {
		
		mPngWriter.getMetadata().setDpi(dpi);
	}
	
	// Only for indexed (palettized) images
	// returns PNGJ palette chunk object
	//--------------------------------------------------------------------------
	public PngChunkPLTE createPalette(int numPaletteEntries) throws Exception {
		
		if (!mImageInfo.indexed) {
			throw new Exception();
		}
		
		PngChunkPLTE palette = mPngWriter.getMetadata().createPLTEChunk();
		palette.setNentries(numPaletteEntries);
		
		return palette;
	}
	
	// Only for indexed (palettized) images
	//--------------------------------------------------------------------------
	public void setTransparentIndex(int paletteIndex) {
		
		PngChunkTRNS trans = mPngWriter.getMetadata().createTRNSChunk();
		trans.setIndexEntryAsTransparent(paletteIndex);
	}

	// Only for indexed (palettized) images
	//--------------------------------------------------------------------------
	public void setTransparentArray(int[] arrayOfAlphas) {
		
		PngChunkTRNS trans = mPngWriter.getMetadata().createTRNSChunk();
		trans.setPalletteAlpha(arrayOfAlphas);
	}
	
	//--------------------------------------------------------------------------
	public void writeArray(int[][] imagePixels) {
		
		mPngWriter.writeRowsInt(imagePixels);
		mPngWriter.end();
	}

	//--------------------------------------------------------------------------
	public void writeArray(byte[][] imagePixels) {
		
		mPngWriter.writeRowsByte(imagePixels);
		mPngWriter.end();
	}

	//--------------------------------------------------------------------------
	public void setColor(PngChunkPLTE palette, int index, Png.RGB color) {
		
		palette.setEntry(index, color.mR, color.mG, color.mB);
	}
	
	//--------------------------------------------------------------------------
	private static int interpolateElement(int startIdx, int endIdx, int curIdx, int startClrVal, int endClrVal) {
		
		float colorVal = ((float)(endClrVal - startClrVal) / (float)(endIdx - startIdx)) 
				* (curIdx - startIdx) + startClrVal;
				
		if (colorVal < 0.0f) colorVal = 0.0f;
		else if (colorVal > 255.0f) colorVal = 255.0f;
		
		return (int)colorVal;
	}
	
	//--------------------------------------------------------------------------
	public static void interpolatePaletteEntries(PngChunkPLTE palette, 
			int startIndex, int startR, int startG, int startB,
			int endIndex, int endR, int endG, int endB) {
	
		for (int idx = startIndex; idx <= endIndex; idx++) {

			int r = interpolateElement(startIndex, endIndex, idx, startR, endR);
			int g = interpolateElement(startIndex, endIndex, idx, startG, endG);
			int b = interpolateElement(startIndex, endIndex, idx, startB, endB);
			palette.setEntry(idx, r, g, b);
		}
	}
	
	// Creates an indexed version of the heatmap...
	//--------------------------------------------------------------------------
	public static void createHeatMap_I(String name, String folder1, String folder2) {
		
		try {
			Asc_Reader file1 = new Asc_Reader(name, folder1);
			Asc_Reader file2 = new Asc_Reader(name, folder2);
	
			int noDataVal = file1.getNoData() / 2; // FIXME
			
			// Blah, find max 
			//	(would be nice if ASC file could just have the min and max value saved in it?)
			double max = 0.3;
			while (file1.ready() && file2.ready()) {
				
				String line1[] = file1.getSplitLine();
				String line2[] = file2.getSplitLine();
				
				for (int x = 0; x < line1.length; x++) 
				{	
					if (Float.valueOf(line1[x]) > noDataVal && Float.valueOf(line2[x]) > noDataVal) { 
						double delta = Math.abs(Float.parseFloat(line1[x]) - Float.parseFloat(line2[x]));
						if (delta > max) {
							max = delta;
						}
					}
				}
			}
			
			Logger.info("Max: " + Double.toString(max));
			if (max < 0.00001) {
				max = 1.0;
			}
			file1.close();
			file2.close();

			file1 = new Asc_Reader(name, folder1);
			file2 = new Asc_Reader(name, folder2);
		
			// Make a temp buffer
			int width = file1.getWidth();
			int height = file1.getHeight();
			
			byte[][] idx = new byte[height][width];
			
			int y = 0;
			
			Logger.info("Generating IDX array");
			
			// Now generate the full resolution heatmap image
			while (file1.ready() && file2.ready()) {
				
				String line1[] = file1.getSplitLine();
				String line2[] = file2.getSplitLine();
				
				for (int x = 0; x < line1.length; x++) 
				{	
					if (Float.valueOf(line1[x]) <= noDataVal || Float.valueOf(line2[x]) <= noDataVal) { 
						idx[y][x] = 4; // trans
					}
					else {
						// Get Normalized Delta (-1.0 to 1.0)
						double delta = (Float.parseFloat(line1[x]) - Float.parseFloat(line2[x])) / max;
					
						delta = delta * 4.0 + 4.5; // round
						if (delta < 0) delta = 0;
						else if (delta > 8) delta = 8;
						idx[y][x] = (byte)delta;
					}
				}
				y++;
			}
		
			file1.close();
			file2.close();
			
			Logger.info("Creating png");

			String file = "./public/file/heat_2.png";
//			Png image = new Png(width, height, 8, 4, file);
			Png png = new Png(width, height, 
				8, 1, file);
		
			Logger.info("Creating palette");
			
			PngChunkPLTE palette = png.createPalette(9);
			Logger.info("Setting palette entries");
			// colors from....http://colorbrewer2.org
			
			// MAGENTA to WHITE to LIME GREEN
/*			interpolatePaletteEntries(palette, 
				0, 197, 27, 125,	// magenta
				4, 247, 247, 247);	// white
			interpolatePaletteEntries(palette, 
				4, 247, 247, 247,	// white
				8, 77, 146, 33);	// limey-green
*/
			// AQUA BLUE to WHITE to BROWN
			interpolatePaletteEntries(palette, 
				0, 0, 102, 94,		// aqua blue
				4, 247, 247, 247);	// white
			interpolatePaletteEntries(palette, 
				4, 247, 247, 247,	// white
				8, 140, 81, 10);	// brown
			
			Logger.info("Setting transparent");
			// set index 4 as transparent
			int[] alpha = new int[9];
			alpha[0] = 255; alpha[1] = 255; alpha[2] = 255; alpha[3] = 255;
			alpha[4] = 0;
			alpha[5] = 255; alpha[6] = 255; alpha[7] = 255; alpha[8] = 255;
			
			png.setTransparentArray(alpha);//TransparentIndex(0);
	
			png.mPngWriter.writeRowsByte(idx);
			/*ImageLineInt imgLine = new ImageLineInt(png.mImageInfo);
			//ImageLineHelper helper = new ImageLineHelper();
	
			Logger.info("Creating scanlines");
			int[] scanline = imgLine.getScanline();
			for (y = 0; y < height; y++) {	
				for (int x = 0; x < width; x++) {
					if (true) {//red[y][x] > 0 && grn[y][x] > 0) {
						scanline[x] = idx[y][x];
					}
					else {
					}
				}
				png.mPngWriter.writeRow(imgLine);
			}
			
			Logger.info("writing png");*/
			png.mPngWriter.end();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}

	//--------------------------------------------------------------------------
	public static void createHeatMap_II() {
	
long heatmapBuildStart = System.currentTimeMillis();
long heatmapResampleStart = 0, heatmapResampleEnd = 0;
long heatmapPngStart = 0, heatmapPngEnd = 0;
		try {
			Layer_Float hi = new Layer_Float("habitat_index", true);
			hi.init();

			float data[][] = hi.getFloatData();
			
			int width = hi.getWidth();
			int height = hi.getHeight();
			
heatmapResampleStart = System.currentTimeMillis();
			int newWidth = width/10;
			int newHeight = height/10;
			
			float resampled[][] = Downsampler.generateAveraged(data, width, height, newWidth, newHeight);
heatmapResampleEnd = System.currentTimeMillis();
			
heatmapPngStart = System.currentTimeMillis();
			float min = resampled[0][0], max = resampled[0][0];
			
			for (int y = 0; y < newHeight; y++) {
				for (int x = 0; x < newWidth; x++) {
					if (resampled[y][x] > max) {
						max = resampled[y][x];
					}
					if (resampled[y][x] < min) {
						min = resampled[y][x];
					}
				}
			}
			
			Logger.info("Min: " + Float.toString(min) + "   Max: " + Float.toString(max));
			Logger.info("Generating IDX array");
	
			byte[][] idx = new byte[newHeight][newWidth];

			// Now generate the heatmap image
			for (int y = 0; y < newHeight; y++) {
				for (int x = 0; x < newWidth; x++) {
					idx[y][x] = (byte)(resampled[y][x] * 8.0f + 0.5f);
				}
			}
		
			Logger.info("Creating png");
			String file = "./public/file/heat_3.png";
			Png png = new Png(newWidth, newHeight, 
				8, 1, file);
		
			Logger.info("Creating palette");
			PngChunkPLTE palette = png.createPalette(9);
			Logger.info("Setting palette entries");
			
			interpolatePaletteEntries(palette, 
				0, 128, 0, 255,		// purple
				2, 255, 0, 0);		// red
			interpolatePaletteEntries(palette, 
				2, 255, 0, 0,		// red
				4, 255, 255, 0);	// yellow
			interpolatePaletteEntries(palette, 
				4, 255, 255, 0,		// yellow
				6, 0, 255, 0);		// green
			interpolatePaletteEntries(palette, 
				6, 0, 255, 0,		// yellow
				8, 0, 128, 255);	// blue
			
			png.mPngWriter.writeRowsByte(idx);
			png.mPngWriter.end();
heatmapPngEnd = System.currentTimeMillis();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
long heatmapBuildEnd = System.currentTimeMillis();
float timeSec = (heatmapBuildEnd - heatmapBuildStart) / 1000.0f;
Logger.info(">>> Heatmap generation timing: " + Float.toString(timeSec));

timeSec = (heatmapResampleEnd - heatmapResampleStart) / 1000.0f;
Logger.info(">>> Heatmap resample timing: " + Float.toString(timeSec));

timeSec = (heatmapPngEnd - heatmapPngStart) / 1000.0f;
Logger.info(">>> Heatmap PNG creation/write timing: " + Float.toString(timeSec));
	}

	//--------------------------------------------------------------------------
	public static void createHeatMap_III() {
	
long heatmapBuildStart = System.currentTimeMillis();
long heatmapResampleStart = 0, heatmapResampleEnd = 0;
long heatmapPngStart = 0, heatmapPngEnd = 0;
		try {
			Layer_Float hi_1 = new Layer_Float("habitat_index", true);
			hi_1.init();
			Layer_Float hi_2 = new Layer_Float("habitat_index_t", true);
			hi_2.init();

			float data1[][] = hi_1.getFloatData();
			float data2[][] = hi_2.getFloatData();
			
			int width = hi_1.getWidth();
			int height = hi_1.getHeight();
			
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					data1[y][x] -= data2[y][x];
				}
			}
			
heatmapResampleStart = System.currentTimeMillis();
			int newWidth = width/10;
			int newHeight = height/10;
			
			float resampled[][] = Downsampler.generateMax(data1, width, height, newWidth, newHeight);
heatmapResampleEnd = System.currentTimeMillis();
			
heatmapPngStart = System.currentTimeMillis();
			float min = resampled[0][0], max = resampled[0][0];
			
			for (int y = 0; y < newHeight; y++) {
				for (int x = 0; x < newWidth; x++) {
					if (resampled[y][x] > max) {
						max = resampled[y][x];
					}
					if (resampled[y][x] < min) {
						min = resampled[y][x];
					}
				}
			}
			
			Logger.info("Min: " + Float.toString(min) + "   Max: " + Float.toString(max));
			Logger.info("Generating IDX array");
	
			byte[][] idx = new byte[newHeight][newWidth];

			// Now generate the heatmap image
			for (int y = 0; y < newHeight; y++) {
				for (int x = 0; x < newWidth; x++) {
					float delta = resampled[y][x] * 4.0f + 4.5f; // round
					if (delta < 0) delta = 0;
					else if (delta > 8) delta = 8;
					idx[y][x] = (byte)(delta);
				}
			}
		
			Logger.info("Creating png");
			String file = "./public/file/heat_max.png";
			Png png = new Png(newWidth, newHeight, 
				8, 1, file);
		
			Logger.info("Creating palette");
			PngChunkPLTE palette = png.createPalette(9);
			Logger.info("Setting palette entries");
			
			interpolatePaletteEntries(palette, 
				0, 128, 0, 255,		// purple
				2, 255, 0, 0);		// red
			interpolatePaletteEntries(palette, 
				2, 255, 0, 0,		// red
				4, 255, 255, 0);	// yellow
			interpolatePaletteEntries(palette, 
				4, 255, 255, 0,		// yellow
				6, 0, 255, 0);		// green
			interpolatePaletteEntries(palette, 
				6, 0, 255, 0,		// green
				8, 0, 128, 255);	// blue

			Logger.info("Setting transparent");
			// set index 4 as transparent
			int[] alpha = new int[9];
			alpha[0] = 255; alpha[1] = 255; alpha[2] = 255; alpha[3] = 255;
			alpha[4] = 0;
			alpha[5] = 255; alpha[6] = 255; alpha[7] = 255; alpha[8] = 255;
			
			png.setTransparentArray(alpha);//TransparentIndex(0);
			
			png.mPngWriter.writeRowsByte(idx);
			png.mPngWriter.end();
heatmapPngEnd = System.currentTimeMillis();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
long heatmapBuildEnd = System.currentTimeMillis();
float timeSec = (heatmapBuildEnd - heatmapBuildStart) / 1000.0f;
Logger.info(">>> Heatmap generation timing: " + Float.toString(timeSec));

timeSec = (heatmapResampleEnd - heatmapResampleStart) / 1000.0f;
Logger.info(">>> Heatmap resample timing: " + Float.toString(timeSec));

timeSec = (heatmapPngEnd - heatmapPngStart) / 1000.0f;
Logger.info(">>> Heatmap PNG creation/write timing: " + Float.toString(timeSec));
	}
}

