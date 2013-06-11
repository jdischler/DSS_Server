package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public abstract class Layer_Base
{
	private static Map<String, Layer_Base>	mLayers;
	
	protected String mName;
	protected int mWidth, mHeight;
	protected int mNoDataValue;
	protected int[][] mIntData;

	// Return the Layer_Base object when asked for it by name
	//--------------------------------------------------------------------------
	public static Layer_Base getLayer(String name) {
		
		name = name.toLowerCase();
		return mLayers.get(name);
	}

	// Use carefully...e.g., only if you are temporarily loading data for a process that rarely runs...
	//--------------------------------------------------------------------------
	public static void removeLayer(String name) {

		Logger.info("A call was made to remove layer: " + name + " from memory");
		name = name.toLowerCase();
		mLayers.remove(name);
	}
	
	// Use even more carefully...currently only be used when the server shuts down.
	//--------------------------------------------------------------------------
	public static void removeAllLayers() {

		Logger.info("A call was made to clear all Layers!");
		mLayers.clear();
	}
	
	// Base constructor
	//--------------------------------------------------------------------------
	public Layer_Base(String name) {
		
		mName = name.toLowerCase();
		if (mLayers == null) {
			mLayers = new HashMap<String, Layer_Base>();
		}
		mLayers.put(mName, this);
	}
	
	//--------------------------------------------------------------------------
	public int getWidth() {
		return mWidth;
	}

	//--------------------------------------------------------------------------
	public int getHeight() {
		return mHeight;
	}
	
	//--------------------------------------------------------------------------
	public int[][] getIntData() {
		return mIntData;
	}
	
	//--------------------------------------------------------------------------
	public void init() {
		
		try{
			loadASC();
		}
		catch(Exception e) {
			Logger.error(e.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	private String getASC_HeaderValue(String line) {
		
		String split[] = line.split("\\s+");
		if (split.length == 2) {
			return split[1];
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	private void readASC_Header(BufferedReader reader) throws Exception {
		
		try {
			String width = reader.readLine(); // ncols
			String height = reader.readLine(); // nrows
			
			String tmp = reader.readLine(); /* xll corner */ Logger.info(tmp);
			tmp = reader.readLine(); /* yll corner */ Logger.info(tmp);
			tmp = reader.readLine(); /* cellsize */ Logger.info(tmp);
			String noData = reader.readLine(); // nodata value
			
			Logger.info(width);
			Logger.info(height);
			Logger.info(noData);
			
			mWidth = Integer.parseInt(getASC_HeaderValue(width));
			mHeight = Integer.parseInt(getASC_HeaderValue(height));
			mNoDataValue = Integer.parseInt(getASC_HeaderValue(noData));
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	private void loadASC() throws Exception {
		
		Logger.info("---------------------");
		Logger.info(mName);
		Logger.info("---------------------");
		
		BufferedReader br = new BufferedReader(new FileReader("./layerData/" + mName + ".asc"));
		try {
			readASC_Header(br);
			allocMemory(mWidth, mHeight);
			
			Logger.info("Attempting to read the array data");
			
			int x = 0, y = 0;
			
			// now read the array data
			while (br.ready()) {
				if (y >= mHeight) {
					Logger.error("BAD READ - more lines than expected!");
					break;
				}
				String line = br.readLine();
				String split[] = line.split("\\s+");
				processASC_Line(y, split);
				y++;
			}
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
			br.close();
		}
		
		onLoadEnd();
	}
	
	//--------------------------------------------------------------------------
	protected void allocMemory(int width, int height) {
		
		Logger.info("Allocating new work array");
		mIntData = new int[mHeight][mWidth];
	}
	
	// Functions that must be in the subclass. 
	//--------------------------------------------------------------------------
	abstract protected void onLoadEnd();
	abstract protected void processASC_Line(int y, String lineElementsArray[]);
	abstract protected int[][] query(JsonNode queryNode, int[][] workArray);
		
	//--------------------------------------------------------------------------
	public static void execQuery(JsonNode layerList, int[][] workArray) {

		if (layerList.isArray()) {
	
			ArrayNode arNode = (ArrayNode)layerList;
			int count = arNode.size();
			for (int i = 0; i < count; i++) {
				Logger.info("Processing one array element in the queryLayers layer list");
				JsonNode arElem = arNode.get(i);
				JsonNode layerName = arElem.get("name");
				if (arElem != null && layerName != null) {
					Layer_Base layer = Layer_Base.getLayer(layerName.getValueAsText());
					if (layer != null) {
						layer.query(arElem, workArray);
					}
				}
			}
		}
	}
}
	

