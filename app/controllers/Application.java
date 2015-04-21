package controllers;

import util.*;

import java.util.*;
import java.io.*;
import java.net.*;
import java.math.BigInteger;


import play.*;
import play.mvc.*;
import play.mvc.Http.Context;
import play.Logger;
import play.cache.*;

import views.html.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import org.apache.commons.io.FileUtils; 
import org.apache.commons.io.filefilter.*; 

//import org.codehaus.jackson.*;
//import org.codehaus.jackson.node.*;
import javax.xml.bind.DatatypeConverter;


import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import com.avaje.ebean.Ebean;


//------------------------------------------------------------------------------
public class Application extends Controller 
{
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}
	
	static int mHeatCount = 0;
	
	//--------------------------------------------------------------------------
	public static String getVersion() {
		play.Configuration config = Play.application().configuration();
		return config.getString("application.version");
    }

	//--------------------------------------------------------------------------
	public static String getApplicationName() {
		play.Configuration config = Play.application().configuration();
		return config.getString("application.name");
    }
    
    // returns NULL if no valid string key exists
	//--------------------------------------------------------------------------
    private static String safeGetString(JsonNode request, String key) {
    
    	String result = null;
    	if (request != null && key != null) {
    		JsonNode node = request.get(key);
    		if (node != null) {
    			result = node.textValue();
    		}
    	}

		return result;
    }
    
    // returns NULL if no valid string key exists
	//--------------------------------------------------------------------------
    private static Boolean safeGetBoolean(JsonNode request, String key) {
    
    	Boolean result = null;
    	if (request != null && key != null) {
    		JsonNode node = request.get(key);
    		if (node != null) {
    			result = node.booleanValue();
    		}
    	}

		return result;
    }
    
	//--------------------------------------------------------------------------
	public static Result index() 
	{
		// Prevent lingering sessions. Though since this is here, the client doesn't check for existing sessions
		//	so the login/logout button may not be in the correct state if this were removed.
		session().clear();
		
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception 
	{
		// NOTE: user can legally be null if they have not registered/logged-in
		ClientUser user = null;
		String userEmail = Context.current().session().get("email");
		if (userEmail != null) {
			user = ClientUser.find.where()
					.eq("email", userEmail)
					.findUnique();
		}

		Query query = new Query();
		JsonNode result = query.selection(request().body().asJson(), user);
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
		
		return badRequest(); // TODO: add return errors if needed...
	}
	
	//--------------------------------------------------------------------------
	public static Result openLayersProxy() 
	{
		BufferedReader rd = null;
		OutputStreamWriter wr = null;
		String charset = "UTF-8";
		String desiredStart = "<wfs:GetFeature";
		String desiredEnd = "</wfs:GetFeature>";
		
		try {
			String getUrl = request().uri().replace("/openLayersProxy?", "");
			getUrl = URLDecoder.decode(getUrl, charset);
				
			String body = request().body().toString();
			int dataStart = body.indexOf(desiredStart);
			int dataEnd = body.indexOf(desiredEnd);
			
			if (dataStart >= 0 && dataEnd >= 0 || dataEnd > dataStart)
			{
				String result = body.substring(dataStart, dataEnd + desiredEnd.length());
				
//				result = URLEncoder.encode(result, charset);
//				Logger.info(" Supposedly is: " + result);
				
				URL url = new URL(getUrl);
				URLConnection conn = url.openConnection();
				conn.setRequestProperty("accept-charset", charset);
				conn.setRequestProperty("content-type", "application/xml");
				conn.setDoOutput(true);
				
				wr = new OutputStreamWriter(conn.getOutputStream(), charset);
				wr.write(result);
				wr.flush();
				
				// get the response
				rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
				String line ="";
				
//				Logger.info("The result:");
				while(rd.ready()) {
					line = rd.readLine();
//					Logger.info(line);
				}
				
				wr.close();
				rd.close();
				return ok(line);
			}
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		Logger.info("WMS request failed");
		return badRequest(); // TODO: add return errors if needed...
	}

	//----------------------------------------------------------------------
	public static Result wmsRequest() 
	{
		// Open up request from client...
		JsonNode request = request().body().asJson();

		Logger.info(request.toString());
		// e.g., 'Vector:Watersheds-C'
		String layerName = safeGetString(request, "layer");
		int x = request.get("x").intValue(); // 585
		int y = request.get("y").intValue(); // 273
		int width = request.get("width").intValue();
		int height = request.get("height").intValue();
		String bbox = safeGetString(request, "bbox");

		BufferedReader rd = null;
		OutputStreamWriter wr = null;
		
		try 
		{
			URL url = new URL("http://pgis.glbrc.org/geoserver/Vector/wms" + 
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
//				Logger.info(line);
				wr.close();
				rd.close();
				return ok(line);
/*				String line1 = rd.readLine();
				if (line1 != null) {
					wr.close();
					rd.close();
					return ok(line1);
				}*/
			}
			
			wr.close();
			rd.close();
		}
		catch (Exception e) {
			Logger.warn(e.toString());
		}
		
		Logger.info("WMS request failed");
		return badRequest(); // TODO: add return errors if needed...
	}

	//----------------------------------------------------------------------
	public static Result getAssumptions() throws Exception 
	{
		// Get the ArrayNodes of all assumption defaults so the client knows...
		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("Assumptions", GlobalAssumptions.getAssumptionDefaultsForClient());
		
		return ok(sendback);
	}

	// TODO: should ultimately have accounts....
	//----------------------------------------------------------------------
	public static Result getClientID() throws Exception 
	{
		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		// FIXME: should ultimately have accounts with log in?
		sendback.put("DSS_clientID", RandomString.get(10));
		
		return ok(sendback);
	}
	
	//----------------------------------------------------------------------
	public static Result setUpScenario() throws Exception
	{
		ClientUser user = null;
		String userEmail = Context.current().session().get("email");
		if (userEmail != null) {
			user = ClientUser.find.where()
					.eq("email", userEmail)
					.findUnique();
		}

		// NOTE: sanity check...
		// Derp, probably needs to be in a process that runs every now and then...
		//	Just to validate that all the code in place is sufficient to not be leaking memory..
		//	And only putting it here for now because this is the main memory using process...
		//	And these are the main offenders that could be holding onto a lot of memory...
		CustomComparison.checkPurgeStaleComparisons();
		Scenario.checkPurgeStaleScenarios();
		
		Logger.info("----- Initializing scenario ----");
		// Create a new scenario and get a transformed crop rotation layer from it...
		JsonNode request = request().body().asJson();

		// TODO: validate that this can't contain anything that could be used as an attack?
		String clientID = safeGetString(request, "clientID");
//		String folder = "client_" + clientID;
		int modelRequestCount = request.get("modelRequestCount").asInt();
		
		int saveID = request.get("saveID").asInt();
		if (saveID < 0) saveID = 0;
		else if (saveID > 9) {
			saveID = 9;
		}
		
		String folder = "client_" + clientID + "/" + Integer.toString(saveID);
		
		// NOTE: user can be null
		Scenario scenario = new Scenario(user);
		scenario.setAssumptions(request);
		scenario.getTransformedRotation(request);
		scenario.mOutputDir = folder;
		
		String cacheID = Scenario.cacheScenario(scenario, clientID, modelRequestCount);

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("scenarioID", cacheID);
		
		QueuedWriter.queueResults(new ScenarioSetupResult(folder, scenario.mNewRotation,
			scenario.mSelection.mRasterData, scenario.mSelection.mWidth, scenario.mSelection.mHeight));

		return ok(sendback);
	}
	
	//----------------------------------------------------------------------
	public static Result runModelCluster() throws Exception 
	{
		Logger.info("----- Model Cluster Process Started ----");

		JsonNode request = request().body().asJson();
		
		String scenarioID = safeGetString(request, "scenarioID");
		Scenario scenario = Scenario.getCachedScenario(scenarioID);
		
		if (scenario == null) {
			return badRequest();
		}
		
		String modelType = safeGetString(request, "modelType");
		List<ModelResult> results = null;
		
		if (modelType.equals("yield")) {
			Model_EthanolNetEnergyIncome model = new Model_EthanolNetEnergyIncome();
			results = model.run(scenario);
		}
		else if (modelType.equals("epic_phosphorus")) {
			Model_P_LossEpic model = new Model_P_LossEpic();
			results = model.run(scenario);
		}
/*		// Model not used from client
		else if (modelType.equals("water_quality")) {
			Model_WaterQuality wq = new Model_WaterQuality();
			results = wq.run(scenario);
		}
*/
		else if (modelType.equals("soc")) {
			Model_SoilCarbon model = new Model_SoilCarbon();
			results = model.run(scenario);
		}
		else if (modelType.equals("pest_pol")) {
			Model_PollinatorPestSuppression model = new Model_PollinatorPestSuppression();
			results = model.run(scenario);
		}
		else if (modelType.equals("nitrous")) {
			Model_NitrousOxideEmissions model = new Model_NitrousOxideEmissions();
			results = model.run(scenario);
		}
		else if (modelType.equals("soil_loss")) {
			Model_Soil_Loss model = new Model_Soil_Loss();
			results = model.run(scenario);
		}
		else if (modelType.equals("habitat_index")) { // Bird habitat
			Model_HabitatIndex model = new Model_HabitatIndex();
			results = model.run(scenario);
		}
		else {
			Logger.error(" Bad model type in runModelCluster: " + modelType);
			return badRequest();
		}
		
		// SendBack to Client
		ObjectNode sendBack  = JsonNodeFactory.instance.objectNode();
		
		if (results != null) {
			Analyzer_HistogramNew histogram = new Analyzer_HistogramNew();
			
			// Try to do an in-memory compare of (usually) default...
			//	if layer is not in memory, try doin a file-based compare
			for (int i = 0; i < results.size(); i++) {
				
				ModelResult res = results.get(i);
				detailedLog("Procesing results for " + res.mName);
				
				String clientID = safeGetString(request, "clientID");
				String clientFolder = "client_" + clientID + "/";
				int compare1ID = request.get("compare1ID").asInt(); // -1 is default
				String runFolder = Integer.toString(compare1ID) + "/";
			
				String path1 = "";
				// Asking to compare against DEFAULT?
				if (compare1ID == -1) {
					path1 = "default/" + res.mName;
					
					// See if the layer is in memory (it usually will be unless the server was started
					//	with the DEFAULTS NOT loaded...)
					Layer_Base layer = Layer_Base.getLayer(path1);
					if (layer != null) {
						// other layer is in memory so compare with that.
						float[][] data1 = layer.getFloatData();
						if (data1 == null) {
							Logger.error("could not get layer in runModelCluster");
						}
						else {
							sendBack.put(res.mName, 
								histogram.run(res.mWidth, res.mHeight, data1, scenario.mSelection,
												res.mRasterData, scenario.mSelection));
						}
						continue; // process next result...
					}
				}
				else {
					path1 = clientFolder + runFolder + res.mName;
				}
				
				// Compare to file was not in memory, set up the real path and we'll try to load it for
				//	comparison (which is slower...booo)
				path1 = "./layerData/" + path1 + ".dss";
				sendBack.put(res.mName, 
						histogram.run(new File(path1), scenario.mSelection,
										res.mWidth, res.mHeight, res.mRasterData, scenario.mSelection));
			}
			// decrement ref count and remove it for real if not needed...
			Scenario.releaseCachedScenario(scenarioID);
		}
		detailedLog("Done processing list of results, queuing results for file writer");
		QueuedWriter.queueResults(results);

		detailedLog(sendBack.toString());
		return ok(sendBack);
	}
 
	//----------------------------------------------------------------------
	public static Result initComparison() throws Exception {
		
		Logger.info("----- Initializing for comparison ----");
		JsonNode request = request().body().asJson();

		// TODO: validate that this can't contain anything that could be used as an attack?
		String clientID = safeGetString(request, "clientID");
		String folder = "client_" + clientID;

		int compare1ID = request.get("compare1ID").asInt(); // -1 is default
		int compare2ID = request.get("compare2ID").asInt(); // -1 is default
		int compareCount = request.get("compareCount").asInt(); // number of models, for ref counting
		
		String path1 = "./layerData/";
		if (compare1ID == -1) {
			path1 += "default/";
		}
		else if (compare1ID < 0 || compare1ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path1 += folder + "/" + Integer.toString(compare1ID) + "/";
		}
		String basePath1 = path1;
		path1 += "selection.sel";
		
		String path2 = "./layerData/";
		if (compare2ID == -1) {
			path2 += "default/";
		}
		else if (compare2ID < 0 || compare2ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path2 += folder + "/" + Integer.toString(compare2ID) + "/";
		}
		String basePath2 = path2;
		path2 += "selection.sel";

		Logger.info(" ... Going to custom compare files in:");
		Logger.info("  " + basePath1);
		Logger.info("  " + basePath2);
		
		File file1 = new File(path1);
		File file2 = new File(path2);
		
		boolean failed = false;
		
		if (compare1ID >= 0 && !file1.exists()) { // ALLOW load of selection fail if this is for DEFAULT
			Logger.error(" Error! - file <" + file1.toString() + 
				"> does not exist");
			failed = true;
		}
		if (compare2ID >= 0 && !file2.exists()) { // ALLOW load of selection fail if this is for DEFAULT
			Logger.error(" Error! - file <" + file2.toString() + 
				"> does not exist");
			failed = true;
		}

		if (failed) {
			Logger.error(" Error! - custom comparison aborting.");
			return badRequest(); // TODO: add return errors if needed...
		}
		
		Selection sel1 = null;
		if (compare1ID >= 0) { // DEFAULT Scenario does not have a selection...
			sel1 = new Selection(file1);
		}
		if (sel1 != null && !sel1.isValid) {
			Logger.error(" Error! - load of selection from file <" + file1.toString() + 
				"> failed! Custom comparison aborting.");
			return badRequest(); // TODO: add return errors if needed...
		}
		
		Selection sel2 = null;
		if (compare2ID >= 0) { // DEFAULT Scenario does not have a selection...
			sel2 = new Selection(file2);
		}
		if (sel2 != null && !sel2.isValid) {
			Logger.error(" Error! - load of selection from file <" + file2.toString() + 
				"> failed! Custom comparison aborting.");
			return badRequest(); // TODO: add return errors if needed...
		}
		
		CustomComparison customCompare = new CustomComparison(compareCount, basePath1, sel1, basePath2, sel2);
		
		String cacheID = CustomComparison.cacheCustomComparions(customCompare, clientID);

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("customCompareID", cacheID);
		
		return ok(sendback);
	}

	//----------------------------------------------------------------------
	public static Result runComparison() throws Exception {
		
		Logger.info("----- Custom Comparison Analysis Cluster Process Started ----");

		JsonNode request = request().body().asJson();
		
		String compareID = safeGetString(request, "customCompareID");
		CustomComparison comparison = CustomComparison.getCachedComparison(compareID);
		
		if (comparison == null) {
			return badRequest(); // TODO: return error if needed...
		}
		
		String file = safeGetString(request, "file");

		// SendBack to Client
		ObjectNode sendBack  = JsonNodeFactory.instance.objectNode();
		
		Analyzer_HistogramNew histogram = new Analyzer_HistogramNew();

		// TODO: FIXME: Comparing against DEFAULT should be able to use in-memory results
		//	and be faster due to half as much file accessing?
		File file1 = new File(comparison.mBasePath1 + "/" + file + ".dss");
		File file2 = new File(comparison.mBasePath2 + "/" + file + ".dss");
		
		Logger.info("Basepath1: " + file1.toString());
		Logger.info("Basepath2: " + file2.toString());

		sendBack.put(file, 
				histogram.run(file1, comparison.mSelection1, 
								file2, comparison.mSelection2));
		// Decrement ref count, will actually release when hits zero
		CustomComparison.releaseCachedComparison(compareID);
		
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
		String model = safeGetString(request, "model");

		if (model == null) {
			Logger.warn("Tried to find a model data file but none was passed. Aborting heatmap.");
			return badRequest(); // TODO: add return errors if needed...
		}

		// type can be: 
		//	delta - shows change between file1 and file2
		//	file1 - shows file1 as an absolute map
		//	file2 - shows file2 as an absolute map		
		String type = safeGetString(request, "type");
		// subtype can be:
		//	equal - equal interval map
		//	quantile - quantiled...
		String subtype = safeGetString(request, "subtype");

		if (type == null) {
			Logger.warn("Tried to find a heatmap 'type' key but didn't. Assuming 'delta'");
			type = "delta";
		}
		
		String clientID = request.get("clientID").textValue();
		String folder = "client_" + clientID;

		int compare1ID = request.get("compare1ID").asInt(); // -1 is default, otherwise 0-9, validated below
		int compare2ID = request.get("compare2ID").asInt(); // -1 is default, otherwise 0-9, validated below
	
		String path1 = "./layerData/";
		if (compare1ID == -1) {
			path1 += "default/";
		}
		else if (compare1ID < 0 || compare1ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path1 += folder + "/" + Integer.toString(compare1ID) + "/";
		}
		path1 += model + ".dss";
		
		String path2 = "./layerData/";
		if (compare2ID == -1) {
			path2 += "default/";
		}
		else if (compare2ID < 0 || compare2ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path2 += folder + "/" + Integer.toString(compare2ID) + "/";
		}
		path2 += model + ".dss";
		
		// BLURF, crutching up for SOC layer being handled/stored kind of differently...

		detailedLog("Going to heatmap files:");
		detailedLog("  " + path1);
		detailedLog("  " + path2);
		
		File file1 = new File(path1);
		File file2 = new File(path2);
		
		if (type.equals("delta")) {
			// for 'delta' type, both files must exist!
			if (!file1.exists() || !file2.exists()) {
				Logger.error("Wanted to open files for 'delta' heatmap but one of the files did not exist");
				return badRequest(); // TODO: add return errors if needed...
			}
		}
		else if (type.equals("file1")) {
			if (!file1.exists()) {
				Logger.error("Wanted to open file for 'file1' heatmap but that file did not exist");
				return badRequest(); // TODO: add return errors if needed...
			}
		}
		else if (type.equals("file2")) {
			if (!file2.exists()) {
				Logger.error("Wanted to open file for 'file2' heatmap but that file did not exist");
				return badRequest(); // TODO: add return errors if needed...
			}
		}
		else {
			Logger.error("Error, unknown heatmap type: <" + type + ">");
			return badRequest(); // TODO: add return errors if needed...
		}

		String outputPath = "/public/dynamicFiles/heat_max_" + model + "_" + Integer.toString(mHeatCount++) + ".png";
		
		// FIXME: not sure why play doesn't hand me back the expected directory path in production?
		if (Play.isProd()) {
			// FIXME: blugh, like this won't be totally fragile? :)
//			outputPath = "/target/scala-2.10/classes" + outputPath;
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
		else
		{
			File fileToMap = file1;
			
			if (type.equals("file2")) { // ONE file absolute map
				fileToMap = file2;
			}
			
			if (subtype.equals("equal")) {
				sendBack = Analyzer_Heatmap.runAbsolute(fileToMap, 
								outputFile, 
								10);
			}
			else {
				sendBack = Analyzer_Heatmap.runAbsoluteQuantiled(fileToMap, 
								outputFile, 
								10);
			}
		}

		if (sendBack != null) {
			sendBack.put("heatFile", "/files/" + outputFile.getName());
		}
		return ok(sendBack);
	}
	
	
	
	
	//--------------------------------------------------------------------------
	//
	// Authentication Shtuffs
	//
	//--------------------------------------------------------------------------
	public static String hashPassword(String pwd) {
	  
		MessageDigest md;
		
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException ex) {
			System.out.println(ex.getMessage());
			return null;
		}
		
		md.update(pwd.getBytes());
		byte[] hashPass = md.digest();
		
		return toHex(hashPass);
	}
	  
	//--------------------------------------------------------------------------
	public static String toHex(byte[] array)
	{
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		}
		else {
			return hex;
		}
	}
	
	//--------------------------------------------------------------------------
	// LOGIN
	//--------------------------------------------------------------------------
	public static Result login() throws Exception
	{
		JsonNode request = request().body().asJson();
		String email = safeGetString(request, "user");
		String password = safeGetString(request, "pwd");

		if (email == null || password == null) {
			return badRequest("Your request is not able to be processed at this time");
		}

		ClientUser user = ClientUser.find
			  .where()
			  .eq("email", email)
			  .findUnique();
			  
		if (user == null) {
			session().clear();
			return badRequest("Invalid user or password");
		}

		// We won't have a salt if the users are added via the YAML script,
		//	so don't allow them to log in. Instead, they will have to use the forgot email option. :/
		if (user.passwordSalt == null || user.passwordSalt.length() <= 0) {
			return badRequest("You will need to use the forgotten email option to log in.");
		}
		
		String encryptedPwd = hashPassword(user.passwordSalt + password);
		Logger.info("user's salt = " + user.passwordSalt);
		Logger.info("encryptedPwd = " + encryptedPwd);
		if(!encryptedPwd.equals(user.password)) {
			session().clear();
			return badRequest("Invalid user or password");
		}
		
		session().clear();
		Logger.info("set new session info...");
		session("email", email);
		SessionId sid = new SessionId(email);
		session("uuid", sid.session_id.toString());
		
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		
		// Get all restricted layers
		sendBack.put("restrictedLayers", Layer_Base.getAccessRestrictedLayers());
		// Then also get the list of ones this user could access...
		sendBack.put("unrestrictedLayers", Layer_Base.getAccessibleRestrictedLayers(user.accessFlags));
		// Should client show the admin button? A client will still have to be authenticated and
		//	have admin rights for the admin box to be populated with anything useful....
		sendBack.put("showExtra", user.admin);
		
		return ok(sendBack);
	}
	
	//--------------------------------------------------------------------------
	// INITIAL REGISTRATION
	//--------------------------------------------------------------------------
	public static Result beginRegistration() throws Exception
	{
		JsonNode request = request().body().asJson();
		String email = safeGetString(request, "user");
		String password = safeGetString(request, "pwd");
		String organization = safeGetString(request, "organization");

		if (email == null || password == null) {
			return badRequest("Your request is not able to be processed at this time");
		}
		
		if (organization == null) {
			organization = "";
		}
		
		ClientUser user = ClientUser.find
			  .where()
			  .eq("email", email)
			  .findUnique();
			  
		if (user != null) {
			session().clear();
			return badRequest("User already exists, please use the retrieve password option instead");
		}

		PendingRegistration pending = PendingRegistration.find
			  .where()
			  .eq("email", email)
			  .findUnique();
			  
		if (pending != null) {
			session().clear();
			return badRequest("A pending registration is already in progress, please follow the link in the email sent to your email address. Note that spam filters may have moved this mail to a spam folder!");
		}

		// 2) generate random salt
		Logger.info("writing entry to pending registration table...");
		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[24];
		random.nextBytes(salt);
		String saltValue = toHex(salt);
		// 3) prepend and hash password
		String securedPassword = hashPassword(saltValue + password);
		// 4) create unique validation code
		SecureRandom randomVc = new SecureRandom();
		byte[] vCode = new byte[24];
		randomVc.nextBytes(vCode);
		String validationCode = Application.toHex(vCode);
		// 5) place submission in temporary queue
		PendingRegistration pendingReg = new PendingRegistration(email, securedPassword, organization, validationCode, saltValue);
		// 6) send confirmation email with validation link and redirect to 'confirmation sent' page
		SendEmail.createValidation(email, validationCode);
		
		return ok();
		// TODO: 8) setup check timestamps in temporary queue to remove "old" registration submissions (expire stale pendingRegistrations)
		// TODO: 9) guard against any vulnerabilities present in form submission?
	}
	
	//--------------------------------------------------------------------------
	public static Result validateRegistration(String validationCode) {
	
		final long HOUR = 3600*1000; // in milli-seconds.
		Date dateTime = new Date();
		
		long newDate = dateTime.getTime() - 1 * HOUR;
		Timestamp compTime = new Timestamp(newDate);
		
		PendingRegistration pr = PendingRegistration.find
			.where()
			.eq("validation_code", validationCode)
			.gt("create_time", compTime)
			.findUnique();
		if (pr != null) {
			ClientUser existingUser = ClientUser.find.where()
				.eq("email", pr.email)
				.findUnique();
			if (existingUser != null) {
				existingUser.password = pr.password;
				existingUser.passwordSalt = pr.password_salt;
				Ebean.save(existingUser);
			} 
			else {
				ClientUser user = new ClientUser(pr.email, pr.organization, pr.password, pr.password_salt);
				Ebean.save(user);
			}
			Ebean.delete(pr);
			return redirect(routes.Application.index());
		} 
		else {
			// TODO: proper return...
			return badRequest();
		}
	}

	//--------------------------------------------------------------------------
	// PASSWORD RESET
	//--------------------------------------------------------------------------
	public static Result requestReset() {
	
		JsonNode request = request().body().asJson();
		String email = safeGetString(request, "user");

		if (email != null) {
			ClientUser userForPWReset = ClientUser.find.where()
				.eq("email", email)
				.findUnique();
				
			if (userForPWReset != null) {
		        // 2) create unique validation code
		        SecureRandom randomVc = new SecureRandom();
		        byte[] vCode = new byte[24];
		        randomVc.nextBytes(vCode);
				String validationCode = Application.toHex(vCode);
				
				// 3) write vcode to db
	    		Logger.info("writing entry to pending registration table for a new password...");
	    		PendingRegistration pendingReg = new PendingRegistration(userForPWReset.email, userForPWReset.password, userForPWReset.organization, validationCode, userForPWReset.passwordSalt);
		    	
		        // 4) send confirmation email with validation link and redirect to 'confirmation sent' page
		        SendEmail.createPasswordReset(userForPWReset.email, validationCode);
		        return ok();
			}
		}
		
		return badRequest("Your request is not able to be processed at this time");
	}
	
	//--------------------------------------------------------------------------
	public static Result tryFinishReset() {
	
		JsonNode request = request().body().asJson();
		
		String email = safeGetString(request, "user");
		String password = safeGetString(request, "pwd");
		String vCode = safeGetString(request, "code");
	
		if (email == null || password == null || vCode == null) {
			return badRequest("Your request is not able to be processed at this time");
		}

		PendingRegistration registration = PendingRegistration.find.where()
	    			.eq("validation_code", vCode)
	    			.findUnique();
	    
	    if (registration != null) {
			String userEmail = registration.email;
			
			// Small extra anti-spoof verification
			if (email.equals(userEmail)) {
				ClientUser user = ClientUser.find.where()
						.eq("email", userEmail)
						.findUnique();
						
				if (user != null) {
					// 2) generate random salt
					SecureRandom random = new SecureRandom();
					byte[] salt = new byte[24];
					random.nextBytes(salt);
					String saltValue = toHex(salt);
					
					// 3) prepend and hash password
					String securedPassword = hashPassword(saltValue + password);
					
					// 5) place submission in db
					user.password = securedPassword;
					user.passwordSalt = saltValue; 
					Ebean.save(user);
					Ebean.delete(registration);
					session().clear();
			        SendEmail.createPasswordChangeConfirmation(userEmail);
					
					return ok("Reset complete, please trying logging in");
				}
			}
		}
		
		return badRequest("Your request is not able to be processed at this time");
	}
	
	//--------------------------------------------------------------------------
	public static Result logout() {
	
		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		
		sendBack.put("restrictedLayers", Layer_Base.getAccessRestrictedLayers());
		
		session().clear();
		return ok(sendBack);
	}

	//--------------------------------------------------------------------------
    // dbTable is expected to come from the server like this:
    //
    //	dbTable. 					-- entire packaged result from the server
    //		.definition[].			-- an array of bit flags that correspond to access switches
    //			.label 				-- name of the switch
    //		.data[].				-- an array of data elements, one for each registered user
    //			.email				-- email address of the given user
    //			.organization		-- optional organization name
    //			.admin				-- is admin?
    //				.flags[]		-- array of bit switches, size of this should match the size of the definition[] above
    //			
    //--------------------------------------------------------------------------
	public static Result getAccess() {
	
		ClientUser user = null;
		String userEmail = Context.current().session().get("email");
		if (userEmail != null) {
			user = ClientUser.find.where()
					.eq("email", userEmail)
					.findUnique();
		}

		if (user == null || user.admin == false) {
			return badRequest();
		}
		
		List<ClientUser> userList = ClientUser.find
			.findList();

		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
		
		ArrayNode definition = JsonNodeFactory.instance.arrayNode();
		// go through our enums and add the name of them to the definition...
		for (ClientUser.ACCESS e : ClientUser.ACCESS.values()) {
			definition.add(e.name());
		}
		
		ArrayNode data = JsonNodeFactory.instance.arrayNode();
		
		for(ClientUser cu : userList){
			ObjectNode aUser = JsonNodeFactory.instance.objectNode();
			aUser.put("email", cu.email);
			aUser.put("organization", cu.organization);
			aUser.put("admin", cu.admin);

			// Extract all the encoded bit switches associated with the enums
			ArrayNode flags = JsonNodeFactory.instance.arrayNode();
			for (ClientUser.ACCESS e : ClientUser.ACCESS.values()) {
				flags.add(((cu.accessFlags & e.value) > 0) ? true : false);
			}
			aUser.put("flags", flags);
			data.add(aUser);
		}

		sendBack.put("definition", definition);
		sendBack.put("data", data);
		
		return ok(sendBack);
	}

    //--------------------------------------------------------------------------
	public static Result changeAccess() {
	
		ClientUser user = null;
		String userEmail = Context.current().session().get("email");
		if (userEmail != null) {
			user = ClientUser.find.where()
					.eq("email", userEmail)
					.findUnique();
		}

		if (user == null || user.admin == false) {
			return badRequest();
		}
		
		JsonNode request = request().body().asJson();
		
		if (!request.isArray()) {
			return badRequest();
		}
		
		for (int i = 0; i < request.size(); i++) {
			JsonNode change = request.get(i);
			if (change.isObject()) {
				String email = safeGetString(change, "email");
				if (email != null) {
					user = ClientUser.find.where()
						.eq("email", userEmail)
						.findUnique();
					if (user != null) {
						boolean changed = false;
						int accessFlags = 0;
						
						Iterator<Map.Entry<String,JsonNode>> itr = change.fields();
						
						while (itr.hasNext()) {
							Map.Entry<String,JsonNode> entry = itr.next();
							String key = entry.getKey();
							JsonNode val = entry.getValue();
							
							if (key == null) continue;
							switch(key) {
								case "admin": {
									Boolean admin = safeGetBoolean(change, "admin");
									if (admin != null && admin != user.admin) {
										changed = true;
										user.admin = admin;
									}
								}
								break;
								
								case "email":
								case "organization":
									// don't allow change, so skip these...
									break;
									
								default: {
									// we assume we are one of the enums, so work those next...
									if (val.isBoolean()) {
										ClientUser.ACCESS e = ClientUser.ACCESS.getEnumForString(key);
										if (e != null && val.booleanValue() == true) {
											accessFlags |= e.value;
										}
									}
								}
							}
						}
						
						if (accessFlags != user.accessFlags) {
							changed = true;
							user.accessFlags = accessFlags;
							Logger.debug("Access flags have changed!");
						}
						if (changed) {
							Logger.debug("Saving one user change for: " + email);
							Ebean.save(user);
						}
					}
				}
			}
		}
		
		return ok();
	}
	
}


