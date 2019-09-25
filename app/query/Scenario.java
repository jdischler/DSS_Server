package util;

import play.*;
import java.util.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import util.Layer_Integer;

//------------------------------------------------------------------------------
public class Scenario 
{
	//--------------------------------------------------------------------------
	// scenario tracking
	// Add row defaults
	private static List<String> mPosition;
	private static List<String> mData;
	private static Long mScenarioCounter = 1L;
	
	public static final void initTracking() {
		
		mPosition = new ArrayList<String>();
		mData = new ArrayList<String>();
		
		mPosition.add("user"); 			mData.add("");
		mPosition.add("scenario"); 		mData.add("");
		mPosition.add("timestamp"); 	mData.add("");
		mPosition.add("step"); 			mData.add("");
		mPosition.add("cdl_2012"); 		mData.add("");
		mPosition.add("lcc"); 			mData.add("");
		mPosition.add("lcs"); 			mData.add("");
		mPosition.add("slope >"); 		mData.add("");
		mPosition.add("slope <"); 		mData.add("");
		mPosition.add("rivers >"); 		mData.add("");
		mPosition.add("rivers <"); 		mData.add("");
		mPosition.add("dairy >"); 		mData.add("");
		mPosition.add("dairy <"); 		mData.add("");
		mPosition.add("public_land >");	mData.add("");
		mPosition.add("public_land <"); mData.add("");
		mPosition.add("watersheds"); 	mData.add("");
		mPosition.add("subset"); 		mData.add("");
		mPosition.add("area_query");	mData.add("");
		mPosition.add("area_trx");		mData.add("");
		mPosition.add("to"); 			mData.add("");
		mPosition.add("fertilized"); 	mData.add("");
		mPosition.add("manure"); 		mData.add("");
		mPosition.add("fall_manure"); 	mData.add("");
		mPosition.add("tilled"); 		mData.add("");
		mPosition.add("cover_crop"); 	mData.add("");
		mPosition.add("countoured"); 	mData.add("");
	}
	
	private static final void set(List<String> row, String key, String value) {
		
		Integer ct = mPosition.size();
		Integer sz = row.size();
		for (int idx = 0; idx < ct; idx++) {
			if (idx >= sz) {
				Logger.error("OOOOOOOOoooof");
				break;
			}
			
			if (key.equalsIgnoreCase(mPosition.get(idx))) {
				row.set(idx, value);
			}
		}
	}

