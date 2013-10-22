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
	public static Result getAssumptions() throws Exception 
	{
		// Get the ArrayNodes of all assumption defaults so the client knows...
		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("Assumptions", GlobalAssumptions.getAssumptionDefaultsForClient());
		
		return ok(sendback);
	}

	//----------------------------------------------------------------------
	public static Result getClientID() throws Exception 
	{
		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("DSS_clientID", RandomString.get(6));
		
		return ok(sendback);
	}
	
	//----------------------------------------------------------------------
	public static Result setUpScenario() throws Exception
	{
		Logger.info("----- Initializing scenario ----");
		// Create a new scenario and get a transformed crop rotation layer from it...
		JsonNode request = request().body().asJson();

		String clientID = request.get("clientID").getTextValue();
		String folder = "client_" + clientID;
		
		Scenario scenario = new Scenario();
		scenario.setAssumptions(request);
		scenario.getTransformedRotation(request);
		scenario.mOutputDir = folder;
		
		String cacheID = Scenario.cacheScenario(scenario, clientID);

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("scenarioID", cacheID);
		
		return ok(sendback);
	}
	
	//----------------------------------------------------------------------
	public static Result runModelCluster() throws Exception 
	{
		Logger.info("----- Model Cluster Process Started ----");

		JsonNode request = request().body().asJson();
		
		// Rotation
		Layer_Base layer = Layer_Base.getLayer("cdl_2012");
		int[][] defaultRotation = layer.getIntData();
		int width = layer.getWidth(), height = layer.getHeight();
		
		Scenario scenario = Scenario.getCachedScenario(request.get("scenarioID").getTextValue());
		
		// TODO: validate that a scenario was found?
		
		String modelType = request.get("modelType").getTextValue();
		List<ModelResult> results = null;
		
		boolean bAnalyzeAll = false;
		
		if (modelType.equals("yield")) {
			Model_EthanolNetEnergyIncome_New ethanolEnergyIncome = new Model_EthanolNetEnergyIncome_New();
			results = ethanolEnergyIncome.run(scenario);
		}
		else if (modelType.equals("n_p")) {
			Model_NitrogenPhosphorus_New np = new Model_NitrogenPhosphorus_New();
			results = np.run(scenario);
		}
		else if (modelType.equals("soc")) {
			Model_SoilCarbon_New soc = new Model_SoilCarbon_New();
			results = soc.run(scenario);
		}
		else if (modelType.equals("pest_pol")) {
			Model_PollinatorPestSuppression_New pp = new Model_PollinatorPestSuppression_New();
			results = pp.run(scenario);
			bAnalyzeAll = true;
		}
		else if (modelType.equals("nitrous")) {
			Model_NitrousOxideEmissions_New n20 = new Model_NitrousOxideEmissions_New();
			results = n20.run(scenario);
		}
		
		else {//(modelType.equals("habitat_index")) {
			Model_HabitatIndex_New hi = new Model_HabitatIndex_New();
			results = hi.run(scenario);
			bAnalyzeAll = true;
		}
		
		// SendBack to Client
		ObjectNode sendBack  = JsonNodeFactory.instance.objectNode();
		
		if (results != null) {
			Analyzer_Histogram histogram = null;
			if (bAnalyzeAll) {
				histogram = new Analyzer_Histogram(new Selection(width, height));
			}
			else {
				histogram = new Analyzer_Histogram(scenario.mSelection);
			}
			
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
					Logger.info("Layer was null: " + compareTo.toString());
					sendBack.put(res.mName, histogram.run(compareTo, res.mWidth, res.mHeight, res.mRasterData));
				}
				else {
					// other layer should be in memory, try to compare with that.
					float[][] data1 = layer.getFloatData();
					if (data1 == null) {
						Logger.info("could not get layer in runModelCluster");
					}
					else {
						Logger.info("Layer was normal: ");
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
		// subtype can be:
		//	equal - equal interval map
		//	quantile - quantiled...
		String subtype = request.get("subtype").getTextValue();

		if (type == null) {
			Logger.info("Tried to find a heatmap 'type' key but didn't. Assuming 'delta'");
			type = "delta";
		}
		
		String clientID = request.get("clientID").getTextValue();
		String folder = "client_" + clientID;

		String path1 = "./layerData/default/" + model + ".dss";
		String path2 = "./layerData/" + folder + "/" + model + ".dss";
		
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
			if (subtype.equals("equal")) {
				sendBack = Analyzer_Heatmap.runEqualInterval(file1, file2, 
							outputFile, 
							10);
			}
			else {
				sendBack = Analyzer_Heatmap.runQuantile(file1, file2, 
							outputFile, 
							10);
			}
		}
		else if (type.equals("file1")) { // ONE file absolute map
			sendBack = Analyzer_Heatmap.runAbsolute(file1, 
							outputFile, 
							10);
		}
		else if (type.equals("file2")) { // ONE file absolute map
			sendBack = Analyzer_Heatmap.runAbsolute(file2, 
							outputFile, 
							10);
		}

		sendBack.put("heatFile", outputFile.getName());
		return ok(sendBack);
	}
}

