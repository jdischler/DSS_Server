package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;


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
	private void cacheLayers() {

		mLayers = new HashMap<String,int[][]>();
		
		Layer_Base layer;
		try {
			layer = new Layer_Indexed("cdl"); layer.init();
			layer = new Layer_Continuous("slope", 0.0f, 90.0f, 0, 360); layer.init();
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
	

