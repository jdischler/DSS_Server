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
	public String exec(JsonNode requestBody) throws Exception
	{
		int x, y;
		// blah, selection color
		int red = 255;
		int green = 0;
		int blue = 96;
		
		// TEMP: just rotate the color to get a different selection color
		if ((mCounter & 0x3) == 1) {
			red = 0;
			green = 128;
			blue = 255;
		}
		else if ((mCounter & 0x3) == 2) {
			red = 255;
			green = 0;
			blue = 255;
		}
		
		// FIXME: can't base size off of a hardcoded layer? The expectation is that
		//	all layers are of the same size....
		Layer_Base tmp = Layer_Base.getLayer("slope");
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
		
		// create image at 8 bit per pixel, indexed
		ImageInfo imgInfo = new ImageInfo(mWidth, mHeight, 8, false, false, true);
		OutputStream outputStream = new FileOutputStream("." + partialPath);
		PngWriter pngW = new PngWriter(outputStream, imgInfo);
//		pngW.setCompLevel(9); // compression level not working?
		pngW.getMetadata().setDpi(306.98);
		PngChunkPLTE palette = pngW.getMetadata().createPLTEChunk();
		palette.setNentries(2);
		palette.setEntry(0, 0,0,0); // black
		palette.setEntry(1, red, green, blue);
		
		// set index 0 (black) as transparent
		PngChunkTRNS trans = pngW.getMetadata().createTRNSChunk();
		trans.setIndexEntryAsTransparent(0);

		// Actually run the query...
		JsonNode layerList = requestBody.get("queryLayers");
		Layer_Base.execQuery(layerList, imgArray);
		
		pngW.writeRowsInt(imgArray);
		pngW.end();
		
//		return ok("http://dss.wei.wisc.edu:9000/app" + partialPath);
		return urlPath;
	}
}
