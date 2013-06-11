package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Calculate Slope
//
// This class calculates a new slope layer based on an imported DEM 
//
//------------------------------------------------------------------------------
public class CalculateSlope
{
	Layer_Base mDEM;
	
	// Internal helper class
	//--------------------------------------------------------------------------
	protected class Vec {
		
		public float mX, mY, mZ;
		
		public Vec(float x, float y, float z) {
			mX = x;
			mY = y;
			mZ = z;
		}
		
		public Vec subtract(Vec that) {
			return new Vec(that.mX - this.mX, that.mY - this.mY, that.mZ - this.mZ); 
		}
		
		public void LOG() {
			Logger.info("Vector is: <" + Float.toString(mX) + "> <"
				+ Float.toString(mY) + "> <"
				+ Float.toString(mZ) + ">");
		}
		
		public Vec normalize() {
			
			// get magnitude of new normal vector...
			float mag = (float)Math.sqrt(this.mX * this.mX + this.mY * this.mY + this.mZ * this.mZ);
			
			// normalize to get a new unit vector
			return new Vec(this.mX / mag, this.mY / mag, this.mZ / mag);
		}
		
		// Note, input is really an (x,y,z) point
		public Vec getNormal(Vec p1, Vec p3) {
			
			Vec v1 = this.subtract(p1).normalize();
			Vec v2 = this.subtract(p3).normalize();
			
			// calculate cross product...
			Vec cross = new Vec(v1.mY * v2.mZ - v1.mZ * v2.mY,
								  v1.mZ * v2.mX - v1.mX * v2.mZ,
								  v1.mX * v2.mY - v1.mY * v2.mX);
			return cross;
		}
		
		// Calculate the angle between the normal of a plane defined by p1, p2, p3...
		//	and a unit vector, typically the up vector (0,0,1);
		public float getAngleBetween(Vec p1, Vec p3, Vec up) {
			
			Vec normal = this.getNormal(p1, p3);
			
			// dot is the dotProduct of the two vectors, which is actually the cosine
			//	of the angle between the two vectors assuming both inputs are unit vectors
			float dot = normal.mX * up.mX + normal.mY * up.mY + normal.mZ * up.mZ;
			
			// convert the cosine of the angle back into degrees
			return (float)Math.toDegrees(Math.acos(dot));
		}
	}
	
	//--------------------------------------------------------------------------
	public void computeSlope() {
		
		Logger.info("Slope Start:");
		try {
			// Load and store data in unmodified format, ie, RAW/original format
			mDEM = new Layer_Raw("dem");
			mDEM.init();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
		}

		processRaster();
		
		// NOTE:
		// DEM not currently used elsewhere? If true, should remove it from memory
		Layer_Base.removeLayer("dem");
		mDEM = null;
	}
	
