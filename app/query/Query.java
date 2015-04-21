package util;

import play.*;
import java.util.*;
import java.io.*;

import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Query {
	
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}
	
	static int mCounter;
	static int mWidth, mHeight; // FIXME: why static?

	//------------------------------------------------------------------------------
	public JsonNode selection(JsonNode requestBody, ClientUser user) throws Exception
	{
		// selection color
		int red = 255;
		int green = 0;
		int blue = 96;
		int x, y;
		int resampleFactor = 5;
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		//	all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("cdl_2012");
		mWidth = tmp.getWidth();
		mHeight = tmp.getHeight();
		
		detailedLog("Called into query");
		detailedLog(requestBody.toString());
		
		int ctr = mCounter++;
		String partialPath = "/public/dynamicFiles/selection" + String.valueOf(ctr) + ".png";
		// FIXME: not sure why play doesn't hand me back the expected directory path in production?
		if (Play.isProd()) {
			// FIXME: blugh, like this won't be totally fragile? :)
//			partialPath = "/target/scala-2.10/classes" + partialPath;
		}
		String urlPath = "/files/selection" + String.valueOf(ctr) + ".png";
		detailedLog("File write path: " + partialPath);
		
		// 8 bits per pixel, one channel (indexed), file path where the png is saved
		// Since this is the file we are saving, it will be smaller than the actual
		//	width/height by the sample factor.
		int newWidth = mWidth / resampleFactor;
		int newHeight = mHeight / resampleFactor;
		Png png = new Png(newWidth, newHeight, 
				8, 1, 
				"." + partialPath);
		
		PngChunkPLTE palette = png.createPalette(5);
		palette.setEntry(0, 0,0,0); // black
		palette.setEntry(1, red, green, blue);
		palette.setEntry(2, red, green, blue);
		palette.setEntry(3, red, green, blue);
		palette.setEntry(4, red, green, blue);
		
		int[] alpha = new int[5];
		// Reducing alpha's for sub-100% values since people seem to visuallly feel the selection
		//	doesn't match the reported % of landscape selected...
		alpha[0] = 0; alpha[1] = 50; alpha[2] = 100; alpha[3] = 150; alpha[4] = 255;
		
		png.setTransparentArray(alpha);

		// Set up to run the query...allocate memory...
		Selection selection = execute(requestBody, user);
		
		byte[][] temp = Downsampler.generateSelection(selection.mRasterData, 
								selection.getWidth(), selection.getHeight(),
								5, // transform to 5 colors
								newWidth, newHeight);
		png.writeArray(temp);

		// Get query statistics (number of selected pixels)
		int count = selection.countSelectedPixels();	

		detailedLog("Query Statistics");
		detailedLog("-----------------------");
		detailedLog("Total selected pixels: " + Integer.toString(count));
		detailedLog("Square km: " + Float.toString(count * 0.03f * 0.03f));

		// Data to return to the client		
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		ret.put("url", urlPath);
		ret.put("selectedPixels", count);
		ret.put("totalPixels", mHeight * mWidth);

		return ret;
	}
	
	//NOTE: user can be null
	//--------------------------------------------------------------------------
	public Selection execute(JsonNode requestBody, ClientUser user) throws Exception {
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		// all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("cdl_2012");
		mWidth = tmp.getWidth();
		mHeight = tmp.getHeight();
		
		Selection selection = new Selection(mWidth, mHeight);

		// Actually run the query...
		JsonNode layerList = requestBody.get("queryLayers");
		Layer_Base.execQuery(layerList, selection, user);
		return selection;
	}
	
}
