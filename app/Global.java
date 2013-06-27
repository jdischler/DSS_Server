package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;


//------------------------------------------------------------------------------
public class Global extends GlobalSettings
{
	// If the play server is started in DEV mode, should we skip loading certain layers
	//	to get a faster server startup time and use less memory?
	private static final boolean LOAD_ALL_LAYERS_FOR_DEV = true;
	
	//--------------------------------------------------------------------------
	@Override
	public void onStart(play.Application app) {
		
		systemReport("Application has started");
		cacheLayers();
		systemReport("Data Layers Cached");
	}
	
	//--------------------------------------------------------------------------
	@Override
	public void onStop(play.Application app) {
		
		Layer_Base.removeAllLayers();
		System.gc();
		systemReport("Application stopped, Garbage Collection call made");
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
	private void systemReport(String customMessage) {
		
		float unitConversion = (1024.0f * 1024.0f); // bytes -> MB
		String unitName = "MB";
		
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| " + customMessage);
		Logger.info("+-------------------------------------------------------+");
		Logger.info("  Available Processors: " + 
			Integer.toString(Runtime.getRuntime().availableProcessors()));
		Logger.info("  Total Free Memory: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().freeMemory() / unitConversion)) +
				unitName);
		Logger.info("  Current Total Memory in Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().totalMemory() / unitConversion)) +
				unitName);
		Logger.info("  Maximum Memory for Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().maxMemory() / unitConversion)) +
				unitName);
		Logger.info("+-------------------------------------------------------+");
	}
	
	// Only tries to load a layer if it isn't in memory already
	//--------------------------------------------------------------------------
	private void cacheLayers() {

		/* // Uncomment if need to recalculate and output slope
		CalculateSlope cs = new CalculateSlope();
		cs.computeSlope();
		*/

		/* // Uncomment if need to recalculate and output crop rotation
		CropRotation cr = new CropRotation();
		cr.computeRotation();
		*/
		
		Layer_Base layer;
		try {
			layer = new Layer_Indexed("rotation"); layer.init();
			
			// data range is 0-90 but expand it up to 0-1000 internally since we are converting to int
			//	and losing some precision
			layer = new Layer_Continuous("slope", 0.0f, 90.0f, 0, 1000); layer.init();
			
			// distance to river can get clamped to the nearest int value without losing much...
			layer = new Layer_Continuous("rivers"); layer.init();
			
			// NOTE: if we have more than 32 watersheds, we CAN'T use Layer_Indexed
			layer = new Layer_Indexed("watersheds"); layer.init();
			
			// NOTE: can put low-priority (rarely used) data layers here so that
			//	we can have them skip loading in DEVELOPMENT mode. Ie, it gives us
			//	some ways that we can get the server up as quickly as possible for
			//	testing and development
			if (Play.isProd() || LOAD_ALL_LAYERS_FOR_DEV == true) {
				
				Logger.info("Loading all layers");
				// SOC can get clamped to the nearest int value without losing much...
				layer = new Layer_Continuous("soc"); layer.init();
			
				// distance to road can get clamped to the nearest int value without losing much...
				layer = new Layer_Continuous("roads"); layer.init();
				
				layer = new Layer_Indexed("lcc"); layer.init();
				layer = new Layer_Indexed("lcs"); layer.init();
			}
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
		}
	}	
}

