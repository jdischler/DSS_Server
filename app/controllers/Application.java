package controllers;

import util.*;

import java.util.*;
import java.io.*;

import play.*;
import play.mvc.*;
import play.Logger;
import play.cache.*;

import views.html.*;

import ar.com.hjg.pngj.*;
import ar.com.hjg.pngj.chunks.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import javax.xml.bind.DatatypeConverter;

//------------------------------------------------------------------------------
public class Application extends Controller {
	
	static int counter;
	static int[][] mImgArray;
	static int mWidth, mHeight;
	
	//--------------------------------------------------------------------------
	public static Result index() {
		
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception {
		
		int x, y;
		// blah, selection color
		int red = 255;
		int green = 64;
		int blue = 0;
		
		mWidth = 3791;
		mHeight = 3133;
		mImgArray = new int[mHeight][mWidth];
		
		// BOOO, prep the array for & logic
		// FIXME: don't use hardcoded image sizes, either 		
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				mImgArray[y][x] = 1;
			}
		}

		Logger.info("Called into query");
		JsonNode valNode = request().body().asJson();
		Logger.info(valNode.toString());
		
//		String fullPath = Play.application().path().getPath() + "/public/file/test" + String.valueOf(counter) + ".png";
//		Logger.info(fullPath);
		
		String partialPath = "/public/file/test" + String.valueOf(counter) + ".png";
		String urlPath = "/app/file/test" + String.valueOf(counter) + ".png";
		Logger.info(partialPath);
		counter++;
		
		// create 3791x3133, 8 bit per pixel, indexed
		ImageInfo imgInfo = new ImageInfo(mWidth,mHeight,8,false,false,true);
		OutputStream outputStream = new FileOutputStream("." + partialPath);
		PngWriter pngW = new PngWriter(outputStream, imgInfo);
//		pngW.setCompLevel(9);
		pngW.getMetadata().setDpi(306.98);
		PngChunkPLTE palette = pngW.getMetadata().createPLTEChunk();
		palette.setNentries(2);
		palette.setEntry(0, 0,0,0); 				// black
		palette.setEntry(1, red, green, blue);
		
		// set index 0 (black) as transparent
		PngChunkTRNS trans = pngW.getMetadata().createTRNSChunk();
		trans.setIndexEntryAsTransparent(0);
	
		JsonNode cdl_node = valNode.get("cdl_value");
		if (cdl_node != null) {
			int cdl_val = cdl_node.getIntValue();

			int[][] cdl = Global.getLayer("cdl");
			
			for (y = 0; y < imgInfo.rows; y++) {
				for (x = 0; x < imgInfo.cols; x++) {
					mImgArray[y][x] &= (cdl[y][x] == cdl_val ? 1 : 0);
				}
			}
		}
		
		Logger.info("checking to see if has slope query");
		JsonNode slope_node = valNode.get("slope");
		if (slope_node != null) {
			Logger.info("has slope query");
			JsonNode gtr = slope_node.get("greater");
			JsonNode less = slope_node.get("less");
			
			int slope_greater = 0 * 4 - 1;
			int slope_less = 90 * 4 + 1;
			
			if (gtr != null) slope_greater = gtr.getIntValue() * 4;
			if (less != null) slope_less = less.getIntValue() * 4;

			int[][] slope = Global.getLayer("slope");
			
			for (y = 0; y < imgInfo.rows; y++) {
				for (x = 0; x < imgInfo.cols; x++) {
					mImgArray[y][x] &= ((slope[y][x] > slope_greater && slope[y][x] < slope_less) ? 1 : 0);
				}
			}
		}
		
/*ELSE num_parms == 2
		for (int y = 0; y < imgInfo.rows; y++) {
			for (int x = 0; x < imgInfo.cols; x++) {
				mImgArray[y][x] = (cdl[y][x] == cdl_val_1 || cdl[y][x] == cdl_val_2) ? 1 : 0;
			}
		}
*/			
/*		int[][] watershed = Global.getLayer("watersheds");
		if (cdl != null) {
			Logger.info("getLayer succeeded!!! wahooo");
		}
		else {
			Logger.info("Boo-ums...getLayer was failsauced");
		}
		
		for (int y = 0; y < imgInfo.rows; y++) {
			for (int x = 0; x < imgInfo.cols; x++) {
				mImgArray[y][x] &= (watershed[y][x] == 266 ? 1 : 0);
			}
		}
	*/	
		pngW.writeRowsInt(mImgArray);
		pngW.end();
		
//		return ok("http://dss.wei.wisc.edu:9000/app" + partialPath);
		return ok("http://localhost:9000" + urlPath);
	}
}