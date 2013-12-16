
package util;

import play.*;
import java.util.*;
import java.io.*;

//------------------------------------------------------------------------------
class Asc_Reader {
	
	private BufferedReader mReader;
	
	private int mWidth, mHeight;
	protected float mCellSize, mCornerX, mCornerY;
	protected int mNoDataValue;

	// Constructor	
	//--------------------------------------------------------------------------
	public Asc_Reader(String name, String folder) {
		
		mReader = readHeader(name, folder);
	}
	
	//--------------------------------------------------------------------------
	public boolean ready() throws Exception {
		
		return mReader.ready();
	}
		
	//--------------------------------------------------------------------------
	public void close() {
		try {
			mReader.close();
		}
		catch(Exception E) {
			Logger.error(E.toString());
		}
	}
	
	// Header Accessors...
	//--------------------------------------------------------------------------
	public int getWidth() {
		return mWidth;
	}
	
	//--------------------------------------------------------------------------
	public int getHeight() {
		return mHeight;
	}
	
	//--------------------------------------------------------------------------
	public float getCornerX() {
		return mCornerX;
	}
	
	//--------------------------------------------------------------------------
	public float getCornerY() {
		return mCornerY;
	}
	
	//--------------------------------------------------------------------------
	public int getNoData() {
		return mNoDataValue;
	}
	
	// Reads the next line from the file and splits it into tokens which are placed
	//	into an array
	//--------------------------------------------------------------------------
	public String[] getSplitLine() throws Exception {
		
		String line = mReader.readLine();
		return line.split("\\s+");
	}
	//--------------------------------------------------------------------------
	private String getHeaderValue(String line) {
		
		String split[] = line.split("\\s+");
		if (split.length == 2) {
			return split[1];
		}
		
		return null;
	}
	
	// Read The Header of The File
	//--------------------------------------------------------------------------
	private BufferedReader readHeader(String name, String folder) {
		
		BufferedReader r = null;
		
		String file = "./layerData/";
		if (folder != null) {
			file += folder + "/";
		}
		file += name + ".asc";
		
		Logger.info("Reading: " + file);
		
		try {
			FileReader f = new FileReader(file);
			r = new BufferedReader(f);
			
			String width = r.readLine(); 	// ncols
			String height = r.readLine(); 	// nrows
			String xllCorner = r.readLine();// xll corner
			String yllCorner = r.readLine();// yll corner
			String cellSize = r.readLine(); // cellsize
			String noData = r.readLine(); 	// nodata value
			
			// Echo string values in ASC header
			Logger.info("  " + width);
			Logger.info("  " + height);
			Logger.info("  " + xllCorner);
			Logger.info("  " + yllCorner);
			Logger.info("  " + cellSize);
			Logger.info("  " + noData);
			
			// convert to required data types and save
			mWidth = Integer.parseInt(getHeaderValue(width));
			mHeight = Integer.parseInt(getHeaderValue(height));
			mCornerX = Float.parseFloat(getHeaderValue(xllCorner));
			mCornerY = Float.parseFloat(getHeaderValue(yllCorner));
			mCellSize = Float.parseFloat(getHeaderValue(cellSize));
			mNoDataValue = Integer.parseInt(getHeaderValue(noData));
		} 
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		return r;
	}
}

