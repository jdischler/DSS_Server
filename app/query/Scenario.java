package util;

import play.*;
import java.util.*;
import java.io.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Scenario 
{
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = true;
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
	
	//--------------------------------------------------------------------------
	public Scenario() {
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
		RandomString uniqueID = new RandomString();
		int tryCount = 0;
		while(tryCount < 1000) {
			String scenarioCacheID = uniqueID.get(5) + 
						clientID + 
						((tryCount > 0) ? Integer.toString(tryCount) : "");
			if (!mCachedScenarios.containsKey(scenarioCacheID)) {
				mCachedScenarios.put(scenarioCacheID, theScenario);
				theScenario.mCachedAtTime = System.currentTimeMillis();
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
					currentSelection = query.execute(transformElement);
				} catch (Exception e) {
					Logger.info(e.toString());
				}
				
				int pixelsSelectedFromQuery = currentSelection.countSelectedPixels();
				float perc = 1.0f;
//				Logger.info("  Num pixels selected from query: " +
//						Integer.toString(currentSelection.countSelectedPixels()));
				
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
}

