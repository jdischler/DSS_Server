package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Layer_Float extends Layer_Base
{
	protected boolean mbInitedMinMaxCache;
	protected float	mMin, mMax;
	protected float[][] mFloatData;
	protected int mCountNoDataCells;
	
	//--------------------------------------------------------------------------
	public Layer_Float(String name) {

		this(name, false); // not temporary
	}
	
	//--------------------------------------------------------------------------
	public Layer_Float(String name, boolean temporary) {

		super(name, temporary);

		mbInitedMinMaxCache = false;
		mCountNoDataCells = 0;
	}
	
	// Min / Max
	//--------------------------------------------------------------------------
	public float getLayerMin() {
		
		return mMin;
	}
	public float getLayerMax() {
		
		return mMax;
	}
	
	//--------------------------------------------------------------------------
	public float[][] getFloatData() {
		
		return mFloatData;
	}

	//--------------------------------------------------------------------------
	protected void allocMemory() {
		
		Logger.info("  Allocating FLOAT work array");
		mFloatData = new float[mHeight][mWidth];
	}
	
	// Copies a file read bytebuffer into the internal native float array...
	//--------------------------------------------------------------------------
	protected void readCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			mFloatData[atY][x] = dataBuffer.getFloat();
		}
	}

	// Copies the native float data into a bytebuffer that is set up to recieve it (by the caller)
	//--------------------------------------------------------------------------
	protected void writeCopy(ByteBuffer dataBuffer, int width, int atY) {
		
		for (int x = 0; x < width; x++) {
			dataBuffer.putFloat(mFloatData[atY][x]);
		}
	}
	
	//--------------------------------------------------------------------------
	final private void cacheMinMax(float value) {
		
		if (value < -9999.1f || value > -9998.9f) { // FIXME
			if (!mbInitedMinMaxCache) {
				mbInitedMinMaxCache = true;
				mMin = value;
				mMax = value;
			}
		
			if (value > mMax) {
				mMax = value;
			}
			else if (value < mMin) {
				mMin = value;
			}
		}
		else {
			mCountNoDataCells++;
		}
	}
	
	//--------------------------------------------------------------------------
	protected void processASC_Line(int y, String lineElementsArray[]) {
		
		for (int x = 0; x < lineElementsArray.length; x++) {
			
			float val = Float.parseFloat(lineElementsArray[x]);
			mFloatData[y][x] = val;
		}
	}

	//--------------------------------------------------------------------------
	protected void onLoadEnd() {
		
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				cacheMinMax(mFloatData[y][x]);
			}
		}
		
		Logger.info("  Value range is: " + Float.toString(mMin) + 
						" to " + Float.toString(mMax));
		Logger.info("  Num Cells with NO_DATA: " + Integer.toString(mCountNoDataCells));
		Logger.info("  % Cells with NO_DATA: %" + Float.toString(mCountNoDataCells / (float)(mHeight * mWidth) * 100.0f));
	}
	
	//--------------------------------------------------------------------------
	protected JsonNode getParameterInternal(JsonNode clientRequest) throws Exception {

		// Set this as a default - call super first so subclass can override a return result
		//	for the same parameter request type. Unsure we need that functionality but...
		JsonNode ret = super.getParameterInternal(clientRequest);

		String type = clientRequest.get("type").textValue();
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

		detailedLog("Running continuous query");

		String lessTest = queryNode.get("lessThanTest").textValue();
		String gtrTest = queryNode.get("greaterThanTest").textValue();
		
		JsonNode gtrValNode = queryNode.get("greaterThanValue");
		JsonNode lessValNode = queryNode.get("lessThanValue");
		
		float minVal = 0, maxVal = 0;
		boolean isGreaterThan = false, isGreaterThanEqual = false;
		boolean isLessThan = false, isLessThanEqual = false;
		
		if (gtrValNode != null) {
			if (gtrValNode.isNumber()) {
				isGreaterThan = (gtrTest.compareTo(">") == 0);
				isGreaterThanEqual = !isGreaterThan;
				minVal = gtrValNode.numberValue().floatValue();
			}
		}
		if (lessValNode != null) {
			if (lessValNode.isNumber()) {
				isLessThan = (lessTest.compareTo("<") == 0);
				isLessThanEqual = !isLessThan;
				maxVal = lessValNode.numberValue().floatValue();
			}
		}
		
		detailedLog("Min value:" + Float.toString(minVal));
		detailedLog("Max value:" + Float.toString(maxVal));
		int x,y;
		
		// Blugh, I count 8 permutations that we care about. These are split out this way
		//	to remove the logic to determine the type of conditional testing to do
		//	from the inside of the loop. Though maybe that java compiler can detect that the
		//	condition could never change inside of the loop? Seems unlikely though..
		if (isGreaterThan) {
			if (isLessThan) {
				// >  <
				detailedLog("> <");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						selection.mRasterData[y][x] &= 
							((mFloatData[y][x] > minVal && mFloatData[y][x] < maxVal) 
							? 1 : 0);
					}
				}
			}
			else if (isLessThanEqual) {
				// >  <=
				detailedLog("> <=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						selection.mRasterData[y][x] &= 
							((mFloatData[y][x] > minVal && mFloatData[y][x] <= maxVal) 
							? 1 : 0);
					}
				}
			}
			else
			{ // >
				detailedLog(">");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						selection.mRasterData[y][x] &= 
							(mFloatData[y][x] > minVal 
							? 1 : 0);
					}
				}
			}
		}
		else if (isGreaterThanEqual) {
			if (isLessThan) {
				// >= <
				detailedLog(">= <");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						selection.mRasterData[y][x] &= 
							((mFloatData[y][x] >= minVal && mFloatData[y][x] < maxVal) 
							? 1 : 0);
					}
				}
			}
			else if (isLessThanEqual) {
				// >=  <=
				detailedLog(">= <=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						selection.mRasterData[y][x] &= 
							((mFloatData[y][x] >= minVal && mFloatData[y][x] <= maxVal) 
							? 1 : 0);
					}
				}
			}
			else
			{ // >=
				detailedLog(">=");
				for (y = 0; y < mHeight; y++) {
					for (x = 0; x < mWidth; x++) {
						selection.mRasterData[y][x] &= 
							(mFloatData[y][x] >= minVal 
							? 1 : 0);
					}
				}
			}
		}
		else if (isLessThan) {
			// <
			detailedLog("<");
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					selection.mRasterData[y][x] &= 
						(mFloatData[y][x] < maxVal 
						? 1 : 0);
				}
			}
		}
		else if (isLessThanEqual) {
			// <=
			detailedLog("<=");
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					selection.mRasterData[y][x] &= 
						(mFloatData[y][x] <= maxVal 
						? 1 : 0);
				}
			}
		}

		detailedLog("Continuous query done!");
		return selection;
	}
}

