package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;


// example ("slope", 0.0, 90.0, 0, 360); .. remap a slope of 0.0-90.0 to 0-360 to better utilize data range
// or ("river" )...which doesn't try to maximize value range, just rounds/clamps distance to river to int
//------------------------------------------------------------------------------
public class Layer_Continuous extends Layer_Base
{
	// Data is currently stored in 32 bit integer - instead of clamping/rounding float
	//	float to int, I set things up to allow a behind-the-scenes reprojection of
	//	the value ranges to better use the integer data range
	boolean mbProjectValues;
	
	// for range/value reprojection
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

	// Range/Value round and clamp constructor variant, ie, values are not reprojected
	//--------------------------------------------------------------------------
	public Layer_Continuous(String name) {

		super(name);

		mbProjectValues = false;
		mFloatMin = -9000; // FIXME: blugh, so lame
		mFloatMax = 999999;// FIXME: blugh, so lame
		mbInitedMinMaxCache = false;
	}
	
	// Range/Value reprojection constructor variant
	//--------------------------------------------------------------------------
	public Layer_Continuous(String name, float fMin, float fMax, int iMin, int iMax) {

		super(name);

		mbProjectValues = true;
		mFloatMin = fMin;
		mFloatMax = fMax;
		mIntMin = iMin;
		mIntMax = iMax;
		mbInitedMinMaxCache = false;
	}
	
	//--------------------------------------------------------------------------
	private int conditionalReprojectFloatToInt(float value) {
		
		if (mbProjectValues) {
			return projectFloatToInt(value);
		}
		else {
			return (int)(value + 0.5); // simple round + clamp
		}
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
		
		if (mbProjectValues) {
			for (int x = 0; x < lineElementsArray.length; x++) {
				
				float val = Float.parseFloat(lineElementsArray[x]);
				cacheMinMax(val);
				mIntData[y][x] = conditionalReprojectFloatToInt(val);
			}
		}
		else {
			for (int x = 0; x < lineElementsArray.length; x++) {
				
				int val = Integer.parseInt(lineElementsArray[x]);
				cacheMinMax((float)val);
				mIntData[y][x] = val;
			}
		}
	}

	//--------------------------------------------------------------------------
	protected void onLoadEnd() {
		
		Logger.info("  Value range is: " + Float.toString(mLayerMin) + 
						" to " + Float.toString(mLayerMax));
	}
	
	//--------------------------------------------------------------------------
	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {

		// Set this as a default - call super first so subclass can override a return result
		//	for the same parameter request type. Unsure we need that functionality but...
		JsonNode ret = super.getParameterInternal(clientRequest);

		String type = clientRequest.get("type").getTextValue();
		if (type.equals("layerRange")) {
			ObjectNode layerRangeObj = JsonNodeFactory.instance.objectNode();
			layerRangeObj.put("layerMin", getLayerMin());
			layerRangeObj.put("layerMax", getLayerMax());
			ret = layerRangeObj;
		}
		
		return ret;
	}
	
	//--------------------------------------------------------------------------
	protected Selection query(JsonNode queryNode, Selection selection) {

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
				minVal = conditionalReprojectFloatToInt(gtrValNode.getNumberValue().floatValue());
			}
		}
		if (lessValNode != null) {
			if (lessValNode.isNumber()) {
				isLessThan = (lessTest.compareTo("<") == 0);
				isLessThanEqual = !isLessThan;
				maxVal = conditionalReprojectFloatToInt(lessValNode.getNumberValue().floatValue());
			}
		}
		
		Logger.info("Min value:" + Integer.toString(minVal));
		Logger.info("Max value:" + Integer.toString(maxVal));
		int x,y;
		
		// Blugh, I count 8 permutations that we care about. These are split out this way
		//	to remove the logic to determine the type of conditional testing to do
		//	from the inside of the loop. Though maybe that java compiler can detect that the
		//	condition could never change inside of the loop? Seems unlikely though..
		if (isGreaterThan) {
			if (isLessThan) {
				// >  <
				Logger.info("> <");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						selection.mSelection[y][x] &= 
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
						selection.mSelection[y][x] &= 
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
						selection.mSelection[y][x] &= 
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
						selection.mSelection[y][x] &= 
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
						selection.mSelection[y][x] &= 
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
						selection.mSelection[y][x] &= 
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
					selection.mSelection[y][x] &= 
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
					selection.mSelection[y][x] &= 
						(mIntData[y][x] <= maxVal 
						? 1 : 0);
				}
			}
		}

		Logger.info("Continuous query done!");
		return selection;
	}
}