	//--------------------------------------------------------------------------
	private void processRaster()
	{
		int NO_DATA = -9999;
		int width = mDEM.getWidth();
		int height = mDEM.getHeight();

		Logger.info("Allocating new output layer.");
		float[][] outputLayer = new float[height][width];
		float[][] inputRaster = ((Layer_Raw)mDEM).getFloatData();
		
		Logger.info("Processing layers to generate slope layer");
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				outputLayer[y][x] = calcAngle(x, y, width, height, inputRaster);
			}
		}
	
		Logger.info("About to output SLOPE");
		try {
			Logger.info("Opening file");
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/slope_new.asc")));
			Logger.info("Outputting header");
			out.println("ncols         " + Integer.toString(width));
			out.println("nrows         " + Integer.toString(height));
			out.println("xllcorner     -10062652.65061");
			out.println("yllcorner     5249032.6922889");
			out.println("cellsize      30");
			out.println("NODATA_value  " + Integer.toString(NO_DATA));
			Logger.info("Writing array");
						
			for (int y = 0; y < height; y++) {
				StringBuffer sb = new StringBuffer();
				for (int x = 0; x < width; x++) {
					if (outputLayer[y][x] < 0) {
						sb.append("-9999");
					}
					else {
						sb.append(String.format("%.2f", outputLayer[y][x]));//Float.toString(outputLayer[y][x]));
					}
					if (x != width - 1) {
						sb.append(" ");
					}
				}
				out.println(sb.toString());
			}
			Logger.info("Closing file");
			out.close();
			Logger.info("SLOPE was written");
		}
		catch(Exception err) {
			Logger.info("Oops, something went wrong with writing SLOPE!");
		}
	}

	//--------------------------------------------------------------------------
	private void processTriad(int x1, int y1, 
							int x2, int y2,
							int x3, int y3,
							int w, int h,
							float[][] ir, Vector<Float> fArray) {
	
		// if any of the x values would overlow the bounds, we don't check this cell							
		if (x1 < 0 || x1 > w-1 || x2 < 0 || x2 > w-1 || x3 < 0 || x3 > w-1) {
			return;
		}
		
		// if any of the y values would overlow the bounds, we don't check this cell							
		if (y1 < 0 || y1 > h-1 || y2 < 0 || y2 > h-1 || y3 < 0 || y3 > h-1) {
			return;
		}
		
		// don't compute an angle for this triad if one of the inputs is a NO_DATA value
		if (ir[y1][x1] >= 0 && ir[y2][x2] >= 0 && ir[y3][x3] >= 0) 
		{
			Vec p2 = new Vec(x2 * 30, y2 * 30, ir[y2][x2]);
		
			// NOTE: the vector computed from p1->p2 & p2->p3 must be orthogonal
			float angle = p2.getAngleBetween(
				new Vec(x1 * 30, y1 * 30, ir[y1][x1]),	// p1 
				new Vec(x3 * 30, y3 * 30, ir[y3][x3]),	// p3 
				new Vec(0,0,1));						// up
			
			fArray.add(angle);
		}
	}
			
	// calculates an angle for cell x, y based on a 3x3 neighborhood
	//--------------------------------------------------------------------------
	private float calcAngle(int x, int y, int w, int h, float[][] inputRaster) {
	
		// Don't calc a slope if self is no data
		if (inputRaster[y][x] < 0) {
			return -1;
		}
		
		Vector<Float> floatArray = new Vector<Float>();
		
		// Specialized (custom) windowing type analysis
		// I only computed the clusters that made sense to me, vs. calculating 
		//	every theoretically possible cluster. Unsure if this is a good assumption..
		// NOTE: processTriad needs input triads to form a set of two vectors that are orthogonal
		//	e.g. p1->p2 & p2->p3 must be orthogonal.
		
		// clusters are a given diagonal, the center and an adjacent edges.
		//  # . .
		//  # # .
		//  . . .
		processTriad(x-1, y-1,
					x-1, y,
					x, y,
					w, h,
					inputRaster, floatArray);
		//  . . .
		//  . # .
		//  # # .
		processTriad(x-1, y+1,
					x, y+1,
					x, y,
					w, h,
					inputRaster, floatArray);
		//  . . .
		//  . # #
		//  . . #
		processTriad(x+1, y+1,
					x+1, y,
					x, y,
					w, h,
					inputRaster, floatArray);
		//  . # #
		//  . # .
		//  . . .
		processTriad(x+1, y-1,
					x, y-1,
					x, y,
					w, h,
					inputRaster, floatArray);
		// clusters are the center point and the two adjacent edges.
		//  . # .
		//  # # .
		//  . . .
		processTriad(x-1, y,
					x, y,
					x, y-1,
					w, h,
					inputRaster, floatArray);
		//  . . .
		//  # # .
		//  . # .
		processTriad(x, y+1,
					x, y,
					x-1, y,
					w, h,
					inputRaster, floatArray);
		//  . . .
		//  . # #
		//  . # .
		processTriad(x+1, y,
					x, y,
					x, y+1,
					w, h,
					inputRaster, floatArray);
		//  . # .
		//  . # #
		//  . . .
		processTriad(x, y-1,
					x, y,
					x+1, y,
					w, h,
					inputRaster, floatArray);

		if (floatArray.size() <= 0) {
			return -1;
		}

		// Capture the max slope for the neighborhood, ie, get the worst-case
		//	erodible slope
		float max = 0;
		for (int i = 0; i < floatArray.size(); i++) {
			if (floatArray.get(i) > max) {
				max = floatArray.get(i);	
			}
		}
	
		return max;
/*
		// Averaging, which we probably don't want to use
		float sum = 0;
		for (int i = 0; i < floatArray.size(); i++) {
			sum += floatArray.get(i);
		}
		
		return sum / floatArray.size();
*/
	}
	
}
