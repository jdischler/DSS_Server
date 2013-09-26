package controllers;

import util.*;

import java.util.*;
import java.io.*;
import java.net.*;

import play.*;
import play.mvc.*;
import play.Logger;
import play.cache.*;

import views.html.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import javax.xml.bind.DatatypeConverter;

//------------------------------------------------------------------------------
public class Application extends Controller 
{
	static int mHeatCount = 0;
	
	//--------------------------------------------------------------------------
	public static Result index() 
	{
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception 
	{
		Query query = new Query();
		JsonNode result = query.selection(request().body().asJson());
		return ok(result);
	}

	//--------------------------------------------------------------------------
	public static Result layerParmRequest() throws Exception 
	{
		JsonNode request = request().body().asJson();

		JsonNode ret = Layer_Base.getParameter(request);
		if (ret != null) 
		{
			return ok(ret);
		}
		
		return ok(); // derp, not really OK if gets here...or??
	}
	
	//----------------------------------------------------------------------
	public static Result wmsRequest() 
	{
		// Open up request from client...
		JsonNode request = request().body().asJson();

		Logger.info(request.toString());
		// e.g., 'Vector:Watersheds-C'
		String layerName = request.get("layer").getTextValue();
		int x = request.get("x").getIntValue(); // 585
		int y = request.get("y").getIntValue(); // 273
		int width = request.get("width").getIntValue();
		int height = request.get("height").getIntValue();
		String bbox = request.get("bbox").getTextValue();

		BufferedReader rd = null;
		OutputStreamWriter wr = null;
		
		try 
		{
			URL url = new URL("http://pgis.glbrc.org:8080/geoserver/Vector/wms" + 
				"?LAYERS=" + layerName + "&QUERY_LAYERS=" + layerName + 
				"&STYLES=&SERVICE=WMS&VERSION=1.1.1&SRS=EPSG:900913" +
				"&REQUEST=GetFeatureInfo&FEATURE_COUNT=10&INFO_FORMAT=application/vnd.ogc.gml/3.1.1" +
				"&BBOX=" + bbox +
				"&HEIGHT=" + Integer.toString(height) + 
				"&WIDTH=" + Integer.toString(width) +
				"&X=" + Integer.toString(x) + 
				"&Y=" + Integer.toString(y));
		
			Logger.info("------------------------------");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.flush();
			
			// get the response
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line = rd.readLine();
			if (line != null) {
				Logger.info(line);
				String line1 = rd.readLine();
				if (line1 != null) {
					return ok(line1);
				}
			}
			
			wr.close();
			rd.close();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info("WMS request failed");
		// TODO: return NOT ok?
		return ok();
	}

	//----------------------------------------------------------------------
	public static JsonNode createAssumption(String category, String icon,
						String variableName, 
						String displayName, float defaultValue) 
	{
	
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("Category", category);
		node.put("Icon", icon);
		node.put("VariableName", variableName);
		node.put("DisplayName", displayName);
		node.put("DefaultValue", defaultValue);

		return node;		
	}
	
	// FIXME: TODO: read from ./serverData/assumptions/assumptions.dat
	//----------------------------------------------------------------------
	public static Result getAssumptions() throws Exception 
	{
				
		ArrayNode array = JsonNodeFactory.instance.arrayNode();
		array.add(createAssumption("Economic", "", "p_corn", "Corn Price", 120.0f));
		array.add(createAssumption("Economic", "", "p_grass", "Grass Price", 100.0f));
		array.add(createAssumption("Economic", "economic_icon.png", "p_eth", "Ethanol Price", 300.0f));
		
		array.add(createAssumption("Conversion Rates", "", "conv_corn", "Corn Grain", 1.8f));
		array.add(createAssumption("Conversion Rates", "", "conv_stover", "Stover", 1.4f));
		array.add(createAssumption("Conversion Rates", "scenario_icon.png", "conv_grass", "Grass", 1.2f));
		
		array.add(createAssumption("Climate", "", "av_temp", "Average Temperature", 76.1f));
		array.add(createAssumption("Climate", "climate_icon.png", "av_rain", "Average Rainfall", 19.2f));
		
		array.add(createAssumption("Other", "policy_icon.png", "temp", "Dunno", 1));

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("Assumptions", array);
		
		return ok(sendback);
	}

	//----------------------------------------------------------------------
	public static Result setUpScenario() throws Exception
	{
		Logger.info("----- Initializing scenario ----");
		// Create a new scenario and get a transformed crop rotation layer from it...
		Scenario scenario = new Scenario();
		scenario.getTransformedRotation(request().body().asJson());
		
		String cacheID = Scenario.cacheScenario(scenario, 12345); // FIXME: real clientID

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("scenarioID", cacheID);
		
		return ok(sendback);
	}
	
	//----------------------------------------------------------------------
	public static Result Models() throws Exception 
	{
		
		
long modelTimeStart = System.currentTimeMillis();

		Logger.info("----- Model Process Started ----");
		//Logger.info("Called into Model:");
		//Logger.info(request().body().asJson().toString());
		
		File output = new File("./layerData/Client_ID");
		if(output.exists())
		{
			Logger.info("This folder already exists");
		}
		else
		{
			output.mkdir();
		}
		
		Layer_Base layer;
		int width, height;
		// Rotation
		int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();

		
long timeStart = System.currentTimeMillis();
		
		//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		// Select the entire landscape before transform
		Selection selectionD = new Selection(width, height);

		// Create a new scenario and get a transformed crop rotation layer from it...
		Scenario scenario = new Scenario();
		scenario.getTransformedRotation(request().body().asJson());
		// Select based on user selection after transform
		Selection selectionT = scenario.mSelection;
		//Logger.info("Server Run The Models Request:");
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Transformation timing: " + Float.toString(timeSec));
		

timeStart = System.currentTimeMillis();
		
		// Model_Soil_Carbon
		// Default and Transform
		Model_Soil_Carbon Model_SOC_T = new Model_Soil_Carbon();
		TwoArrays Array_SOC = Model_SOC_T.Soil_Carbon(selectionD, Rotation, scenario.mNewRotation);
		float SOC_D[] = Array_SOC.a;
		float SOC_T[] = Array_SOC.b;
		float Min_SOC_D = Array_SOC.Min_a;
		float Max_SOC_D = Array_SOC.Max_a;
		float Min_SOC_T = Array_SOC.Min_b;
		float Max_SOC_T = Array_SOC.Max_b;
		
		// Selection for default
		Model_Selection SOCD = new Model_Selection();
		JsonNode SendBack_SOCD = SOCD.Selection(selectionD, selectionT, SOC_D);
		// Selection for transform
		Model_Selection SOCT = new Model_Selection();
		JsonNode SendBack_SOCT = SOCT.Selection(selectionD, selectionT, SOC_T);
		
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Soil Carbon timing:  " + Float.toString(timeSec));


timeStart = System.currentTimeMillis();
		// Model_Nitrous_Oxide_Emissions
		// Default
		Model_Nitrous_Oxide_Emissions NOED = new Model_Nitrous_Oxide_Emissions();
		float ArrayNOE_D[] = NOED.Nitrous_Oxide_Emissions(selectionD, Rotation, "folder");
		Model_Selection NOE_D = new Model_Selection();
		JsonNode SendBack_NOED = NOE_D.Selection(selectionD, selectionT, ArrayNOE_D);
		ArrayNOE_D = null;
		
		// Transform
		Model_Nitrous_Oxide_Emissions NOET = new Model_Nitrous_Oxide_Emissions();
		float ArrayNOE_T[] = NOET.Nitrous_Oxide_Emissions(selectionD, scenario.mNewRotation, "folder");
		Model_Selection NOE_T = new Model_Selection();
		JsonNode SendBack_NOET = NOE_T.Selection(selectionD, selectionT, ArrayNOE_T);
		ArrayNOE_T = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model Nitrous Oxide Emissions timing: " + Float.toString(timeSec));


timeStart = System.currentTimeMillis();
		// Corn and Grass Production for D
		Model_Crop_Yield Model_CGD = new Model_Crop_Yield();
		float YID[] = Model_CGD.Crop_Y(selectionD, Rotation);
		
		// Regular Models
		// Default
		Model_Ethanol_Net_Energy_Income ENENID = new Model_Ethanol_Net_Energy_Income();
		ThreeArrays ArrayD = ENENID.Ethanol_Net_Energy_Income(YID, selectionD, Rotation, "folder");
		float ArrayED[] = ArrayD.a;
		float ArrayNED[] = ArrayD.b;
		float ArrayNID[] = ArrayD.c_float;
		float Min_ED = ArrayD.Min_a;
		float Max_ED = ArrayD.Max_a;
		float Min_NED = ArrayD.Min_b;
		float Max_NED = ArrayD.Max_b;
		float Min_NID = ArrayD.Min_c;
		float Max_NID = ArrayD.Max_c;
		
		Model_Selection ED = new Model_Selection();
		JsonNode SendBack_ED = ED.Selection(selectionD, selectionT, ArrayED);
		Model_Selection NED = new Model_Selection();
		JsonNode SendBack_NED = NED.Selection(selectionD, selectionT, ArrayNED);
		Model_Selection NID = new Model_Selection();
		JsonNode SendBack_NID = NID.Selection(selectionD, selectionT, ArrayNID);
		ArrayD = null;
		ArrayNID = null;
		ArrayED = null;
		ArrayNED = null;
		YID = null;
		System.gc();
		
		// Corn and Grass Production for T
		Model_Crop_Yield Model_CGT = new Model_Crop_Yield();
		float YIT[] = Model_CGT.Crop_Y(selectionD, scenario.mNewRotation);
		
		// Transform
		Model_Ethanol_Net_Energy_Income ENENIT = new Model_Ethanol_Net_Energy_Income();
		ThreeArrays ArrayT = ENENIT.Ethanol_Net_Energy_Income(YIT, selectionD, scenario.mNewRotation, "folder");
		float ArrayET[] = ArrayT.a;
		float ArrayNET[] = ArrayT.b;
		float ArrayNIT[] = ArrayT.c_float;
		float Min_ET = ArrayT.Min_a;
		float Max_ET = ArrayT.Max_a;
		float Min_NET = ArrayT.Min_b;
		float Max_NET = ArrayT.Max_b;
		float Min_NIT = ArrayT.Min_c;
		float Max_NIT = ArrayT.Max_c;
		
		Model_Selection ET = new Model_Selection();
		JsonNode SendBack_ET = ET.Selection(selectionD, selectionT, ArrayET);
		Model_Selection NET = new Model_Selection();
		JsonNode SendBack_NET = NET.Selection(selectionD, selectionT, ArrayNET);
		Model_Selection NIT = new Model_Selection();
		JsonNode SendBack_NIT = NIT.Selection(selectionD, selectionT, ArrayNIT);
		ArrayT = null;
		ArrayET = null;
		ArrayNET = null;
		ArrayNIT = null;
		YIT = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Ethanol / Energy / Income timing: " + Float.toString(timeSec));
		
		
timeStart = System.currentTimeMillis();
		// Model_Habitat_Index
		// Default
		Model_Habitat_Index HID = new Model_Habitat_Index();
		float ArrayHI_D[] = HID.Habitat_Index(selectionD, Rotation);
		Model_Selection HIDS = new Model_Selection();
		JsonNode SendBack_HID = HIDS.Selection(selectionD, selectionT, ArrayHI_D);
		ArrayHI_D = null;
		
		// Transform
		Model_Habitat_Index HIT = new Model_Habitat_Index();
		float ArrayHI_T[] = HIT.Habitat_Index(selectionD, scenario.mNewRotation);
		Model_Selection HITS = new Model_Selection();
		JsonNode SendBack_HIT = HITS.Selection(selectionD, selectionT, ArrayHI_T);
		ArrayHI_T = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Habitat Index timing: " + Float.toString(timeSec));
		
		
timeStart = System.currentTimeMillis();
		// Models at watershed scale for Nitrogen and Phosphorus
		// Default
		Model_Nitrogen_Phosphorus N_P_D = new Model_Nitrogen_Phosphorus();
		ThreeArrays ArrayN_P_D = N_P_D.Nitrogen_Phosphorus(selectionD, Rotation, "folder");
		float ArrayND[] = ArrayN_P_D.a;
		float ArrayPHD[] = ArrayN_P_D.b;
		int TotalD[] = ArrayN_P_D.c_int;
		float Min_ND = ArrayN_P_D.Min_a;
		float Max_ND = ArrayN_P_D.Max_a;
		float Min_PHD = ArrayN_P_D.Min_b;
		float Max_PHD = ArrayN_P_D.Max_b;
		float Min_TD = ArrayN_P_D.Min_c;
		float Max_TD = ArrayN_P_D.Max_c;
		
		Model_Selection_N_P ND = new Model_Selection_N_P();
		JsonNode SendBack_ND = ND.Selection_N_P(ArrayND, TotalD);
		Model_Selection_N_P PHD = new Model_Selection_N_P();
		JsonNode SendBack_PHD = PHD.Selection_N_P(ArrayPHD, TotalD);
		ArrayN_P_D = null;
		ArrayND = null;
		ArrayPHD = null;
		TotalD = null;
		System.gc();
		
		// Transform
		Model_Nitrogen_Phosphorus N_P_T = new Model_Nitrogen_Phosphorus();
		ThreeArrays ArrayN_P_T = N_P_T.Nitrogen_Phosphorus(selectionD, scenario.mNewRotation, "folder");
		float ArrayNT[] = ArrayN_P_T.a;
		float ArrayPHT[] = ArrayN_P_T.b;
		int TotalT[] = ArrayN_P_T.c_int;
		float Min_NT = ArrayN_P_T.Min_a;
		float Max_NT = ArrayN_P_T.Max_a;
		float Min_PHT = ArrayN_P_T.Min_b;
		float Max_PHT = ArrayN_P_T.Max_b;
		float Min_TT = ArrayN_P_T.Min_c;
		float Max_TT = ArrayN_P_T.Max_c;
		
		Model_Selection_N_P NT = new Model_Selection_N_P();
		JsonNode SendBack_NT = NT.Selection_N_P(ArrayNT, TotalT);
		Model_Selection_N_P PHT = new Model_Selection_N_P();
		JsonNode SendBack_PHT = PHT.Selection_N_P(ArrayPHT, TotalT);
		ArrayN_P_T = null;
		ArrayNT = null;
		ArrayPHT = null;
		TotalT = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Nitrogen / Phosphorus timing: " + Float.toString(timeSec));
		

timeStart = System.currentTimeMillis();
		// Model_Pollinator_Pest_Suppression
		// Default
		Model_Pollinator_Pest_Suppression PPS_D = new Model_Pollinator_Pest_Suppression();
		TwoArrays ArrayP_PS_D = PPS_D.Pollinator_Pest_Suppression(selectionD, Rotation);
		float ArrayPOD[] = ArrayP_PS_D.a;
		float ArrayPSD[] = ArrayP_PS_D.b;
		float Min_POD = ArrayP_PS_D.Min_a;
		float Max_POD = ArrayP_PS_D.Max_a;
		float Min_PSD = ArrayP_PS_D.Min_b;
		float Max_PSD = ArrayP_PS_D.Max_b;
		
		Model_Selection POD = new Model_Selection();
		JsonNode SendBack_POD = POD.Selection(selectionD, selectionT, ArrayPOD);
		Model_Selection PSD = new Model_Selection();
		JsonNode SendBack_PSD = PSD.Selection(selectionD, selectionT, ArrayPSD);
		ArrayP_PS_D = null;
		ArrayPOD = null;
		ArrayPSD = null;
		System.gc();
		
		// Transform
		Model_Pollinator_Pest_Suppression PPS_T = new Model_Pollinator_Pest_Suppression();
		TwoArrays ArrayP_PS_T = PPS_T.Pollinator_Pest_Suppression(selectionD, scenario.mNewRotation);
		float ArrayPOT[] = ArrayP_PS_T.a;
		float ArrayPST[] = ArrayP_PS_T.b;
		float Min_POT = ArrayP_PS_T.Min_a;
		float Max_POT = ArrayP_PS_T.Max_a;
		float Min_PST = ArrayP_PS_T.Min_b;
		float Max_PST = ArrayP_PS_T.Max_b;
		
		Model_Selection POT = new Model_Selection();
		JsonNode SendBack_POT = POT.Selection(selectionD, selectionT, ArrayPOT);
		Model_Selection PST = new Model_Selection();
		JsonNode SendBack_PST = PST.Selection(selectionD, selectionT, ArrayPST);
		ArrayP_PS_T = null;
		ArrayPOT = null;
		ArrayPSD = null;
		System.gc();

timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Pollinator / Pest timing: " + Float.toString(timeSec));


		// SendBack to Client
		ObjectNode SendBack  = JsonNodeFactory.instance.objectNode();
		ObjectNode SendBackD = JsonNodeFactory.instance.objectNode();
		ObjectNode SendBackT = JsonNodeFactory.instance.objectNode();
		
		SendBackD.put("Ethanol", SendBack_ED);
		SendBackT.put("Ethanol", SendBack_ET);
		SendBackD.put("Net_Energy", SendBack_NED);
		SendBackT.put("Net_Energy", SendBack_NET);
		SendBackD.put("Net_Income", SendBack_NID);
		SendBackT.put("Net_Income", SendBack_NIT);
		SendBackD.put("Habitat_Index", SendBack_HID);
		SendBackT.put("Habitat_Index", SendBack_HIT);
		SendBackD.put("Nitrogen", SendBack_ND);
		SendBackT.put("Nitrogen", SendBack_NT);
		SendBackD.put("Phosphorus", SendBack_PHD);
		SendBackT.put("Phosphorus", SendBack_PHT);
		SendBackD.put("Pest_Suppression", SendBack_PSD);
		SendBackT.put("Pest_Suppression", SendBack_PST);
		SendBackD.put("Pollinator", SendBack_POD);
		SendBackT.put("Pollinator", SendBack_POT);
		SendBackD.put("Soil_Carbon", SendBack_SOCD);
		SendBackT.put("Soil_Carbon", SendBack_SOCT);
		SendBackD.put("Nitrous_Oxide_Emissions", SendBack_NOED);
		SendBackT.put("Nitrous_Oxide_Emissions", SendBack_NOET);
		
		SendBack.put(  "Default", SendBackD);
		SendBack.put("Transform", SendBackT);
		
		Logger.info(SendBackD.toString());
		Logger.info(SendBackT.toString());
		
long modelTimeEnd = System.currentTimeMillis();
timeSec = (modelTimeEnd - modelTimeStart) / 1000.0f;
Logger.info("---- Model Process Finished ----");
Logger.info("   Model total time: " + Float.toString(timeSec) + "s");
		
		return ok(SendBack);
	}

	//----------------------------------------------------------------------
	public static Result runModelCluster() throws Exception 
	{
		Logger.info("----- Model Cluster Process Started ----");

		JsonNode request = request().body().asJson();
		
		// Rotation
		int[][] defaultRotation = Layer_Base.getLayer("Rotation").getIntData();
		Layer_Base layer = Layer_Base.getLayer("Rotation");
		int width = layer.getWidth(), height = layer.getHeight();
		
		Scenario scenario = Scenario.getCachedScenario(request.get("scenarioID").getTextValue());
		
		String modelType = request.get("modelType").getTextValue();
		List<ModelResult> results = null;
		
		if (modelType.equals("yield")) {
			Model_EthanolNetEnergyIncome_New ethanolEnergyIncome = new Model_EthanolNetEnergyIncome_New();
			results = ethanolEnergyIncome.run(scenario.mNewRotation, width, height, "clientID");
		}
		else if (modelType.equals("n_p")) {
			Model_NitrogenPhosphorus_New np = new Model_NitrogenPhosphorus_New();
			results = np.run(scenario.mNewRotation, width, height, "clientID");
		}
		else if (modelType.equals("soc")) {
			Model_SoilCarbon_New soc = new Model_SoilCarbon_New();
			results = soc.run(scenario.mNewRotation, width, height, "clientID");
		}
		else if (modelType.equals("pest_pol")) {
			Model_PollinatorPestSuppression_New pp = new Model_PollinatorPestSuppression_New();
			results = pp.run(scenario.mNewRotation, width, height, "clientID");
		}
		else if (modelType.equals("nitrous")) {
			Model_NitrousOxideEmissions_New n20 = new Model_NitrousOxideEmissions_New();
			results = n20.run(scenario.mNewRotation, width, height, "clientID");
		}
		
		else {//(modelType.equals("habitat_index")) {
			Model_HabitatIndex_New hi = new Model_HabitatIndex_New();
			results = hi.run(scenario.mNewRotation, width, height, "clientID");
		}
		
		// SendBack to Client
		ObjectNode sendBack  = JsonNodeFactory.instance.objectNode();
		
		if (results != null) {
			Analyzer_Histogram histogram = new Analyzer_Histogram(scenario.mSelection);
			
			for (int i = 0; i < results.size(); i++) {
				
				ModelResult res = results.get(i);
				
				// Try to an in-memory compare of (usually) default...
				//	if layer is not in memory, try doin a file-based compare
				Logger.info("Procesing results for " + res.mName);
				
				String defaultCompare = "default/" + res.mName;
				 // CRUTCH up SOC layer since it is not in DEFAULT/SOC in memory...
				 if (modelType.equals("soc")) {
					defaultCompare = "soc";
				}
				layer = Layer_Base.getLayer(defaultCompare);
				
				if (layer == null) {
					// No layer data, try compare as a file...
					// TODO: Add crutching up for SOC comparisons? will not be in /default....!
					File compareTo = new File("./layerData/default/" + res.mName + ".dss");
					sendBack.put(res.mName, histogram.run(compareTo, res.mWidth, res.mHeight, res.mRasterData));
				}
				else {
					// other ayer should be in memory, try to compare with that.
					float[][] data1 = layer.getFloatData();
					if (data1 == null) {
						Logger.info("could not get layer in runModelCluster");
					}
					else {
						sendBack.put(res.mName, histogram.run(res.mWidth, res.mHeight, data1, res.mRasterData));
					}
				}
			}
		}
		Logger.info("Done processing list of results, queuing results for file writer");
		QueuedWriter.queueResults(results);

		Logger.info(sendBack.toString());
		return ok(sendBack);
	}
	
	//----------------------------------------------------------------------
	public static Result getHeatmap() throws Exception 
	{
		// Open up request from client...
		JsonNode request = request().body().asJson();
		Logger.info("---- getHeatmap request ----");
		Logger.info(request.toString());
		
		// model can be: (TODO: verify list)
		//	habitat_index, soc, nitrogen, phosphorus, pest, pollinator(s?), net_energy,
		//		net_income, ethanol, nitrous_oxide
		String model = request.get("model").getTextValue();

		if (model == null) {
			Logger.info("Tried to find a model data file but none was passed. Aborting heatmap.");
			return ok(); // FIXME: not ok.
		}

		// type can be: 
		//	delta - shows change between file1 and file2
		//	file1 - shows file1 as an absolute map
		//	file2 - shows file2 as an absolute map		
		String type = request.get("type").getTextValue();

		if (type == null) {
			Logger.info("Tried to find a heatmap 'type' key but didn't. Assuming 'delta'");
			type = "delta";
		}
		
		String path1 = "./layerData/default/" + model + ".dss";
		String path2 = "./layerData/clientID/" + model + ".dss";
		
		// BLURF, crutching up for SOC layer being handled/stored kind of differntly...
		if (model.equals("soc")) {
			path1 = "./layerData/soc.dss";
		}
		
		File file1 = new File(path1);
		File file2 = new File(path2);
		
		if (type.equals("delta")) {
			// for 'delta' type, both files must exist!
			if (!file1.exists() || !file2.exists()) {
				Logger.info("Wanted to open files for 'delta' heatmap but one of the files did not exist");
				return ok(); // FIXME: not ok.
			}
		}
		else if (type.equals("file1")) {
			if (!file1.exists()) {
				Logger.info("Wanted to open file for 'file1' heatmap but that file did not exist");
				return ok(); // FIXME: not ok.
			}
		}
		else if (type.equals("file2")) {
			if (!file2.exists()) {
				Logger.info("Wanted to open file for 'file2' heatmap but that file did not exist");
				return ok(); // FIXME: not ok.
			}
		}
		else {
			Logger.info("Error, unknown heatmap type: <" + type + ">");
			return ok(); // FIXME: not ok.
		}

		String outputPath = "/public/file/heat_max_" + model + "_" + Integer.toString(mHeatCount++) + ".png";
		
		// FIXME: not sure why play doesn't hand me back the expected directory path in production?
		if (Play.isProd()) {
			// FIXME: blugh, like this won't be totally fragile? :)
			outputPath = "/target/scala-2.10/classes" + outputPath;
		}
		outputPath = "." + outputPath;
		
		File outputFile = new File(outputPath);
		ObjectNode sendBack = null;
		
		if (type.equals("delta")) { // TWO file heatmap
			sendBack = Analyzer_Heatmap.run(file1, file2, 
							outputFile, 
							10);
		}
		else if (type.equals("file1")) { // ONE file absolute map
			sendBack = Analyzer_Heatmap.run(file1, 
							outputFile, 
							10);
		}
		else if (type.equals("file2")) { // ONE file absolute map
			sendBack = Analyzer_Heatmap.run(file2, 
							outputFile, 
							10);
		}

		sendBack.put("heatFile", outputFile.getName());
		return ok(sendBack);
	}
}

