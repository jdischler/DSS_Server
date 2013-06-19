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
	static int mWidth, mHeight;

	//------------------------------------------------------------------------------
	public JsonNode exec(JsonNode requestBody) throws Exception
	{
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		int x, y;
		// blah, selection color
		int red = 255;
		int green = 0;
		int blue = 96;
		int resampleFactor = 5;
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		//	all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("rotation");
		mWidth = tmp.getWidth();//4710;//3791;
		mHeight = tmp.getHeight();//3869;//3133;
		
		int[][] imgArray = new int[mHeight][mWidth];
		
		// BOOO, prep the array for & logic
		// FIXME: don't use hardcoded image sizes, either 		
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				imgArray[y][x] = 1;
			}
		}

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
		
		// 8 bits per pixel, one channel (indexed);
		Png png = new Png(mWidth / resampleFactor, mHeight / resampleFactor, 
				8, 1, 
				"." + partialPath);
		
		PngChunkPLTE palette = png.createPalette(2);
		palette.setEntry(0, 0,0,0); // black
		palette.setEntry(1, red, green, blue);
		
		// set index 0 (black) as transparent
		png.setTransparentIndex(0);

		// Actually run the query...
		JsonNode layerList = requestBody.get("queryLayers");
		Layer_Base.execQuery(layerList, imgArray);
		
		png.writeResampledArray(mWidth, mHeight, imgArray);
		
		int count = 0;	
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				count += imgArray[y][x];
			}
		}

		Logger.info("Query Statistics");
		Logger.info("-----------------------");
		Logger.info("Total selected pixels: " + Integer.toString(count));
		Logger.info("Square km: " + Float.toString(count * 0.03f * 0.03f));
		
		ret.put("url", urlPath);
		ret.put("selectedPixels", count);
		ret.put("totalPixels", mHeight * mWidth);

		return ret;
	}
}
