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
		Logger.info("Application is stopping");
		Layer_Base.removeAllLayers();
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
			
			// distance to road can get clamped to the nearest int value without losing much...
			layer = new Layer_Continuous("roads"); layer.init();
			
			// SOC can get clamped to the nearest int value without losing much...
			layer = new Layer_Continuous("soc"); layer.init();
			
			// NOTE: if we have more than 32 watersheds, we can't use Layer_Indexed
			layer = new Layer_Indexed("watersheds"); layer.init();
			
			layer = new Layer_Indexed("lcc"); layer.init();
			layer = new Layer_Indexed("lcs"); layer.init();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
		}
	}	
}
	

