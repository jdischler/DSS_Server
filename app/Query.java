package util;

import play.*;
import java.util.*;
import java.io.*;

import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Query {
	
	static int mCounter;
	static int mWidth, mHeight; // FIXME: why static?

	//------------------------------------------------------------------------------
	public JsonNode selection(JsonNode requestBody) throws Exception
	{
		// selection color
		int red = 255;
		int green = 0;
		int blue = 96;
		int x, y;
		int resampleFactor = 5;
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		//	all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("rotation");
		mWidth = tmp.getWidth();
		mHeight = tmp.getHeight();
		
		Logger.info("Called into query");
		Logger.info(requestBody.toString());
		
		String partialPath = "/public/file/test" + String.valueOf(mCounter) + ".png";
		// FIXME: not sure why play doesn't hand me back the expected directory path in production?
		if (Play.isProd()) {
			// FIXME: blugh, like this won't be totally fragile? :)
			partialPath = "/target/scala-2.10/classes" + partialPath;
		}
		String urlPath = "/app/file/test" + String.valueOf(mCounter) + ".png";
		Logger.info("File write path: " + partialPath);
		mCounter++;
		
		// 8 bits per pixel, one channel (indexed), file path where the png is saved
		// Since this is the file we are saving, it will be smaller than the actual
		//	width/height by the sample factor.
		Png png = new Png(mWidth / resampleFactor, mHeight / resampleFactor, 
				8, 1, 
				"." + partialPath);
		
		PngChunkPLTE palette = png.createPalette(2);
		palette.setEntry(0, 0,0,0); // black
		palette.setEntry(1, red, green, blue);
		
		// set index 0 (black) as transparent
		png.setTransparentIndex(0);

		// Set up to run the query...allocate memory...
		Selection selection = execute(requestBody);
		
		// Pass the whole array at the full size...and let the writer do the resample
		//	to convert this to the size the png will be written at
		png.writeResampledSelection(mWidth, mHeight, selection);
		
		// Get query statistics (number of selected pixels)
		int count = selection.countSelectedPixels();	

		Logger.info("Query Statistics");
		Logger.info("-----------------------");
		Logger.info("Total selected pixels: " + Integer.toString(count));
		Logger.info("Square km: " + Float.toString(count * 0.03f * 0.03f));

		// Data to return to the client		
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		ret.put("url", urlPath);
		ret.put("selectedPixels", count);
		ret.put("totalPixels", mHeight * mWidth);

		return ret;
	}
	
	//--------------------------------------------------------------------------
	public Selection execute(JsonNode requestBody) throws Exception {
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		// all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("rotation");
		mWidth = tmp.getWidth();
		mHeight = tmp.getHeight();
		
		Selection selection = new Selection(mWidth, mHeight);

		// Actually run the query...
		JsonNode layerList = requestBody.get("queryLayers");
		Layer_Base.execQuery(layerList, selection);
		return selection;
	}
	
}
