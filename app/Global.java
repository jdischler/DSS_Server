package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;


//------------------------------------------------------------------------------
public class Global extends GlobalSettings
{
	//--------------------------------------------------------------------------
	@Override
	public void onStart(play.Application app) {
		Logger.info("Application has started");
		cacheLayers();
		Logger.info("--data layers cached");
	}
	
	//--------------------------------------------------------------------------
	@Override
	public void onStop(play.Application app) {
		Logger.info("Application has been stopped");
	}
	
	//--------------------------------------------------------------------------
	private String getValue(String line) {
		
		String split[] = line.split("\\s+");
		if (split.length == 2) {
			return split[1];
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	private void cacheLayers() {

		Layer_Base layer;
		try {
			layer = new Layer_Indexed("cdl"); layer.init();
			layer = new Layer_Continuous("slope", 0.0f, 90.0f, 0, 360); layer.init();
			//Rotation
			layer = new Layer_Indexed("cdl_2010"); layer.init();
			layer = new Layer_Indexed("cdl_2011"); layer.init();
			layer = new Layer_Indexed("cdl_2012"); layer.init();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
		}
		Rotation();
	}
	
	private void Rotation()
	{
		Logger.info("Rotation Start:");
		int width = 6150, height = 4557;
		int NO_CONDITION = -1;
		int NO_DATA = -9999;
		int inCORN = 1, inGRAINS = 2, inVEGGIES = 3, inTREECROP = 4, inGRASS = 6, inWOODS = 7, inWETLAND = 8,
			inWATER = 9, inSUBURBAN = 10, inURBAN = 11, inBARREN = 12, inOTHERCROP = 15, inSOY =  16,
			inALFALFA = 17, inCORN_GRAIN = 18, inSOY_GRAIN = 19, inOIL = 21;
			
		int CONTINUOUS_CORN = 100, CONTINUOUS_SOY = 200, CORN_SOY = 300, CORN_FORAGE = 400, OTHER_GRAIN = 500, VEGGIES = 600, TREE_CROPS = 700,
			CONTINUOUS_ALFALFA = 800, GRASS_HAY = 900, OTHER_AG = 1000, WOODLAND = 1100, WETLAND = 1200, WATER = 1300, SUBURBS = 1400,
			URBAN = 1500, BARREN = 1600;
		
		Logger.info("Import CDL layers into array:");
		int[][] layer2010 = Layer_Base.getLayer("cdl_2010").getLayerRaster();
		if (layer2010 == null){
			Logger.info("Faill 2010");
		}
		int[][] layer2011 = Layer_Base.getLayer("cdl_2011").getLayerRaster();
		if (layer2011 == null){
			Logger.info("Faill 2011");
		}
		int[][] layer2012 = Layer_Base.getLayer("cdl_2012").getLayerRaster();
		if (layer2012 == null){
			Logger.info("Faill 2012");
		}
		
		Logger.info("Import CDL layers into array:");
		int[][] outputLayer = new int[height][width];
		
		Logger.info("Import CDL layers into array:");
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
					do
					{
						
						result = areSame(inCORN, one, two, three, CONTINUOUS_CORN, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inSOY, one, two, three, CONTINUOUS_SOY, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inVEGGIES, one, two, three, VEGGIES, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inTREECROP, one, two, three, TREE_CROPS, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inALFALFA, one, two, three, CONTINUOUS_ALFALFA, result); if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inGRASS, one, two, three, GRASS_HAY, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inWOODS, one, two, three, WOODLAND, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inWETLAND, one, two, three, WETLAND, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inWATER, one, two, three, WATER, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inSUBURBAN, one, two, three, SUBURBS, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = areSame(inURBAN, one, two, three, URBAN, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
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
						result = ifAny(inCORN_GRAIN, one, two, three, OTHER_GRAIN, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inSOY_GRAIN, one, two, three, OTHER_GRAIN, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						
						result = ifAny(inURBAN, one, two, three, URBAN, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inSUBURBAN, one, two, three, SUBURBS, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inBARREN, one, two, three, BARREN, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inWATER, one, two, three, WETLAND, result); 		if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inOTHERCROP, one, two, three, OTHER_AG, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inGRASS, one, two, three, GRASS_HAY, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						result = ifAny(inALFALFA, one, two, three, GRASS_HAY, result); 	if (result != NO_DATA && result != NO_CONDITION) break;
						
					} while(false);
					
					if (result != NO_DATA && result != NO_CONDITION) {
						result /= 100;
					}
					if (result == NO_CONDITION) {
						result = one * 10000 + two * 100 + three;
					}
				}
				outputLayer[y][x] = result;
			}
		}
	
		Logger.info("About to output EXPLODE");
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/explode.asc")));
			out.println("ncols         6150");
			out.println("nrows         4557");
			out.println("xllcorner     -10062652.65061");
			out.println("yllcorner     5249032.6922889");
			out.println("cellsize      30");
			out.println("NODATA_value  -9999");
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
			out.close();
		}
		catch(Exception err) {
			Logger.info("Opps");
		}
		Logger.info("EXPLODE was written");
	}
	
	private int ifAny(int type, int y1, int y2, int y3, int output, int current) {
		if ( y1 == type || y2 == type || y3 == type) {
			return output;
		}
		return current;
	}
	
	private int areSame(int type, int one, int two, int three, int output, int current) {
		if (type == one && one == two && two == three) {
			return output;
		}
		return current;
	}

	// NOTE: this also covers	
	private int anyTwoYearRotation(int type1, int type2, int one, int two, int three, int rotation, int current) {
		if ((one == type1 || one == type2) && (two == type1 || two == type2) && (three == type1 || three == type2) && (one != two || two != three)) {
			return rotation;
		}
		return current;
	}
	
	// NOTE: match three	
	private int MatchThree(int type1, int type2, int type3, int one, int two, int three, int output, int current) {
		if ((one == type1 || one == type2 || one == type3) && (two == type1 || two == type2 || two == type3) && (three == type1 || three == type2 || three == type3) && (one != two && two != three && one != three)) {
			return output;
		}
		return current;
	}
}
	