	// messy but makes the tracking add-on a little faster to slip in
	public String mUserId; // 
	public String mScenarioId;
	
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}
	
	// Scenarios can be cached for sharing amongst other threads
	private static Map<String, Scenario>	mCachedScenarios;
	private long mCachedAtTime;
	
	public int mRefCounts;
	
	public GlobalAssumptions mAssumptions;
	public Selection mSelection;
	public String mOutputDir;
	private JsonNode mConfiguration;
	public int[][] mNewRotation; // copy of Rotation layer, but selection transformed
	public ClientUser mOptionalUser = null; // yup, can be null
	
	//--------------------------------------------------------------------------
	public Scenario(ClientUser user) {
		mOptionalUser = user; // ca be null
	}

	//--------------------------------------------------------------------------
	public final int getWidth() {
		if (mSelection != null) {
			return mSelection.getWidth();
		}
		return 0;
	}

	//--------------------------------------------------------------------------
	public final int getHeight() {
		if (mSelection != null) {
			return mSelection.getHeight();
		}
		return 0;
	}
	
	//--------------------------------------------------------------------------
	public void setAssumptions(JsonNode clientAssumptions) {
		
		mAssumptions = new GlobalAssumptions();
		try {
			mAssumptions.setAssumptionsFromClient(clientAssumptions);
		} 
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}
	
	// Returns a cacheStringID, which should be saved and returned to free the scenario...
	//--------------------------------------------------------------------------
	public static final String cacheScenario(Scenario theScenario, String clientID, int requestCount) {
		
		if (mCachedScenarios == null) {
			mCachedScenarios = new HashMap<String, Scenario>();
		}
		
		theScenario.mRefCounts = requestCount;
		int tryCount = 0;
		while(tryCount < 1000) {
			String scenarioCacheID = mScenarioCounter.toString();
			mScenarioCounter++;			
			theScenario.mScenarioId = scenarioCacheID;
			scenarioCacheID = clientID + scenarioCacheID;
			if (!mCachedScenarios.containsKey(scenarioCacheID)) {
				mCachedScenarios.put(scenarioCacheID, theScenario);
				theScenario.mCachedAtTime = System.currentTimeMillis();
				theScenario.mUserId = clientID;
				return scenarioCacheID;
			}
			tryCount++;
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	public static final void checkPurgeStaleScenarios() {
		
		if (mCachedScenarios == null) {
			return;
		}
		// giving 5 minutes 		
		long expireHours = 0 * 10 * 60 * 1000; // 0 hour -> minutes -> seconds -> milliseconds
		long roughlyNow = System.currentTimeMillis();
		for (Map.Entry<String, Scenario> entry : mCachedScenarios.entrySet()) {
			Logger.warn("Have possibly stale scenario objects hanging around...");
			Scenario value = entry.getValue();
			if (roughlyNow - value.mCachedAtTime > expireHours) {
				Logger.error("Error - removing potentially stale scenario. " +
					"Anything caching a scenario should be remove cached scenario when " +
					"done using that scenario!");
				String key = entry.getKey();
				releaseCachedScenario(key);
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public static final Scenario getCachedScenario(String cacheStringID) {
		
		if (mCachedScenarios == null) {
			Logger.warn("Attempting to fetch a scenario but the cache has not been initialized!");
			return null;
		}
		Scenario res = mCachedScenarios.get(cacheStringID);
		if (res == null) {
			Logger.warn("Attempting to fetch scenario named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return null;
		}
		
		return res;
	}
	
	//--------------------------------------------------------------------------
	public static final void releaseCachedScenario(String cacheStringID) {
		
		if (mCachedScenarios == null) {
			Logger.warn("Attempting to uncache a scenario but the cache has not been initialized!");
			return;
		}
		
		Scenario res = mCachedScenarios.get(cacheStringID);
		if (res == null) {
			Logger.warn("Attempting to uncache scenario named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return;
		}
		
		res.mRefCounts--;
		if (res.mRefCounts > 0) {
			return;
		}
		detailedLog(" - releasing cache for scenario, cache string named <" + cacheStringID + ">");
		Scenario scen = mCachedScenarios.remove(cacheStringID);
/*		scen.mAssumptions = null;
		scen.mSelection = null; 
		scen.mNewRotation = null;
*/		
	}
	
	
	//--------------------------------------------------------------------------
/*	public JsonNode run() {
		
		mNewRotation = duplicateRotation();
		transformRotation(mNewRotation);
		
		Models model = new Models();
		JsonNode SendBack = model.modeloutcome(null, mSelection, mOutputDir, mNewRotation);
		return SendBack;
	}*/

	//--------------------------------------------------------------------------
	public int[][] getTransformedRotation(JsonNode configuration) {
		
		mConfiguration = configuration;
		
		detailedLog("Beginning transform rotation...");
		detailedLog("...Current rotation duplicating...");
		mNewRotation = duplicateRotation();
		detailedLog("...Duplicated rotation transforming...");
		mNewRotation = transformRotation(mNewRotation);
		detailedLog("...Transform complete!!");
		
		return mNewRotation;
	}
	
	//--------------------------------------------------------------------------
	private int[][] duplicateRotation() {
	
		// uses clone to duplicate the data array
		Layer_Base original = Layer_Base.getLayer("cdl_2012");//.getIntData().clone();
		int [][] originalData = original.getIntData();
		
		int width = original.getWidth();
		int height = original.getHeight();
		
		mNewRotation = new int[height][];
		for (int y = 0; y < height; y++) {
			mNewRotation[y] = originalData[y].clone(); 
		}
		return mNewRotation;
	}

	//--------------------------------------------------------------------------
	private int[][] transformRotation(int[][] rotationToTransform) {
	
		Query query = new Query();
		
		JsonNode transformQueries = mConfiguration.get("transforms");
		if (transformQueries != null && transformQueries.isArray()) {
			
			detailedLog("...Has Transforms array...");
			Selection currentSelection = null, oldSelection = null;
			ArrayNode transformArray = (ArrayNode)transformQueries;
			int count = transformArray.size();
			
			for (int i = 0; i < count; i++) {
				detailedLog("...Processing one array element in the transform list...");
				JsonNode transformElement = transformArray.get(i);
				
				if (transformElement == null) {
					Logger.warn("Boooo....transform element was null.");
					continue; // TODO: signal back to client that an error happened vs. just doing nothing
				}
				else if (!transformElement.isObject()) {
					Logger.warn("Booooooo.....transform element is not an object");
					continue; // TODO: signal back to client that an error happened vs. just doing nothing
				}
				
				// get the new landuse...but remember that it needs to be in the
				//	format of a bit mask "position" that corresponds to the index
				//	.vs the index value itself.
				JsonNode transformConfig = transformElement.get("config");
				if (transformConfig == null || !transformConfig.isObject()) {
					Logger.warn("Boooo....transform config does not exist or is not an object");
					continue; // TODO: signal back to client that an error happened vs. just doing nothing
				}
				
				ObjectNode transformConfigObj = (ObjectNode)transformConfig;
				
				int newLandUse = transformConfigObj.get("LandUse").intValue();
				detailedLog("  + New land use code: " + Integer.toString(newLandUse));
				newLandUse = Layer_Integer.convertIndexToMask(newLandUse);
				
				JsonNode managementOptions = transformConfigObj.get("Options");
				if (managementOptions == null || !managementOptions.isObject()) {
					detailedLog("  +- No management options came along, currently not an error");
				}
				else {
					detailedLog("  +-- Management Options from Client: " + managementOptions.toString());
					// Blah, dig in and apply options.
					// ---- Fertilizer ----
					JsonNode fertNode = managementOptions.get("Fertilizer");
					if (fertNode != null && fertNode.isObject()) { 
						ObjectNode fertilizerOptions = (ObjectNode)fertNode;
						if (fertilizerOptions.get("Fertilizer").booleanValue()) {
							detailedLog("  +--- Applying Fertilizer");
							newLandUse = ManagementOptions.E_Fertilizer.setOn(newLandUse); // else no fertilizer
							if (fertilizerOptions.get("FertilizerManure").booleanValue()) {
								detailedLog("  +--- Fertilizer Is Manure");
								newLandUse = ManagementOptions.E_Manure.setOn(newLandUse); // else is synthetic
								if (fertilizerOptions.get("FertilizerFallSpread").booleanValue()) {
									newLandUse = ManagementOptions.E_FallManure.setOn(newLandUse); // else is spread other time
									detailedLog("  +--- Fertilizer Is Fall Spread Manure");
								}
							}
						}
						else {
							detailedLog("  +--- No Fertilizer");
						}
					}
					// ---- Tillage ----
					JsonNode tillNode = managementOptions.get("Tillage");
					if (tillNode != null && tillNode.isObject()) {
						ObjectNode tillageOptions = (ObjectNode)tillNode;
						if (tillageOptions.get("Tillage").booleanValue()) {
							newLandUse = ManagementOptions.E_Till.setOn(newLandUse); // else is no-till
							detailedLog("  +--- Applying Tillage");
						}
						else {
							detailedLog("  +---- Using no-till");
						}
					}
					// ---- Cover Crop ----
					JsonNode ccnode = managementOptions.get("CoverCrop");
					if (ccnode != null && ccnode.isObject()) {
						ObjectNode ccOptions = (ObjectNode)ccnode;
						if (ccOptions.get("CoverCrop").booleanValue()) {
							newLandUse = ManagementOptions.E_CoverCrop.setOn(newLandUse); // else is no-covercrop
							detailedLog("  +--- Applying CoverCrop");
						}
						else {
							detailedLog("  +---- Using no covercrop");
						}
					}
					// ---- Contour ----
					JsonNode cnode = managementOptions.get("Contour");
					if (cnode != null && cnode.isObject()) {
						ObjectNode cOptions = (ObjectNode)cnode;
						if (cOptions.get("Contour").booleanValue()) {
							newLandUse = ManagementOptions.E_Contour.setOn(newLandUse); // else is no-contour
							detailedLog("  +--- Applying Contouring");
						}
						else {
							detailedLog("  +---- Using no contour");
						}
					}
					// ---- Terraced ----
					JsonNode tnode = managementOptions.get("Terraced");
					if (tnode != null && tnode.isObject()) {
						ObjectNode tOptions = (ObjectNode)tnode;
						if (tOptions.get("Terraced").booleanValue()) {
							newLandUse = ManagementOptions.E_Terrace.setOn(newLandUse); // else is no-terraces
							detailedLog("  +--- Applying Terracing");
						}
						else {
							detailedLog("  +---- Using no terraces");
						}
					}
				}
				
				try {
					currentSelection = query.execute(transformElement, mOptionalUser);
				} catch (Exception e) {
					Logger.info(e.toString());
				}
				
				int pixelsSelectedFromQuery = currentSelection.countSelectedPixels();
				float perc = 1.0f;
//				Logger.info("  Num pixels selected from query: " +
//						Integer.toString(currentSelection.countSelectedPixels()));
				
				((ObjectNode)transformElement).put("area_query", pixelsSelectedFromQuery * 30.0f / 4046.856f);
				if (oldSelection != null) {
					// remove the old selection from the current/new selection
					//	this prevents us from running a transform on land that is
					//	already transformed....
					currentSelection.removeSelection(oldSelection);
					int actualPixelsSelectedFromQuery = currentSelection.countSelectedPixels();
//					Logger.info("  Num pixels selected after removing old selection: " +
//						Integer.toString(currentSelection.countSelectedPixels()));
					perc = actualPixelsSelectedFromQuery / (float)pixelsSelectedFromQuery;
					detailedLog("  Pixels removed from selection: " +
						Integer.toString(pixelsSelectedFromQuery - actualPixelsSelectedFromQuery));
					
					// export area in acres
					((ObjectNode)transformElement).put("area_trx", actualPixelsSelectedFromQuery * 30.0f / 4046.856f);
				}
				else {
					// export area in acres
					((ObjectNode)transformElement).put("area_trx", pixelsSelectedFromQuery * 30.0f / 4046.856f);
				}
				
				detailedLog("  Actual selection percentage: " + 
					Float.toString(perc * 100));
				
				// Run the transform on a (possibly) reduced selection
				//	e.g., if this is the second or later query in a series,
				//	the first (highest priority) transform will trump any subsequent transforms
				int x, y;
								
				for (y = 0; y < currentSelection.mHeight; y++) {
					for (x = 0; x < currentSelection.mWidth; x++) {
						if (currentSelection.isSelected(x, y)) {
							rotationToTransform[y][x] = newLandUse;			
						}
					}
				}
				
				if (oldSelection != null) {
					// Now grow the selection up to be the sum of both selections
					//	...thereby potentially growing the selection up to include
					//	more pixels...which will then be candidates for being excluded
					//	from subsequent transform passes...
					currentSelection.combineSelection(oldSelection);
//					Logger.info("  Num pixels selected after combining new and old selection: " +
//						Integer.toString(currentSelection.countSelectedPixels()));
				}
				
				oldSelection = currentSelection;
			}
			
			mSelection = currentSelection;
		}
		
		return rotationToTransform;
	}

	//--------------------------------------------------------------------------
	public static String logCSVHeader() {
		return String.join(",", mPosition) + "\n";
	}
	//--------------------------------------------------------------------------
	public String logCSV() {
			
		JsonNode transformQueries = mConfiguration.get("transforms");
		if (transformQueries == null || !transformQueries.isArray()) {
			return "bad config";
		}
		
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012");
		if (cdl == null) return "bad config";
		
		Selection currentSelection = null, oldSelection = null;
		ArrayNode transformArray = (ArrayNode)transformQueries;
		StringBuilder str = new StringBuilder();
		
		DateTimeFormatter FOMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm");
		LocalDateTime localDateTime = LocalDateTime.now();
		String ts = FOMATTER.format(localDateTime);
		
		int count = transformArray.size();
		for (Integer i = 0; i < count; i++) {
			// copy the default (no data) set
			List<String> row = new ArrayList<String>(mData);
			set(row, "user", mUserId);
			set(row, "scenario", mScenarioId);
			set(row, "timestamp", ts);
			set(row, "step", i.toString());
			
			JsonNode transformElement = transformArray.get(i);
			if (transformElement == null || !transformElement.isObject()) {
				continue;
			}
			
			JsonNode query = transformElement.get("queryLayers");			
			//str.append("Query:" + query.toString() + "\n");
			
			JsonNode transformConfig = transformElement.get("config");
			if (transformConfig == null || !transformConfig.isObject()) {
				continue;
			}
			
			ObjectNode transformConfigObj = (ObjectNode)transformConfig;
			
			try {
				Integer newLandUse = Json.safeGetInteger(transformConfig, "LandUse");
				String tmp = "";
				if (newLandUse == 1) tmp = "corn";
				else if (newLandUse == 6) tmp = "grass";
				else if (newLandUse == 16) tmp = "soy";
				else if (newLandUse == 17) tmp = "alfalfa";
				
				set(row, "to", tmp);
				set(row, "area_query", Json.safeGetOptionalFloat(transformElement, "area_query", -1.0f).toString());
				set(row, "area_trx", Json.safeGetOptionalFloat(transformElement, "area_trx", -1.0f).toString());
				
				if (query != null && query.isArray()) {
					
					ArrayNode arNode = (ArrayNode)query;
					int ct = arNode.size();
					for (int ii = 0; ii < ct; ii++) {
						JsonNode arElem = arNode.get(ii);

						String type = Json.safeGetString(arElem, "type");
						String subType = Json.safeGetOptionalString(arElem, "subType", null);
						String name = Json.safeGetString(arElem, "name"); // layer name
						
						if (subType != null && subType.equalsIgnoreCase("vectorSelect")) {
							set(row, "watersheds", arElem.get("selected").asText());
						}
						else if (type.equalsIgnoreCase("fractionalLand")) {
							set(row, "subset", arElem.get("fraction").asText());
						}
						else if (type.equalsIgnoreCase("continuous")) {
							Float lessThan = Json.safeGetOptionalFloat(arElem, "lessThanValue", null);
							Float gtrThan = Json.safeGetOptionalFloat(arElem, "greaterThanValue", null);
							if (lessThan != null) {
								set(row, name + " <", lessThan.toString());
							}
							if (gtrThan != null) {
								set(row, name + " >", gtrThan.toString());
							}
						}
						else if (type.equalsIgnoreCase("indexed")) {
							ArrayNode matchVals = (ArrayNode)arElem.get("matchValues");
							if (matchVals == null) continue;
							String strVal = "";
							for (int idx = 0; idx < matchVals.size(); idx++) {
								if (idx > 0) strVal += ":";
								Integer index = matchVals.get(idx).intValue();
								if (name.equalsIgnoreCase("cdl_2012")) {
									if (index == 1) strVal += "corn";
									else if (index == 6) strVal += "grass";
									else if (index == 16) strVal += "soy";
									else if (index == 17) strVal += "alfalfa";
								}
								else if (name.equalsIgnoreCase("lcc")) {
									strVal += index;
									/*if (index == 1) strVal += "crop_I";
									else if (index == 2) strVal += "crop_II";
									else if (index == 3) strVal += "crop_III";
									else if (index == 4) strVal += "crop_IV";
									else if (index == 5) strVal += "marg_I";
									else if (index == 6) strVal += "marg_II";
									else if (index == 7) strVal += "marg_III";
									else if (index == 8) strVal += "marg_IV";*/
								}
								else if (name.equalsIgnoreCase("lcs")) {
									if (index == 1) strVal += "erosion_prone";
									else if (index == 2) strVal += "saturated_soils";
									else if (index == 3) strVal += "poor_texture";
								}
							}
							set(row, name, strVal);
						}
					}
				}
				
				JsonNode managementOptions = transformConfigObj.get("Options");
				if (managementOptions != null && managementOptions.isObject()) {
					JsonNode fertNode = managementOptions.get("Fertilizer");
					if (fertNode != null && fertNode.isObject()) { 
						ObjectNode fertOpts = (ObjectNode)fertNode;
						set(row, "fertilized", Json.safeGetOptionalBoolean(fertOpts, "Fertilizer", false).toString());
						set(row, "manure", Json.safeGetOptionalBoolean(fertOpts, "FertilizerManure", false).toString());
						set(row, "fall_manure", Json.safeGetOptionalBoolean(fertOpts, "FertilizerFallSpread", false).toString());
					}
					JsonNode tillNode = managementOptions.get("Tillage");
					if (tillNode != null && tillNode.isObject()) {
						ObjectNode tillOpts = (ObjectNode)tillNode;
						set(row, "tilled", Json.safeGetOptionalBoolean(tillOpts, "Tillage", false).toString());;
					}
					JsonNode ccnode = managementOptions.get("CoverCrop");
					if (ccnode != null && ccnode.isObject()) {
						ObjectNode ccOpts = (ObjectNode)ccnode;
						set(row, "cover_crop", Json.safeGetOptionalBoolean(ccOpts, "CoverCrop", false).toString());;
					}
					JsonNode cnode = managementOptions.get("Contour");
					if (cnode != null && cnode.isObject()) {
						ObjectNode contOpts = (ObjectNode)cnode;
						set(row, "countoured", Json.safeGetOptionalBoolean(contOpts, "Contour", false).toString());;
					}
				}
			}
			catch(Exception e) {
				Logger.error(e.toString());
			}
			
			str.append(String.join(",", row) + "\n");
		}
		
		return str.toString();
	}
}
