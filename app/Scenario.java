package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Scenario 
{
	// Scenarios can be cached for sharing amongst other threads
	private static Map<String, Scenario>	mCachedScenarios;
	private long mCachedAtTime;
	
	public GlobalAssumptions mAssumptions;
	public String mClientID;
	public Selection mSelection; 
	public String mOutputDir;
	private JsonNode mConfiguration;
	public int[][] mNewRotation; // copy of Rotation layer, but selection transformed
	
	//--------------------------------------------------------------------------
	public Scenario(JsonNode configuration, String outputDir) {
		
		mConfiguration = configuration;
		mOutputDir = outputDir;
	}

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
	public static final String cacheScenario(Scenario theScenario, int clientID) {
		
		if (mCachedScenarios == null) {
			mCachedScenarios = new HashMap<String, Scenario>();
		}
		
		RandomString uniqueID = new RandomString();
		int tryCount = 0;
		while(tryCount < 1000) {
			String scenarioCacheID = uniqueID.get(5) + 
						Integer.toString(clientID) + 
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
	private static final void checkPurgeStaleScenarios() {
		
		if (mCachedScenarios == null) {
			return;
		}

		// giving 5 minutes 		
		long expireHours = 0 * 5 * 60 * 1000; // 0 hour -> minutes -> seconds -> milliseconds
		long roughlyNow = System.currentTimeMillis();
		for (Map.Entry<String, Scenario> entry : mCachedScenarios.entrySet()) {
			Scenario value = entry.getValue();
			if (roughlyNow - value.mCachedAtTime > expireHours) {
				Logger.info("Warning - removing potentially stale scenario. " +
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
			Logger.info("Attempting to fetch a scenario but the cache has not been initialized!");
			return null;
		}
		Scenario res = mCachedScenarios.get(cacheStringID);
		if (res == null) {
			Logger.info("Attempting to fetch scenario named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return null;
		}
		
		return res;
	}
	
	//--------------------------------------------------------------------------
	public static final void releaseCachedScenario(String cacheStringID) {
		
		if (mCachedScenarios == null) {
			Logger.info("Attempting to uncache a scenario but the cache has not been initialized!");
			return;
		}
		
		Scenario res = mCachedScenarios.get(cacheStringID);
		if (res == null) {
			Logger.info("Attempting to uncache acenario named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return;
		}
		Logger.info(" - releasing cache for scenario, cache string named <" + cacheStringID + ">");
		mCachedScenarios.put(cacheStringID, null);
		mCachedScenarios.remove(cacheStringID);
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
		
		Logger.info("Beginning transform rotation...");
		mNewRotation = duplicateRotation();
		mNewRotation = transformRotation(mNewRotation);
		Logger.info("...Transform complete!!");
		
		return mNewRotation;
	}
	
	//--------------------------------------------------------------------------
	private int[][] duplicateRotation() {
	
		Logger.info("Current rotation duplicating...");
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
		
		Logger.info("Duplicated rotation transforming...");
		JsonNode transformQueries = mConfiguration.get("transforms");
		if (transformQueries != null && transformQueries.isArray()) {
			
			Logger.info("Has Transforms array...");
			Selection currentSelection = null, oldSelection = null;
			ArrayNode transformArray = (ArrayNode)transformQueries;
			int count = transformArray.size();
			
			for (int i = 0; i < count; i++) {
				Logger.info("-Processing one array element in the transform list...");
				JsonNode transformElement = transformArray.get(i);
				
				if (transformElement == null) {
					Logger.info("Boooo....transform element was null.");
				}
				else if (!transformElement.isObject()) {
					Logger.info("Booooooo.....transform element is not an object");
				}
				
				// get the new landuse...but remember that it needs to be in the
				//	format of a bit mask "position" that corresponds to the index
				//	.vs the index value itself.
				int newLanduse = transformElement.get("newLandUse").getValueAsInt();
				Logger.info("+New land use code: " + Integer.toString(newLanduse));
				newLanduse = Layer_Integer.convertIndexToMask(newLanduse);
				
				try {
					currentSelection = query.execute(transformElement);
				} catch (Exception e) {
					Logger.info(e.toString());
				}
				
				Logger.info("  Num pixels selected from query: " +
						Integer.toString(currentSelection.countSelectedPixels()));
				
				if (oldSelection != null) {
					// remove the old selection from the current/new selection
					//	this prevents us from running a transform on land that is
					//	already transformed....
					currentSelection.removeSelection(oldSelection);
					Logger.info("  Num pixels selected after removing old selection: " +
						Integer.toString(currentSelection.countSelectedPixels()));
				}
				
				// Run the transform on a (possibly) reduced selection
				//	e.g., if this is the second or later query in a series,
				//	the first (highest priority) transform will trump any subsequent transforms
				int x, y;
				for (y = 0; y < currentSelection.mHeight; y++) {
					for (x = 0; x < currentSelection.mWidth; x++) {
						if (currentSelection.isSelected(x, y)) {
							rotationToTransform[y][x] = newLanduse;			
						}
					}
				}
				
				if (oldSelection != null) {
					// Now grow the selection up to be the sum of both selections
					//	...thereby potentially growing the selection up to include
					//	more pixels...which will then be candidates for being excluded
					//	from subsequent transform passes...
					currentSelection.combineSelection(oldSelection);
					Logger.info("  Num pixels selected after combining new and old selection: " +
						Integer.toString(currentSelection.countSelectedPixels()));
				}
				
				oldSelection = currentSelection;
			}
			
			mSelection = currentSelection;
		}
		
		return rotationToTransform;
	}
}

