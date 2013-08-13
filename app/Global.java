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
			Output.mkdir();
			
			//Layer_Base layer;
			//int width, height;
			// Rotation
			//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
			//layer = Layer_Base.getLayer("Rotation");
			//width = layer.getWidth();
			//height = layer.getHeight();
			
			//Selection selection = new Selection(width, height);
		
			// Corn and Grass Production for D
			//Model_Corn_Grass_Production Model_CGD = new Model_Corn_Grass_Production();
			//TwoArrays ArrayD = Model_CGD.Corn_Grass_P(selection, "Default", Rotation);
			//float ArrayCD[] = ArrayD.a;
			//float ArrayGD[] = ArrayD.b;
			
			// Regular Models
			//Model_Ethanol ED = new Model_Ethanol();
			//ED.Ethanol(ArrayCD, ArrayGD, selection, "Default", Rotation);
			
			//Model_Net_Energy NE_D = new Model_Net_Energy();
			//NE_D.Net_Energy(ArrayCD, ArrayGD, selection, "Default", Rotation);
			
			//Model_Net_Income NI_D = new Model_Net_Income();
			//NI_D.Net_Income(ArrayCD, ArrayGD, selection, "Default", Rotation);
			
			//Regular_Functions RF = new Regular_Functions();
			//RF.Regular_Functions(selection, "Default", Rotation);
			
			// Moving Window Models
			//Model_Habitat_Index HI_D = new Model_Habitat_Index();
			//HI_D.Habitat_Index(selection, "Default", Rotation);
			
			//Model_Nitrogen N_D = new Model_Nitrogen();
			//N_D.Nitrogen(selection, "Default", Rotation);
			
			//Model_Phosphorus PH_D = new Model_Phosphorus();
			//PH_D.Phosphorus(selection, "Default", Rotation);
			
			//Model_Pest_Suppression PS_D = new Model_Pest_Suppression();
			//PS_D.Pest_Suppression(selection, "Default", Rotation);
			
			//Model_Pollinator PO_D = new Model_Pollinator();
			//PO_D.Pollinator(selection, "Default", Rotation);
			
			//Moving_Window_Functions MWF = new Moving_Window_Functions();
			//MWF.Moving_Window_Functions(selection, "Default", Rotation);
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
//				layer = new Layer_Continuous("roads"); layer.init();
				
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

