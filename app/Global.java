package util;

import play.*;
import java.util.*;
import java.io.*;


//------------------------------------------------------------------------------
public class Global extends GlobalSettings
{
	static Map<String,int[][]>	mLayers;

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
	private void cacheByteLayerLow(String layername) throws Exception {
		
		int[][] workArray;
		int width, height; // 3263x2696
		
		Logger.info(layername);
		
		BufferedReader br = new BufferedReader(new FileReader("./layerData/" + layername + ".asc"));
		try {
			// read header first
			String hd1 = br.readLine(); // ncols
			String hd2 = br.readLine(); // nrows
			String hd3 = br.readLine(); // xll corner
			String hd4 = br.readLine(); // yll corner
			String hd5 = br.readLine(); // cellsize
			String hd6 = br.readLine(); // nodata value
			
			Logger.info(hd1);
			Logger.info(hd2);
			Logger.info(hd3);
			Logger.info(hd4);
			Logger.info(hd5);
			Logger.info(hd6);
			
			width = Integer.parseInt(getValue(hd1));
			height = Integer.parseInt(getValue(hd2));
			
			Logger.info("Allocating new work array");
			workArray = new int[height][width];
			
			Logger.info("Putting array into Map");
			mLayers.put(layername.toLowerCase(), workArray);
			
			Logger.info("Attempting to read the array data");
			
			int x = 0, y = 0;
			
			// now read the array data
			while (br.ready()) {
				if (y >= height) {
					Logger.error("BAD READ");
				}
				String line = br.readLine();
				String split[] = line.split("\\s+");
				for (int i = 0; i < split.length; i++) {
					workArray[y][x] = Integer.parseInt(split[i]);
					x++;
					if (x >= width) {
						x = 0;
						y++;
					}
				}
			}
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
			br.close();
		}

	}
	
	// example ("slope", 0.0, 90.0, 0, 360); .. remap a slope of 0.0-90.0 to 0-360
	//--------------------------------------------------------------------------
	private void cacheFloatLayerLow(String layername, 
			float f_min, float f_max, 
			int i_min, int i_max) throws Exception {
		
		int[][] workArray;
		int width, height; // 3263x2696
		
		Logger.info(layername);
		
		BufferedReader br = new BufferedReader(new FileReader("./layerData/" + layername + ".asc"));
		try {
			// read header first
			String hd1 = br.readLine(); // ncols
			String hd2 = br.readLine(); // nrows
			String hd3 = br.readLine(); // xll corner
			String hd4 = br.readLine(); // yll corner
			String hd5 = br.readLine(); // cellsize
			String hd6 = br.readLine(); // nodata value
			
			Logger.info(hd1);Logger.info(hd2);Logger.info(hd3);Logger.info(hd4);Logger.info(hd5);Logger.info(hd6);
			
			width = Integer.parseInt(getValue(hd1));
			height = Integer.parseInt(getValue(hd2));
			
			Logger.info("Allocating new work array");
			workArray = new int[height][width];
			
			Logger.info("Putting array into Map");
			mLayers.put(layername.toLowerCase(), workArray);
			
			Logger.info("Attempting to read the array data");
			
			int x = 0, y = 0;
			
			// now read the array data
			while (br.ready()) {
				if (y >= height) {
					Logger.error("BAD READ");
				}
				String line = br.readLine();
				String split[] = line.split("\\s+");
				for (int i = 0; i < split.length; i++) {
					
					float val = Float.parseFloat(split[i]);
					// only "project" to new range if in the specified float range
					//	this allows nodata values (such as -9999) to pass through
					if (val >= f_min && val <= f_max) {
						//normalize (0-1)
						val = ((val - f_min) / (f_max - f_min));
						// "reproject" to new range
						val = val * (i_max - i_min) + i_min;
					}
					workArray[y][x] = (int)val;
					
					x++;
					if (x >= width) {
						x = 0;
						y++;
					}
				}
			}
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
			br.close();
		}

	}
	
	//--------------------------------------------------------------------------
	private void cacheLayers() {

		mLayers = new HashMap<String,int[][]>();
		
		try {
			cacheByteLayerLow("cdl");
			cacheFloatLayerLow("slope", 0.0f, 90.0f, 0, 360);
//			cacheLayerLow("rivers");
//			cacheLayerLow("roads");
//			cacheLayerLow("slope");
//			cacheLayerLow("watersheds");
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
		}
	}
	
	//--------------------------------------------------------------------------
	public static int[][] getLayer(String name) {
		
		return mLayers.get(name);
	}
}
	

