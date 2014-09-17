package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;

//import org.codehaus.jackson.*;
//import org.codehaus.jackson.node.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Layer_Integer extends Layer_Base
{
	// Count at which we switch to using a hashset vs. a standard array list...
	private static final int RAW_BREAK_EVEN_COUNT = 10;
	
	public enum EType {
		EPreShiftedIndex,		// data is shifted at load time
		EQueryShiftedIndex,		// data is shifted for query testing only
		ERaw					// data is loaded raw and unmodified
	}	
	
	// Internal helper class to store color key information...
	//--------------------------------------------------------------------------
	protected class Layer_Key {
		
		public int mIndex;
		public String mLabel, mHexColor;
		
		// NOTE: if no color, string will come in as NULL
		public Layer_Key(int index, String label, String hexColor) {
			mIndex = index;
			mLabel = label;
			mHexColor = hexColor;
		}

		// NOTE: returns NULL if color string is NULL
		public JsonNode getAsJson() {
			
			if (mHexColor == null) {
				return null;
			}
			
			ObjectNode ret = JsonNodeFactory.instance.objectNode();
			
			ret.put("index", mIndex);
			ret.put("label", mLabel);
			ret.put("color", mHexColor);
			
			return ret;
		}
	}
	
	private ArrayList<Layer_Key> mLayerKey;
	protected int[][] mIntData;
	protected int mNoDataValue;
	protected int mConvertedNoDataValue;
	protected EType mLayerDataFormat;
	protected boolean mbInitedMinMaxCache;
	protected int mMin, mMax;
	
	// Pass true to have the data shifted for mask type comparisons.
	//--------------------------------------------------------------------------
	public Layer_Integer(String name, EType layerType) {
		
		super(name);
		
		mLayerKey = new ArrayList<Layer_Key>();
		mNoDataValue = -9999; // TODO: load from file...
		// if raw, don't convert the no-data value...otherwise it isn't really raw anymore...
		if (layerType == EType.ERaw) {
			mConvertedNoDataValue = mNoDataValue;
		}
		else {
			mConvertedNoDataValue = 0; // default to turning -9999 into a zero value...
		}
		mLayerDataFormat = layerType;
	}
	
	//--------------------------------------------------------------------------
	public Layer_Integer(String name) {
		
		this(name, EType.EPreShiftedIndex); // default to a pre-shifted (load time) index
	}

	// Call after contructor...But before Layer.init...if default conversion of -9999 to 0
	//	is not ok.	
	//--------------------------------------------------------------------------
	public void setNoDataConversion(int newConversionValue) {
		
		mConvertedNoDataValue = newConversionValue;
	}
	
	//--------------------------------------------------------------------------
	public int[][] getIntData() {
		
		return mIntData;
	}
	
	//--------------------------------------------------------------------------
	protected void allocMemory() {
		
		Logger.info("  Allocating INT work array");
		mIntData = new int[mHeight][mWidth];
	}
	
	// Copies a file read bytebuffer into the internal native int array...
	//--------------------------------------------------------------------------
	protected void readCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			mIntData[atY][x] = dataBuffer.getInt();
			cacheMinMax(mIntData[atY][x]); 
		}
	}

	// Copies the native int data into a bytebuffer that is set up to recieve it (by the caller)
	//--------------------------------------------------------------------------
	protected void writeCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			dataBuffer.putInt(mIntData[atY][x]);
		}
	}
	
	//--------------------------------------------------------------------------
	protected void processASC_Line(int y, String lineElementsArray[]) {
		
		boolean erred = false;
		for (int x = 0; x < lineElementsArray.length; x++) {
			int val = Integer.parseInt(lineElementsArray[x]);
			if (val == mNoDataValue) {
				val = mConvertedNoDataValue;
			}
			else {
				cacheMinMax(val);
				// Optionally convert to a bit style value for fast/simultaneous compares
				if (mLayerDataFormat == EType.EPreShiftedIndex) {
					if (val <= 31) {
						val = convertIndexToMask(val);
					}
					else if (!erred) {
						erred = true;
						Logger.error("  BAD value - indexed values can only be 1-31. Was: " 
							+ Integer.toString(val));
					}
				}
			}
			mIntData[y][x] = val;
		}
	}

	//--------------------------------------------------------------------------
	final private void cacheMinMax(int value) {
		
		if (value != mNoDataValue) {
			if (!mbInitedMinMaxCache) {
				mbInitedMinMaxCache = true;
				mMin = value;
				mMax = value;
			}
		
			if (value > mMax) {
				mMax = value;
			}
			else if (value < mMin) {
				mMin = value;
			}
		}
	}
	
	// Loads a color key if there is one....
	//--------------------------------------------------------------------------
	protected void onLoadEnd() {
		
		Logger.info("  Value range is: " + Integer.toString(mMin) + 
						" to " + Integer.toString(mMax));
		
		File colorKeyFile = new File("./layerData/" + mName + ".key");
		if (!colorKeyFile.exists())
		{
			return;
		}
		
		Logger.info("  Attempting to read color and name key file.");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(colorKeyFile));

			// now read the array data
			while (br.ready()) {
				String line = br.readLine().trim();
				
				if (!line.startsWith(";") && line.length() > 0) {
					String split[] = line.split(",");
				
					if (split.length < 2 || split.length > 3) {
						Logger.error("  Parse error reading /layerData/" + mName + ".key");
						Logger.error("  Error: <read>" + line);
						Logger.error("    Expected Line Format: Index, Display Name, Display Color (hex)");
						throw new Exception();
					}
					else {
						int index = Integer.parseInt(split[0].trim());
						String label = split[1].trim();
						
						String color = null;
						if (split.length == 3) {
							color = split[2].trim();
						}
								
						Layer_Key keyItem = new Layer_Key(index, label, color);
						mLayerKey.add(keyItem);
					}
				}
			}
		}
		catch (Exception e) {
			Logger.warn("  " + e.toString());
		}
		finally {
			if (br != null) {
				try {
					br.close();
				}
				catch (Exception e) {
					Logger.warn("  " + e.toString());
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------
	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {

		// Set this as a default - call super first so subclass can override a return result
		//	for the same parameter request type. Unsure we need that functionality but...
		JsonNode ret = super.getParameterInternal(clientRequest);

		String type = clientRequest.get("type").textValue();
		if (type.equals("colorKey")) {
			ret = getColorKeyAsJson();
		}
		
		return ret;
	}
	
	//--------------------------------------------------------------------------
	public JsonNode getColorKeyAsJson() {
		
		ArrayNode ret = JsonNodeFactory.instance.arrayNode();
		
		for (int i = 0; i < mLayerKey.size(); i++) {
			
			// NOTE: some color keys may have a NULL color hex string which
			//	means that they do not get sent to the client.
			//	They can still be queried from the JAVA code...
			JsonNode val = mLayerKey.get(i).getAsJson();
			if (val != null) {
				ret.add(val);
			}
		}
		
		return ret;
	}

	// Takes an index on an indexed raster and converts it to the appropriate
	//	bit position. Index must be 1 based and not more than 31 (ie, 1-31)
	//--------------------------------------------------------------------------
	public static int convertIndexToMask(int index) {
		
		if (index <= 0 || index > 31) {
			Logger.warn("Bad index in convertIndexToMask: " + Integer.toString(index));
			return 0;
		}
		
		return (1 << (index-1));
	}
	
	// Takes a variable number of integer arguments...can be called like these: 
	//	int mask1 = Layer_Indexed.convertIndicesToMask(1,5,8,11,15);
	//	int mask2 = Layer_Indexed.convertIndicesToMask(2,3,7);
	//--------------------------------------------------------------------------
	public static int convertIndicesToMask(int... indicesList) {
		
		int result = 0;
		for (int i=0; i < indicesList.length; i++) {
			result |= convertIndexToMask(indicesList[i]);
		}
		
		return result;
	}

	//--------------------------------------------------------------------------
	public int getIndexForString(String indexName) {
		
		for (int t=0; t < mLayerKey.size(); t++) {
			Layer_Key key = mLayerKey.get(t);
			if (key != null && key.mLabel.equalsIgnoreCase(indexName)) {
				return key.mIndex;
			}
		}
		
		return 0;
	}
	
	// Takes a variable number of String arguments...can be called like these: 
	//	int mask1 = Layer_Indexed.convertStringsToMask("corn","soy","woodland");
	//	int mask2 = Layer_Indexed.convertStringsToMask("corn");
	//--------------------------------------------------------------------------
	public int convertStringsToMask(String... nameList) {
		
		int result = 0;
		for (int i=0; i < nameList.length; i++) {
			result |= convertIndexToMask(getIndexForString(nameList[i]));
		}
		
		return result;
	}
	
	//--------------------------------------------------------------------------
	private int getCompareBitMask(JsonNode matchValuesArray) {
		
		int queryMask = 0;
		
		ArrayNode arNode = (ArrayNode)matchValuesArray;
		if (arNode != null) {
			int count = arNode.size();
			Logger.debug("Query Index Array count: " + Integer.toString(count));
			StringBuffer debug = new StringBuffer();
			debug.append("Query Indices: ");
			for (int i = 0; i < count; i++) {
				JsonNode node = arNode.get(i);
				
				int val = node.intValue(); // FIXME: default value?
				debug.append(Integer.toString(val));
				if (i < count - 1) {
					debug.append(", ");
				}
				queryMask |= convertIndexToMask(val);
			}
			
			Logger.debug(debug.toString());
			Logger.info("Final Query Mask: " + Integer.toString(queryMask));
			return queryMask;
		}
		
		return 1;
	}

	//--------------------------------------------------------------------------
	private Set<Integer> getCompareSet(JsonNode matchValuesArray) {
		
		ArrayNode arNode = (ArrayNode)matchValuesArray;
		if (arNode != null) {
			int count = arNode.size();
			if (count < RAW_BREAK_EVEN_COUNT) return null; // Break even point?
				
			Set<Integer> set = new HashSet<Integer>(count);
			for (int i = 0; i < count; i++) {
				JsonNode node = arNode.get(i);
				
				int val = node.intValue(); // FIXME: default value?
				set.add(val);
			}
			Logger.info(set.toString());
			return set;
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	private int[] getCompareArray(JsonNode matchValuesArray) {
		
		ArrayNode arNode = (ArrayNode)matchValuesArray;
		if (arNode != null) {
			int count = arNode.size();
			int array[] = new int [count];
			Logger.debug("Query Index Array count: " + Integer.toString(count));
			StringBuffer debug = new StringBuffer();
			debug.append("Query Indices: ");
			for (int i = 0; i < count; i++) {
				JsonNode node = arNode.get(i);
				
				int val = node.intValue(); // FIXME: default value?
				debug.append(Integer.toString(val));
				if (i < count - 1) {
					debug.append(", ");
				}
				array[i] = val;
			}
			
			Logger.debug(debug.toString());
			return array;
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	private Selection doRawQuery(JsonNode queryNode, Selection selection) {
		
		Set<Integer> set = getCompareSet(queryNode);
		
		// We'll get a set back if we're near the supposed break-even point....
		if (set != null) {
			for (int y = 0; y < mHeight; y++) {
				for (int x = 0; x < mWidth; x++) {
					boolean found = false;
					// Only check values that ARE NOT noData and where Selection is 1
					if (mIntData[y][x] >= 0 && selection.mRasterData[y][x] > 0) {
						if (set.contains(mIntData[y][x])) {
							found = true;
						}
					}
					selection.mRasterData[y][x] &= (found ? 1 : 0);
				}
			}
			
			return selection;
		}
		
		// Else....Doing a slower per-array-element test...
		int array[] = getCompareArray(queryNode);
		if (array != null) {
			for (int y = 0; y < mHeight; y++) {
				for (int x = 0; x < mWidth; x++) {
					boolean found = false;
					// Only check values that ARE NOT noData and where Selection is 1
					if (mIntData[y][x] >= 0 && selection.mRasterData[y][x] > 0) {
						for (int i = 0; i < array.length; i++) {
							if (mIntData[y][x] == array[i]) {
								found = true;
								break;
							}
						}
					}
					selection.mRasterData[y][x] &= (found ? 1 : 0);
				}
			}
		}
		else {
			Logger.warn("Tried to get a match array but it failed!");
		}
		
		return selection;
	}
	
	//--------------------------------------------------------------------------
	protected Selection query(JsonNode queryNode, Selection selection) {

		Logger.info("Running indexed query");
		JsonNode queryValues = queryNode.get("matchValues");

		if (mLayerDataFormat == EType.ERaw) {
			selection = doRawQuery(queryValues, selection);
		}
		else {
			// Doing the faster bit-mask check...
			int test_mask = getCompareBitMask(queryValues);
			if (mLayerDataFormat == EType.EPreShiftedIndex) {
				// Doing the fastest already-shifted test...
				for (int y = 0; y < mHeight; y++) {
					for (int x = 0; x < mWidth; x++) {
						selection.mRasterData[y][x] &= ((mIntData[y][x] & test_mask) > 0 ? 1 : 0);
					}
				}
			}
			else if (mLayerDataFormat == EType.EQueryShiftedIndex) {
				// doing the slightly less fast shift-at-each-pixel test...
				for (int y = 0; y < mHeight; y++) {
					for (int x = 0; x < mWidth; x++) {
						int shifted = (1 << (mIntData[y][x]-1));
						selection.mRasterData[y][x] &= ((shifted & test_mask) > 0 ? 1 : 0);
					}
				}
			}
			else {
				Logger.error("Unhandled and known integer layer type!");
			}
		}
		
		return selection;
	}
}

