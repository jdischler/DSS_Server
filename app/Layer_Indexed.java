package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Layer_Indexed extends Layer_Base
{
	// Internal helper class to store color key information...
	//--------------------------------------------------------------------------
	protected class Layer_Key {
		
		private int mIndex;
		private String mLabel, mHexColor;
		
		public Layer_Key(int index, String label, String hexColor) {
			mIndex = index;
			mLabel = label;
			mHexColor = hexColor;
		}

		public JsonNode getAsJson() {
			ObjectNode ret = JsonNodeFactory.instance.objectNode();
			
			ret.put("index", mIndex);
			ret.put("label", mLabel);
			ret.put("color", mHexColor);
			
			return ret;
		}
	}
	
	private ArrayList<Layer_Key> mLayerKey;
	
	//--------------------------------------------------------------------------
	public Layer_Indexed(String name) {
		super(name);
		
		mLayerKey = new ArrayList<Layer_Key>();
	}
	
	//--------------------------------------------------------------------------
	protected void processASC_Line(int y, String lineElementsArray[]) {
		
		boolean erred = false;
		for (int x = 0; x < lineElementsArray.length; x++) {
			int val = Integer.parseInt(lineElementsArray[x]);
			if (val <= 0) { // mNoDataValue?
				val = 0;
			}
			else {
				// convert to a bit style value for fast/simultaneous compares
				if (val <= 32) {
					val = convertIndexToMask(val);
				}
				else if (!erred) {
					erred = true;
					Logger.error("  BAD value - indexed values can only be 1-32. Was: " 
						+ Integer.toString(val));
				}
			}
			mIntData[y][x] = val;
		}
	}

	// Loads a color key if there is one....
	//--------------------------------------------------------------------------
	protected void onLoadEnd() {
		
		Logger.info("  Attempting to read color and name key file.");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader("./layerData/" + mName + ".key"));

			// now read the array data
			while (br.ready()) {
				String line = br.readLine();
				String split[] = line.split(",");
			
				if (split.length != 3) {
					Logger.info("  Parse error reading /layerData/" + mName + ".key");
					Logger.info("  Error: <read>" + line);
					throw new Exception();
				}
				else {
					int index = Integer.parseInt(split[0].trim());
					String label = split[1].trim();
					String color = split[2].trim();
							
					Layer_Key keyItem = new Layer_Key(index, label, color);
					mLayerKey.add(keyItem);
				}
			}
		}
		catch (Exception e) {
			Logger.info("  " + e.toString());
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (Exception e) {
					Logger.info("  " + e.toString());
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------
	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {

		// Set this as a default - call super first so subclass can override a return result
		//	for the same parameter request type. Unsure we need that functionality but...
		JsonNode ret = super.getParameterInternal(clientRequest);

		String type = clientRequest.get("type").getTextValue();
		if (type.equals("colorKey")) {
			ret = getColorKeyAsJson();
		}
		
		return ret;
	}
	
	//--------------------------------------------------------------------------
	public JsonNode getColorKeyAsJson() {
		
		ArrayNode ret = JsonNodeFactory.instance.arrayNode();
		
		for (int i = 0; i < mLayerKey.size(); i++) {
			ret.add(mLayerKey.get(i).getAsJson());
		}
		
		return ret;
	}

	// Takes an index on an indexed raster and converts it to the appropriate
	//	bit position. Index must be 1 based and not more than 32 (ie, 1-32)
	//--------------------------------------------------------------------------
	public static int convertIndexToMask(int index) {
		
		if (index <= 0 || index > 32) {
			Logger.info("Bad index in convertIndexToMask: " + Integer.toString(index));
			return 1;
		}
		
		return (1 << (index-1));
	}
	
	//--------------------------------------------------------------------------
	private int getCompareBitMask(JsonNode matchValuesArray) {
		
		int queryMask = 0;
		
		Logger.info("trying to get Compare Bit Mask");
		
		if (matchValuesArray.isArray()) {
			Logger.info("Ok, matchValuesArray IS an array");
		}
		
		ArrayNode arNode = (ArrayNode)matchValuesArray;
		if (arNode != null) {
			int count = arNode.size();
			Logger.info(Integer.toString(count));

			for (int i = 0; i < count; i++) {
				JsonNode node = arNode.get(i);
				
				int val = node.getValueAsInt(1); // FIXME: default value?
				Logger.info(Integer.toString(val));
				queryMask |= convertIndexToMask(val);
			}
			
			Logger.info(Integer.toString(queryMask));
			return queryMask;
		}
		
		return 1;
	}
	
	//--------------------------------------------------------------------------
	protected Selection query(JsonNode queryNode, Selection selection) {

		Logger.info("Running indexed query");
		JsonNode queryValues = queryNode.get("matchValues");
		int test_mask = getCompareBitMask(queryValues);

		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				selection.mSelection[y][x] &= ((mIntData[y][x] & test_mask) > 0 ? 1 : 0);
			}
		}
		return selection;
	}
}

