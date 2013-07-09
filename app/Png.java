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

	// FIXME: probably need to rethink this...?	probably only works for indexed?
	//--------------------------------------------------------------------------
	public void writeArray(int[][] imagePixels) {
		
		mPngWriter.writeRowsInt(imagePixels);
		mPngWriter.end();
	}
	
	// FIXME: probably need to rethink this...?	probably only works for indexed?
	//--------------------------------------------------------------------------
	public void writeResampledSelection(int sourceWidth, int sourceHeight, Selection selection) {
		
		float stepX = (float)mImageInfo.cols / sourceWidth;
		float stepY = (float)mImageInfo.rows / sourceHeight;
		
		byte[][] temp = new byte[mImageInfo.rows][mImageInfo.cols];
		
		float fy = 0;
		
		for (int y = 0; y < sourceHeight; y++) {
			float fx = 0;
			for (int x = 0; x < sourceWidth; x++) {
				temp[((int)fy)][((int)fx)] |= selection.mSelection[y][x];
				fx += stepX;
			}
			fy += stepY;
		}
		
		mPngWriter.writeRowsByte(temp);
		mPngWriter.end();
	}

	//--------------------------------------------------------------------------
	public static void createHeatmapFromDelta(String deltaFile, String outputPng) {
	
		// TODO: open deltaFile (ASC) and get width/height
		int width = 1;
		int height = 1;
		
		// 8 bits per pixel, 4 channels (RGBA)
		Png newImage = new Png(width, height, 8, 4, outputPng);
		
		// TODO: finish me
		
//		newImage.mPngWriter.writeRowsInt(temp);
		newImage.mPngWriter.end();
	}
}

