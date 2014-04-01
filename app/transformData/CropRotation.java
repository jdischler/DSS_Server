package util;

import play.*;
import java.util.*;
import java.io.*;

//------------------------------------------------------------------------------
// Crop Rotation
//
// This class calculates a new crop rotation layer based on data imported from 3 
//	adjacent CDL years and using some (fairly) messy logic to sort things out...
//
//------------------------------------------------------------------------------
public class CropRotation
{
	//--------------------------------------------------------------------------
	public void computeRotation() {
		
		Logger.info("Rotation Start:");
		Layer_Integer layer;
		try {
			// Load layers and store data in unmodified format, ie, RAW/original format
			layer = new Layer_Integer("cdl_2010", Layer_Integer.EType.ERaw); layer.init();
			layer = new Layer_Integer("cdl_2011", Layer_Integer.EType.ERaw); layer.init();
			layer = new Layer_Integer("cdl_2012", Layer_Integer.EType.ERaw); layer.init();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
		}

		processLayers();
		
		// Remove data
		Layer_Base.removeLayer("cdl_2010");
		Layer_Base.removeLayer("cdl_2011");
		Layer_Base.removeLayer("cdl_2012");
	}
	
	//--------------------------------------------------------------------------
	private void processLayers()
	{
		int NO_CONDITION = -1;
		int NO_DATA = -9999;
		int inCORN = 1, inGRAINS = 2, inVEGGIES = 3, inTREECROP = 4, inGRASS = 6, inWOODS = 7, inWETLAND = 8,
			inWATER = 9, inSUBURBAN = 10, inURBAN = 11, inBARREN = 12, inOTHERCROP = 15, inSOY =  16,
			inALFALFA = 17, inCORN_GRAIN = 18, inSOY_GRAIN = 19, inOIL = 21;
			
		int CONTINUOUS_CORN = 100, CONTINUOUS_SOY = 200, CORN_SOY = 300, CORN_FORAGE = 400, OTHER_GRAIN = 500, VEGGIES = 600, TREE_CROPS = 700,
			CONTINUOUS_ALFALFA = 800, GRASS_HAY = 900, OTHER_AG = 1000, WOODLAND = 1100, WETLAND = 1200, WATER = 1300, SUBURBS = 1400,
			URBAN = 1500, BARREN = 1600;
		
		Logger.info("Gathering data pointers:");
		// Get cdl_2010 and get the layer width/height off of that...
		Layer_Base cdl_2010 = Layer_Base.getLayer("cdl_2010");
		if (cdl_2010 == null){
			Logger.info("Fail 2010");
		} 
		int width = cdl_2010.getWidth();
		int height = cdl_2010.getHeight();
		
		int[][] layer2010 = cdl_2010.getIntData();
		
		int[][] layer2011 = Layer_Base.getLayer("cdl_2011").getIntData();
		if (layer2011 == null){
			Logger.info("Fail 2011");
		}
		int[][] layer2012 = Layer_Base.getLayer("cdl_2012").getIntData();
		if (layer2012 == null){
			Logger.info("Fail 2012");
		}
		
		Logger.info("Allocating new output layer.");
		int[][] outputLayer = new int[height][width];
		
		Logger.info("Processing cdl layers to generate rotation layer");
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int result = NO_CONDITION;
				int one = layer2010[y][x], two = layer2011[y][x], three = layer2012[y][x];
				
				// RULES
				if (one == NO_DATA || two == NO_DATA || three == NO_DATA) {
				//	Logger.info("No-Data:");
					result = NO_DATA;
				}
				else
				{
					do // do once...
					{
						result = areSame(inCORN, one, two, three, CONTINUOUS_CORN, result);	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inSOY, one, two, three, CONTINUOUS_SOY, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inVEGGIES, one, two, three, VEGGIES, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inTREECROP, one, two, three, TREE_CROPS, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inALFALFA, one, two, three, CONTINUOUS_ALFALFA, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inGRASS, one, two, three, GRASS_HAY, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inWOODS, one, two, three, WOODLAND, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inWETLAND, one, two, three, WETLAND, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inWATER, one, two, three, WATER, result); 			if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inSUBURBAN, one, two, three, SUBURBS, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inURBAN, one, two, three, URBAN, result); 			if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inBARREN, one, two, three, BARREN, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inOTHERCROP, one, two, three, OTHER_AG, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						
						result = anyTwoYearRotation(inCORN, inSOY, one, two, three, CORN_SOY, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = anyTwoYearRotation(inGRASS, inWOODS, one, two, three, GRASS_HAY, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = anyTwoYearRotation(inALFALFA, inWOODS, one, two, three, GRASS_HAY, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = anyTwoYearRotation(inGRASS, inWETLAND, one, two, three, WETLAND, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = anyTwoYearRotation(inGRASS, inALFALFA, one, two, three, GRASS_HAY, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = anyTwoYearRotation(inGRASS, inWATER, one, two, three, WETLAND, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = anyTwoYearRotation(inWOODS, inWATER, one, two, three, WOODLAND, result); if (result != NO_DATA && result != NO_CONDITION) break;
						
						result = MatchThree(inGRASS, inWOODS, inALFALFA, one, two, three, GRASS_HAY, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
												
						result = ifAny(inCORN, one, two, three, OTHER_GRAIN, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inSOY, one, two, three, OTHER_GRAIN, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inGRAINS, one, two, three, OTHER_GRAIN, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inVEGGIES, one, two, three, VEGGIES, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inTREECROP, one, two, three, TREE_CROPS, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inWETLAND, one, two, three, WETLAND, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inCORN_GRAIN, one, two, three, OTHER_GRAIN, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inSOY_GRAIN, one, two, three, OTHER_GRAIN, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						
						result = ifAny(inURBAN, one, two, three, URBAN, result); 			if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inSUBURBAN, one, two, three, SUBURBS, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inBARREN, one, two, three, BARREN, result); 			if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inWATER, one, two, three, WETLAND, result); 			if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inOTHERCROP, one, two, three, OTHER_AG, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inGRASS, one, two, three, GRASS_HAY, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inALFALFA, one, two, three, GRASS_HAY, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						
					} while(false); // do once...
					
					if (result != NO_DATA && result != NO_CONDITION) {
						// Data value 'constants' multipied by 100 when defined to avoid colliding with
						//	input values...so divide by 100 to get them back to the right range
						result /= 100;
					}
					if (result == NO_CONDITION) {
						// output a triple value for debug (when loaded into ArcGIS)
						result = one * 10000 + two * 100 + three;
					}
				}
				outputLayer[y][x] = result;
			}
		}
	
		Logger.info("About to output EXPLODE");
		try {
			Logger.info("Opening file");
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/explode.asc")));
			Logger.info("Outputting header");
			out.println("ncols         " + Integer.toString(width));
			out.println("nrows         " + Integer.toString(height));
			out.println("xllcorner     -10062652.65061");
			out.println("yllcorner     5249032.6922889");
			out.println("cellsize      30");
			out.println("NODATA_value  -9999");
			Logger.info("Writing array");
			for (int y = 0; y < height; y++) {
				StringBuffer sb = new StringBuffer();
				for (int x = 0; x < width; x++) {
					sb.append(Integer.toString(outputLayer[y][x]));
					if (x != width - 1) {
						sb.append(" ");
					}
				}
				out.println(sb.toString());
			}
			Logger.info("Closing file");
			out.close();
			Logger.info("EXPLODE was written");
		}
		catch(Exception err) {
			Logger.info("Oops, something went wrong with writing Explode!");
		}
	}
	
	//--------------------------------------------------------------------------
	private int ifAny(int type, int y1, int y2, int y3, int output, int current) {
		if ( y1 == type || y2 == type || y3 == type) {
			return output;
		}
		return current;
	}
	
	//--------------------------------------------------------------------------
	private int areSame(int type, int one, int two, int three, int output, int current) {
		if (type == one && one == two && two == three) {
			return output;
		}
		return current;
	}

	// NOTE: this also covers	
	//--------------------------------------------------------------------------
	private int anyTwoYearRotation(int type1, int type2, int one, int two, int three, int rotation, int current) {
		if ((one == type1 || one == type2) && (two == type1 || two == type2) && (three == type1 || three == type2) && (one != two || two != three)) {
			return rotation;
		}
		return current;
	}
	
	// NOTE: match three	
	//--------------------------------------------------------------------------
	private int MatchThree(int type1, int type2, int type3, int one, int two, int three, int output, int current) {
		if ((one == type1 || one == type2 || one == type3) && (two == type1 || two == type2 || two == type3) && (three == type1 || three == type2 || three == type3) && (one != two && two != three && one != three)) {
			return output;
		}
		return current;
	}
}
