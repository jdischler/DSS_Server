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
	
	//--------------------------------------------------------------------------
	@Override
	public void onStart(play.Application app) 
	{
		// Test of heatmapping...
//		Png.createHeatMap_I("Nitrogen", "Default", "Client_ID"); 
 
		systemReport("Application has started");
		cacheLayers();
		systemReport("Data Layers Cached");
				
		File Output = new File("./layerData/Default");
		if(!Output.exists())
		{
			//Output.mkdir();
			
			//Layer_Base layer;
			//int width, height;
			
			// Rotation
			//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
			//layer = Layer_Base.getLayer("Rotation");
			//width = layer.getWidth();
			//height = layer.getHeight();
			
			//Selection selection = new Selection(width, height);

			// Corn and Grass Production for D
			//Model_Crop_Yield Model_CGD = new Model_Crop_Yield();
			//FourArrays ArrayD = Model_CGD.Crop_Y(selection, "Default", Rotation);
			//float ArrayCD[] = ArrayD.a;
			//float ArrayGD[] = ArrayD.b;
			//float ArraySD[] = ArrayD.c;
			//float ArrayAD[] = ArrayD.d;
			
			// Regular Models
			// Model_Ethanol
			//Model_Ethanol E_D = new Model_Ethanol();
			//E_D.Ethanol(ArrayCD, ArrayGD, ArraySD, ArrayAD, selection, "Default", Rotation);
	
			// Model_Net_Energy
			//Model_Net_Energy NE_D = new Model_Net_Energy();
			//NE_D.Net_Energy(ArrayCD, ArrayGD, ArraySD, ArrayAD, selection, "Default", Rotation);
	
			// Model_Net_Income
			//Model_Net_Income NI_D = new Model_Net_Income();
			//NI_D.Net_Income(ArrayCD, ArrayGD, ArraySD, ArrayAD, selection, "Default", Rotation);
	
			// Models with Moving Window
			// Model_Habitat_Index
			//Model_Habitat_Index HI_D = new Model_Habitat_Index();
			//HI_D.Habitat_Index(selection, "Default", Rotation);
			
			// Model_Pest_Suppression
			//Model_Pest_Suppression PS_D = new Model_Pest_Suppression();
			//PS_D.Pest_Suppression(selection, "Default", Rotation);
	
			// Model_Pollinator
			//Model_Pollinator PO_D = new Model_Pollinator();
			//PO_D.Pollinator(selection, "Default", Rotation);
			
			//Model_Nitrogen_Phosphorus
			//Model_Nitrogen_Phosphorus N_P_D = new Model_Nitrogen_Phosphorus();
			//N_P_D.Nitrogen_Phosphorus(selection, "Default", Rotation);

		}
			
			// Run Default for Each Model
			//Models_Default_T MD = new Models_Default_T();
			//MD.Calculate_T("Default_T");
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
	
	// Only tries to load a layer if it isn't in memory already
	//--------------------------------------------------------------------------
	private void cacheLayers() 
	{
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
		finally {
		}
	}
}

