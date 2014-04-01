package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public abstract class Layer_Base
{
	// STATIC DATA --------------------------
	// NOTE: Width, height, cellSize, and corners were deliberately made static because
	//	no code can currently handle layers of different dimensions or data density.
	private static Map<String, Layer_Base>	mLayers;
	
	protected static int mWidth, mHeight;
	protected static float mCellSize, mCornerX, mCornerY;
	
	protected final static boolean mbUseBinaryFormat = true;
	protected final static int mBinaryWriteVersion = 1; // NOTE: update version for each new header version change
	
	// CLASS DATA --------------------------
	protected String mName;
	// TODO: move noDataValue into subclasses? Is that even possible? Considering float might store NaN
	protected int mNoDataValue;
	
	// Return the Layer_Base object when asked for it by name
	//--------------------------------------------------------------------------
	public static Layer_Base getLayer(String name) {
		
		if (mLayers == null) {
			Logger.info("Info: getLayer called with no mLayers. Allocating one.");
			mLayers = new HashMap<String, Layer_Base>();
		}
		String name_low = name.toLowerCase();
		if (!mLayers.containsKey(name_low)) {
			Logger.info("getLayer called looking for: " + name_low + "  but layer doesn't exist");
			return null;
		}
		return mLayers.get(name_low);
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

		Logger.info(" ... A call was made to clear all Layers!");
		mLayers.clear();
	}
	
	// Base constructor
	//--------------------------------------------------------------------------
	public Layer_Base(String name) {
		
		this(name, false); // not temporary...
	}
	
	// TEMPORARY layers are not added to the managed list...
	//--------------------------------------------------------------------------
	public Layer_Base(String name, boolean temporary) {
	
		mName = name.toLowerCase();
		if (!temporary) {
			if (mLayers == null) {
				mLayers = new HashMap<String, Layer_Base>();
			}
			mLayers.put(mName, this);
		}
	}
	
	//--------------------------------------------------------------------------
	public int getWidth() {
		return mWidth;
	}
	public int getHeight() {
		return mHeight;
	}
	
	//--------------------------------------------------------------------------
	public int[][] getIntData() {
		return null;
	}
	
	//--------------------------------------------------------------------------
	public float[][] getFloatData() {
		return null;
	}
	
	//--------------------------------------------------------------------------
	public void init() {
		
		try {
			// TODO: FIXME: binary format reading or writing has an issue, not sure which
			if (mbUseBinaryFormat) {
				File input = new File("./layerData/" + mName + ".dss");
				if(input.exists()) {
					readBinary();
				}
				else {
					// wanting to use binary format but file doesn't exist...
					// 	load ASC, save as Binary
					loadASC();
					writeBinary();
				}
			}
			else {
				loadASC();
			}
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
			String xllCorner = reader.readLine(); // xll corner
			String yllCorner = reader.readLine(); // yll corner
			String cellSize = reader.readLine(); // cellsize
			String noData = reader.readLine(); // nodata value
			
			// Echo string values in ASC header
			Logger.info("  " + width);
			Logger.info("  " + height);
			Logger.info("  " + xllCorner);
			Logger.info("  " + yllCorner);
			Logger.info("  " + cellSize);
			Logger.info("  " + noData);
			
			// convert to required data types and save
			mWidth = Integer.parseInt(getASC_HeaderValue(width));
			mHeight = Integer.parseInt(getASC_HeaderValue(height));
			mCornerX = Float.parseFloat(getASC_HeaderValue(xllCorner));
			mCornerY = Float.parseFloat(getASC_HeaderValue(yllCorner));
			mCellSize = Float.parseFloat(getASC_HeaderValue(cellSize));
			mNoDataValue = Integer.parseInt(getASC_HeaderValue(noData));
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	private void loadASC() throws Exception {
		
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| " + mName);
		Logger.info("+-------------------------------------------------------+");
		
		BufferedReader br = new BufferedReader(new FileReader("./layerData/" + mName + ".asc"));
		try {
			readASC_Header(br);
			allocMemory();
			
			Logger.info("  Attempting to read the array data");
			
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
		Logger.info(" ");
	}
	
	//--------------------------------------------------------------------------
	abstract protected void allocMemory();
	
	// Generally comes from a client request for data about this layer...
	//	this could be layer width/height, maybe cell size (30m), maybe the layer
	//	ranges, in the case of indexed layers...the key/legend for the layer
	//--------------------------------------------------------------------------
	public static JsonNode getParameter(JsonNode clientRequest) throws Exception {
		
		JsonNode ret = null;
		String layername = clientRequest.get("name").textValue();
		
		Layer_Base layer = Layer_Base.getLayer(layername);
		if (layer != null) {
			ret = layer.getParameterInternal(clientRequest);
		}

		return ret;
	}
	
	// ordering expects subclasses to call the super (this) first...
	//--------------------------------------------------------------------------
	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {
		
		// Set this as a default
		JsonNode ret = null;
		
		// Do any processing here if the base layer class needs to...
		//	e.g., if we ever need to pass back layer dimensions, cell size, etc...
		
		return ret;
	}
	
	// Functions that must be in the subclass. 
	//--------------------------------------------------------------------------
	abstract protected void onLoadEnd();
	abstract protected void processASC_Line(int y, String lineElementsArray[]);
	abstract protected Selection query(JsonNode queryNode, Selection selection);
		// Copies a file read bytebuffer into the internal native float array...
	abstract protected void readCopy(ByteBuffer dataBuffer, int width, int atY);
		// Copies the native float data into a bytebuffer that is set up to recieve it (by the caller)
	abstract protected void writeCopy(ByteBuffer dataBuffer, int width, int atY);
		
	//--------------------------------------------------------------------------
	public static void execQuery(JsonNode layerList, Selection selection) {

		if (layerList.isArray()) {
	
			ArrayNode arNode = (ArrayNode)layerList;
			int count = arNode.size();
			for (int i = 0; i < count; i++) {
				Logger.info("Processing one array element in the queryLayers layer list");
				JsonNode arElem = arNode.get(i);
				JsonNode layerName = arElem.get("name");
				if (arElem != null && layerName != null) {
					Layer_Base layer = Layer_Base.getLayer(layerName.textValue());
					if (layer != null) {
						layer.query(arElem, selection);
					}
				}
			}
		}
	}

	//--------------------------------------------------------------------------
	// BINARY format reading/writing
	//	~3-6x faster 
	//--------------------------------------------------------------------------
	private void writeBinary() throws Exception {
		
		Logger.info("Writing Binary");
		File output = new File("./layerData/" + mName + ".dss");

		FileOutputStream fos = new FileOutputStream(output);
		try {
			WritableByteChannel channel = fos.getChannel();
			ByteBuffer buf = ByteBuffer.allocateDirect(4); // FIXME: size of int
			
			// write version type			
			buf.putInt(mBinaryWriteVersion);
			buf.flip();
			channel.write(buf);
			
			buf = ByteBuffer.allocateDirect(6 * 4); // FIXME: header field ct * size of int
			
			buf.putInt(mWidth);
			buf.putInt(mHeight);
			buf.putFloat(mCornerX);
			buf.putFloat(mCornerY);
			buf.putFloat(mCellSize);
			buf.putInt(mNoDataValue);
			buf.flip();
			channel.write(buf);
			
			buf = ByteBuffer.allocateDirect(mWidth * 4); // FIXME: size of int?
			
			for (int y = 0; y < mHeight; y++) {
				// shuttle native internal data, line by line, into buf for writing
				buf.clear();
				writeCopy(buf, mWidth, y);
				buf.flip();
				channel.write(buf);
			}
			channel.close();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
			fos.close();
		}
	}
	
	//--------------------------------------------------------------------------
	private void readBinary() throws Exception {
		
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| Binary Read: " + mName);
		Logger.info("+-------------------------------------------------------+");
		File input = new File("./layerData/" + mName + ".dss");

		Logger.info("  Real Path: " + input.getCanonicalPath());
		FileInputStream fis = new FileInputStream(input);
		
		try {
			ReadableByteChannel channel = fis.getChannel();
			
			Logger.info("  Reading header...");
			ByteBuffer buf = ByteBuffer.allocateDirect(4); // FIXME: size of int (version)?
			channel.read(buf); 
			buf.rewind();
			Logger.info("  - Binary file version: " + Integer.toString(buf.getInt()));
				
			buf = ByteBuffer.allocateDirect(6 * 4); // FIXME: size of header * size of int?
			channel.read(buf); 
			buf.rewind();
			
			mWidth = buf.getInt();
			mHeight = buf.getInt();
			mCornerX = buf.getFloat();
			mCornerY = buf.getFloat();
			mCellSize = buf.getFloat();
			mNoDataValue = buf.getInt();
			
			Logger.info("  - Width: " + Integer.toString(mWidth) 
							+ "  Height: " + Integer.toString(mHeight));
			allocMemory();
			
			buf = ByteBuffer.allocateDirect(mWidth * 4); // FIXME: size of int?
			
			for (int y = 0; y < mHeight; y++) {
				// shuttle read data from buf, line by line, into native internal arrays.
				buf.clear();
				channel.read(buf);
				buf.rewind();
				readCopy(buf, mWidth, y);
			}
			channel.close();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
			fis.close();
		}
		
		onLoadEnd();
		Logger.info("");
	}
}

