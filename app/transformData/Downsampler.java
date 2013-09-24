package util;

import play.*;
import java.util.*;

// TODO: consider some way to capture the Min and Max value and return that??
//------------------------------------------------------------------------------
public class Downsampler
{
	private float[][] mData;
	private int mWidth, mHeight;
	
	private float[][] mResampledData;
	private int mTargetWidth, mTargetHeight;
	
	// Init with original data array and the sizes of that array. 
	//--------------------------------------------------------------------------
	Downsampler(float[][] data, int width, int height) {
		
		mData = data;
		mWidth = width;
		mHeight = height;
	}
	
	// Returns a transformed data array of the requested size. Sampling is done via averaging
	//--------------------------------------------------------------------------
	float [][] generateAveraged(int newWidth, int newHeight) {
	
		mTargetWidth = newWidth;
		mTargetHeight = newHeight;
		mResampledData = new float[newHeight][newWidth];
		
		float widthFactor = mWidth / mTargetWidth;
		float heightFactor = mHeight / mTargetHeight;
		
		int upLeftX = 0, upLeftY = 0;
		
		for (int y = 0; y < mTargetHeight - 1; y++) {
			int lowRightY = Math.round((y + 1) * heightFactor);
			
			for (int x = 0; x < mTargetWidth - 1; x++) {
				int lowRightX = Math.round((x + 1) * widthFactor);
				
				// Calculate that ave value and stuff it into mResampledData[y][x]
				float sum = 0;
				int ct = 0;
				for (int yy = upLeftY; yy <= lowRightY; yy++) {
					for (int xx = upLeftX; xx <= lowRightX; xx++) {
						if (!Float.isNaN(mData[yy][xx])) {
							sum += mData[yy][xx];
							ct++;
						}
					}
				}
				
				float ave = 0;
				if (ct > 0) {
					ave = sum / ct;
				}
				mResampledData[y][x] = ave;
				upLeftX = lowRightX;
			}
			upLeftY = lowRightY;
		}
		
		return mResampledData;
	}
	
	// Returns a transformed data array of the requested size. Sampling is done via taking MAX
	//	absolute value...
	//--------------------------------------------------------------------------
	float [][] generateMax(int newWidth, int newHeight) {
	
		mTargetWidth = newWidth;
		mTargetHeight = newHeight;
		mResampledData = new float[newHeight][newWidth];
		
		float widthFactor = mWidth / mTargetWidth;
		float heightFactor = mHeight / mTargetHeight;
		
		int upLeftX = 0, upLeftY = 0;
		
		for (int y = 0; y < mTargetHeight - 1; y++) {
			int lowRightY = Math.round((y + 1) * heightFactor);
			
			for (int x = 0; x < mTargetWidth - 1; x++) {
				int lowRightX = Math.round((x + 1) * widthFactor);
				
				// Find the max value and stuff it into mResampledData[y][x]
				float max = 0;
				for (int yy = upLeftY; yy <= lowRightY; yy++) {
					for (int xx = upLeftX; xx <= lowRightX; xx++) {
						float result = mData[yy][xx];
						if (!Float.isNaN(result)) {
							// test absolute value so we capture the largest magnitude, be it pos or neg
							if (Math.abs(result) > Math.abs(max)) {
								// but save the original max result, vs. absolute val.
								max = result;
							}
						}
					}
				}
				
				mResampledData[y][x] = max;
				upLeftX = lowRightX;
			}
			upLeftY = lowRightY;
		}
		
		return mResampledData;
	}
}

