package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Scenario 
{
	private Selection mSelection; 
	private String mOutputDir;
	private JsonNode mConfiguration;
	private int[][] mNewRotation; // copy of Rotation layer, but selection transformed
	
	//--------------------------------------------------------------------------
	public Scenario(JsonNode configuration, String outputDir) {
		
		mConfiguration = configuration;
		mOutputDir = outputDir;
	}
	
	//--------------------------------------------------------------------------
	public JsonNode run() {
		
		mNewRotation = duplicateRotation();
		transformRotation(mNewRotation);
		
		Models model = new Models();
		JsonNode SendBack = model.modeloutcome(null, mSelection, mOutputDir, mNewRotation);
		return SendBack;
	}
	
	//--------------------------------------------------------------------------
	private int[][] duplicateRotation() {
	
		// uses clone to duplicate the data array
		return Layer_Base.getLayer("Rotation").getIntData().clone();
	}

	//--------------------------------------------------------------------------
	private void transformRotation(int[][] rotationToTransform) {
	
		Query query = new Query();
		
		JsonNode transformQueries = mConfiguration.get("transforms");
		if (transformQueries != null && transformQueries.isArray()) {
	
			Selection currentSelection = null, oldSelection = null;
			ArrayNode transformArray = (ArrayNode)transformQueries;
			int count = transformArray.size();
			
			for (int i = 0; i < count; i++) {
				Logger.info("Processing one array element in the transform list");
				JsonNode transformElement = transformArray.get(i);
				
				// get the new landuse...but remember that it needs to be in the
				//	format of a bit mask "position" that corresponds to the index
				//	.vs the index value itself.
				int newLanduse = transformElement.get("newLanduse").getValueAsInt();
				newLanduse = Layer_Indexed.convertIndexToMask(newLanduse);
				
				try {
					currentSelection = query.execute(transformElement);
				} catch (Exception e) {
					Logger.info(e.toString());
				}
				
				if (oldSelection != null) {
					// remove the old selection from the current/new selection
					//	this prevents us from running a transform on land that is
					//	already transformed....
					currentSelection.removeSelection(oldSelection);
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
				}
				
				oldSelection = currentSelection;
			}
		}
	}
}

