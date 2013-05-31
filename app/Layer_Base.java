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
	protected Integer mWidth, mHeight;
	protected Integer mNoDataValue;
	protected int[][] mData;

	//--------------------------------------------------------------------------
	public Layer_Base(String name) {
		
		mName = name.toLowerCase();
		if (mLayers == null) {
			mLayers = new HashMap<String, Layer_Base>();
		}
		mLayers.put(name, this);
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
			
			Logger.info("Allocating new work array");
			mData = new int[mHeight][mWidth];
			
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
	public static Layer_Base getLayer(String name) {
		
		return mLayers.get(name);
	}
	
	//--------------------------------------------------------------------------
	abstract protected void onLoadEnd();
	abstract protected int[][] query(JsonNode queryNode, int[][] workArray);
	abstract protected void processASC_Line(int y, String lineElementsArray[]);
		
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
	

