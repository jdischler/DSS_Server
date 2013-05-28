package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Layer_Indexed extends Layer_Base
{
/*	private Integer mWidth, mHeight;
	private Integer mNoDataValue;
	private int[][] mData;
*/
	// TODO: hold data for index, colors, names, etc...
	
	//--------------------------------------------------------------------------
	public Layer_Indexed(String name) {
		super(name);
	}
	
	// TODO: Read in indexed file for colors, names, etc.
	
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
				if (val <= 31) {
					val = (1 << (val-1));
				}
				else if (!erred) {
					erred = true;
					Logger.error("BAD value - indexed values can only be 1-31. Was: " 
						+ Integer.toString(val));
				}
			}
			mData[y][x] = val;
		}
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
				queryMask |= (1 << (val-1));
			}
			
			Logger.info(Integer.toString(queryMask));
			return queryMask;
		}
		
		return 1;
	}
	
	//--------------------------------------------------------------------------
	public int[][] query(JsonNode queryNode, int[][] workArray) {

		Logger.info("Running indexed query");
		JsonNode queryValues = queryNode.get("matchValues");
		int test_mask = getCompareBitMask(queryValues);

		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				workArray[y][x] &= ((mData[y][x] & test_mask) > 0 ? 1 : 0);
			}
		}
		return workArray;
	}
}
	

