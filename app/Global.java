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
	private static final boolean LOAD_ALL_LAYERS_FOR_DEV = false;
	private static final boolean LOAD_DEFAULT_DATA = true;
	
	//--------------------------------------------------------------------------
	@Override
	public void onStart(play.Application app) 
	{
		systemReport("Application has started");
		
		// create any computed layers (currently don't have any in here?)
		computeLayers();
		
		cacheLayers();

		QueuedWriter.beginWatchWriteQueue();
		conditionalCreateDefaultModelOutputs();		
		cacheModelDefaults();
		
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
	
	//--------------------------------------------------------------------------
	private void computeLayers() {
		/* // Uncomment if need to recalculate and output slope
		CalculateSlope cs = new CalculateSlope();
		cs.computeSlope();
		*/

		/* // Uncomment if need to recalculate and output crop rotation
		CropRotation cr = new CropRotation();
		cr.computeRotation();
		*/
		
		/* new CalculateCornGrassProduction().run();
		*/
	}
	
	//--------------------------------------------------------------------------
	private void cacheLayers() 
	{
		Layer_Base layer;
		try {
			if (Play.isProd() || LOAD_ALL_LAYERS_FOR_DEV == true) {
				Logger.info("Loading all layers");
			}
			
			layer = new Layer_Integer("rotation"); layer.init();
			layer = new Layer_Float("slope"); layer.init();
			layer = new Layer_Float("cec"); layer.init();
			layer = new Layer_Float("depth"); layer.init();
			layer = new Layer_Float("silt"); layer.init();
			layer = new Layer_Float("soc"); layer.init();
			layer = new Layer_Integer("watersheds", Layer_Integer.EType.EQueryShiftedIndex); layer.init();
			layer = new Layer_Float("texture"); layer.init();
			layer = new Layer_Float("om_soc"); layer.init();
			layer = new Layer_Float("drainage"); layer.init();
			layer = new Layer_Float("ph"); layer.init();
			
			// NOTE: am putting low-priority (rarely used) data layers here so that
			//	we can have them skip loading in DEVELOPMENT mode. Ie, faster loads
			//	and less memory usage...
			if (Play.isProd() || LOAD_ALL_LAYERS_FOR_DEV == true) {
				
				layer = new Layer_Float("rivers"); layer.init();
				layer = new Layer_Integer("lcc"); layer.init();
				layer = new Layer_Integer("lcs"); layer.init();
//				layer = new Layer_Continuous("roads"); layer.init();
			}
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	private void cacheModelDefaults() {
		
		if (LOAD_DEFAULT_DATA) {
			Logger.info(" --- LOADING MODEL DEFAULT RESULT FILES ---");
			Layer_Base layer;
			try {
				layer = new Layer_Float("default/net_income"); layer.init();
				layer = new Layer_Float("default/net_energy"); layer.init();
				layer = new Layer_Float("default/ethanol"); layer.init();
				layer = new Layer_Float("default/habitat_index"); layer.init();
				layer = new Layer_Float("default/nitrogen"); layer.init();
				layer = new Layer_Float("default/phosphorus"); layer.init();
				layer = new Layer_Float("default/pest"); layer.init();
				layer = new Layer_Float("default/pollinator"); layer.init();
				layer = new Layer_Float("default/nitrous_oxide"); layer.init();
			}
			catch (Exception e) {
				Logger.info(e.toString());
			}
		}
		else {
			Logger.info(" --- SKIPPING LOAD OF MODEL DEFAULT RESULT FILES ---");
		}
	}

	// TODO: potentially check for individual model files vs. just the directory?
	//--------------------------------------------------------------------------
	private void conditionalCreateDefaultModelOutputs() {
		
		// Check for Default Scenario files...to replace them, you need to delete the whole
		//	DEFAULT folder otherwise they will not be recalculated with how this is coded
		//	The default folder also cannot exist for first generation (even if empty...)
		File Output = new File("./layerData/default");
		if(!Output.exists()) {
			
			Logger.info("Default scenario folder does not exist, creating it and default model files!");
			// Rotation
			Layer_Base layer = Layer_Base.getLayer("Rotation");
			int width = layer.getWidth();
			int height = layer.getHeight();
			int[][] defaultRotation = Layer_Base.getLayer("Rotation").getIntData();
			
			List<ModelResult> results;
			results = new Model_HabitatIndex_New().run(defaultRotation, width, height, "default");
			QueuedWriter.queueResults(results);

			results = new Model_EthanolNetEnergyIncome_New().run(defaultRotation, width, height, "default");
			QueuedWriter.queueResults(results);
			
			results = new Model_PollinatorPestSuppression_New().run(defaultRotation, width, height, "default");
			QueuedWriter.queueResults(results);
			
			results = new Model_NitrogenPhosphorus_New().run(defaultRotation, width, height, "default");
			QueuedWriter.queueResults(results);

			results = new Model_NitrousOxideEmissions_New().run(defaultRotation, width, height, "default");
			QueuedWriter.queueResults(results);

			// NOTE: SOC for the default is not in the model run because it is not a computed data layer like others...
			
			// wait for write queue to dump out the defaults...
			while(QueuedWriter.doesWriteQueueHaveFiles()) {
				Logger.info("Waiting for defaults to be written by the QueuedWriter");
				try {
					Thread.sleep(4000);
				}
				catch(Exception e) {
					// blah, java exception handling...
				}
			}
		}
	}
}

