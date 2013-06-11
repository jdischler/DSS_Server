package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;


// example ("slope", 0.0, 90.0, 0, 360); .. remap a slope of 0.0-90.0 to 0-360
//------------------------------------------------------------------------------
public class Layer_Continuous extends Layer_Base
{
	// for range reprojection
	float 	mFloatMin, mFloatMax;
	int		mIntMin, mIntMax;
	
	boolean mbInitedMinMaxCache;
	float	mLayerMin, mLayerMax;
	
	//--------------------------------------------------------------------------
	public float getLayerMin() {
		return mLayerMin;
	}
	
	//--------------------------------------------------------------------------
	public float getLayerMax() {
		return mLayerMax;
	}
	
	//--------------------------------------------------------------------------
	public Layer_Continuous(String name, float fMin, float fMax, int iMin, int iMax) {

		super(name);

		mFloatMin = fMin;
		mFloatMax = fMax;
		mIntMin = iMin;
		mIntMax = iMax;
		mbInitedMinMaxCache = false;
	}
	
	// ONLY "project" to new range if in the specified float range to being with
	//	This allows nodata values (such as -9999) to pass through
	//--------------------------------------------------------------------------
	private int projectFloatToInt(float value) {
		
		if (value >= mFloatMin && value <= mFloatMax) {
			//normalize (0-1)
			value = ((value - mFloatMin) / (mFloatMax - mFloatMin));
			// "reproject" to new range
			value = value * (mIntMax - mIntMin) + mIntMin;
		}
		return (int)(value + 0.5f);
	}

	//--------------------------------------------------------------------------
	private void cacheMinMax(float value) {
		
		// Only do data in the specified range
		if (value >= mFloatMin && value <= mFloatMax) {
			if (!mbInitedMinMaxCache) {
				mbInitedMinMaxCache = true;
				mLayerMin = value;
				mLayerMax = value;
			}
		
			if (value > mLayerMax) {
				mLayerMax = value;
			}
			else if (value < mLayerMin) {
				mLayerMin = value;
			}
		}
	}
	
	//--------------------------------------------------------------------------
	protected void processASC_Line(int y, String lineElementsArray[]) {
		
		for (int x = 0; x < lineElementsArray.length; x++) {
			
			float val = Float.parseFloat(lineElementsArray[x]);
			cacheMinMax(val);
			mIntData[y][x] = projectFloatToInt(val);
		}
	}

	//--------------------------------------------------------------------------
	protected void onLoadEnd() {
		
		Logger.info("Value range is: " + Float.toString(mLayerMin) + 
						" to " + Float.toString(mLayerMax));
	}
	
	//--------------------------------------------------------------------------
	protected int[][] query(JsonNode queryNode, int[][] workArray) {

		Logger.info("Running continuous query");

		String lessTest = queryNode.get("lessThanTest").getValueAsText();
		String gtrTest = queryNode.get("greaterThanTest").getValueAsText();
		
		JsonNode gtrValNode = queryNode.get("greaterThanValue");
		JsonNode lessValNode = queryNode.get("lessThanValue");
		
		int minVal = 0, maxVal = 0;
		boolean isGreaterThan = false, isGreaterThanEqual = false;
		boolean isLessThan = false, isLessThanEqual = false;
		
		if (gtrValNode != null) {
			if (gtrValNode.isNumber()) {
				isGreaterThan = (gtrTest.compareTo(">") == 0);
				isGreaterThanEqual = !isGreaterThan;
				minVal = projectFloatToInt(gtrValNode.getNumberValue().floatValue());
			}
		}
		if (lessValNode != null) {
			if (lessValNode.isNumber()) {
				isLessThan = (lessTest.compareTo("<") == 0);
				isLessThanEqual = !isLessThan;
				maxVal = projectFloatToInt(lessValNode.getNumberValue().floatValue());
			}
		}
		
		Logger.info("Min value:" + Integer.toString(minVal));
		Logger.info("Max value:" + Integer.toString(maxVal));
		int x,y;
		
		// Blugh, I count 8 permutations that we care about.		
		if (isGreaterThan) {
			if (isLessThan) {
				// >  <
				Logger.info("> <");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						workArray[y][x] &= 
							((mIntData[y][x] > minVal && mIntData[y][x] < maxVal) 
							? 1 : 0);
					}
				}
			}
			else if (isLessThanEqual) {
				// >  <=
				Logger.info("> <=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						workArray[y][x] &= 
							((mIntData[y][x] > minVal && mIntData[y][x] <= maxVal) 
							? 1 : 0);
					}
				}
			}
			else
			{ // >
				Logger.info(">");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						workArray[y][x] &= 
							(mIntData[y][x] > minVal 
							? 1 : 0);
					}
				}
			}
		}
		else if (isGreaterThanEqual) {
			if (isLessThan) {
				// >= <
				Logger.info(">= <");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						workArray[y][x] &= 
							((mIntData[y][x] >= minVal && mIntData[y][x] < maxVal) 
							? 1 : 0);
					}
				}
			}
			else if (isLessThanEqual) {
				// >=  <=
				Logger.info(">= <=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						workArray[y][x] &= 
							((mIntData[y][x] >= minVal && mIntData[y][x] <= maxVal) 
							? 1 : 0);
					}
				}
			}
			else
			{ // >=
				Logger.info(">=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						workArray[y][x] &= 
							(mIntData[y][x] >= minVal 
							? 1 : 0);
					}
				}
			}
		}
		else if (isLessThan) {
			// <
			Logger.info("<");
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					workArray[y][x] &= 
						(mIntData[y][x] < maxVal 
						? 1 : 0);
				}
			}
		}
		else if (isLessThanEqual) {
			// <=
			Logger.info("<=");
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					workArray[y][x] &= 
						(mIntData[y][x] <= maxVal 
						? 1 : 0);
				}
			}
		}

		Logger.info("Continuous query done!");
		return workArray;
	}
}

