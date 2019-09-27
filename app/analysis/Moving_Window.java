package util;

import util.Layer_Integer;

// This class calculate proportation of Ag, forest and grass with the user specified rectangle buffer 
// Inputs are location of cell, window size
// Output proportion of Ag, Forest, Grass

// It uses a cool idea by Amin to zig zag across the cells, subtracting only the old cells
//	that fall out of the moving window...and adding in the new cells at the leading edge
//	of the moving window.

// Since the n window manages the irregular movement process, it returns the coordinates of
//	where it is at to the caller. The caller must only issue a call to advance to move the window
// Version 08/20/2013

// Simple usage example
/*
Moving_N_Window nWin = new Moving_N_Window(windowSize, rasterData, rasterWidth, rasterHeight);

boolean moreCells = true;
while (!moreCells) {
	
	Moving_N_Window.N_WindowPoint point = nWin.getPoint();
	float agProp = nWin.getPropAg();
	someDataArray[point.y][point.x] = agProp;
	
	moreCells = nWin.advance();
}
*/

//------------------------------------------------------------------------------
public abstract class Moving_Window
{
	// small helper class.....
	public final class WindowPoint
	{
		public int mX, mY;
		
		public WindowPoint(int x, int y) {
			mX = x;
			mY = y;
		}
	}
	
	//
	protected int[][] mRasterData;
	protected int mRasterWidth, mRasterHeight;
	protected int mHalfWindowSize; // half the size, in cells
	
	// Define working variables
	protected int mUpLeft_X, mUpLeft_Y;
	protected int mLowRight_X, mLowRight_Y;
	
	protected int mTotal; // total data cells in window. NoData cells are NOT counted
	
	protected int mCountAg, mAgMask;
	protected int mCountForest, mForestMask;
	protected int mCountGrass, mGrassMask;
	
	protected int mAt_X, mAt_Y;
	protected WindowPoint mPoint;
	
	// Define moving window - it assumes starting at (x,y) = (0,0) - but that could be changed if needed
	//--------------------------------------------------------------------------
	public Moving_Window(int win_sz, int [][] rasterData, int raster_w, int raster_h) {
		
		mRasterData = rasterData;
		mRasterWidth = raster_w;
		mRasterHeight = raster_h;
		mHalfWindowSize = win_sz / 2;
		
		mAt_X = 0;
		mAt_Y = 0;
		
		// 1, 2, 3, 15, 16, 18, 19, 21
		mAgMask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		// 4, 7
		mForestMask = 1024; // 11
		// 6, 17
		mGrassMask = 128 + 256; // 8 and 9

		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012");
		
		mGrassMask = cdl.convertStringsToMask("grass") | cdl.convertStringsToMask("alfalfa");;	
		mForestMask = cdl.convertStringsToMask("woodland") | cdl.convertStringsToMask("Tree_Crop");
		mAgMask = cdl.convertStringsToMask("corn") | cdl.convertStringsToMask("soy") 
				| cdl.convertStringsToMask("grains") | cdl.convertStringsToMask("veggies") 
				| cdl.convertStringsToMask("other_crop") | cdl.convertStringsToMask("corn_grain") 
				| cdl.convertStringsToMask("soy_grain");
		
		mPoint = new WindowPoint(mAt_X, mAt_Y);
		
		calcWindowBounds();
		initCounts();
	}
	
	//--------------------------------------------------------------------------
	protected void calcWindowBounds() {
		
		updateBoundsMoving_X();
		updateBoundsMoving_Y();
	}
	
	//--------------------------------------------------------------------------
	protected final void updateBoundsMoving_X() {
		
		mUpLeft_X = mAt_X - mHalfWindowSize;
		mLowRight_X = mAt_X + mHalfWindowSize;
		
		if (mUpLeft_X < 0) {
			mUpLeft_X = 0;
		}
		if (mLowRight_X > mRasterWidth - 1) {
			mLowRight_X = mRasterWidth - 1;
		}
	}

	//--------------------------------------------------------------------------
	protected final void updateBoundsMoving_Y() {
		
		mUpLeft_Y = mAt_Y - mHalfWindowSize;
		mLowRight_Y = mAt_Y + mHalfWindowSize;
		
		if (mUpLeft_Y < 0) {
			mUpLeft_Y = 0;
		}
		if (mLowRight_Y > mRasterHeight - 1) {
			mLowRight_Y = mRasterHeight - 1;
		}
	}
	
	// Called internally off the constructor
	//--------------------------------------------------------------------------
	protected void initCounts() {
		
		mTotal = 0;	
		
		for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
			for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
				int cellValue = mRasterData[y][x]; 
				if (cellValue != 0) {
					mTotal++;
					
					// Calculate count of land cover in the given moving window
					if ((cellValue & mAgMask) > 0) {
						mCountAg++;	
					}
					else if ((cellValue & mGrassMask) > 0) {
						mCountGrass++;
					}
					else if ((cellValue & mForestMask) > 0) {
						mCountForest++;
					}
				}
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public final WindowPoint getPoint() {
		
		mPoint.mX = mAt_X;
		mPoint.mY = mAt_Y;
		return mPoint;
	}
	
	//--------------------------------------------------------------------------
	public final int getWindowCenterValue() {
		return mRasterData[mAt_Y][mAt_X];
		
	}
	
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public abstract boolean advance();

	// If total cells is zero, there is no reasonable proportion we can calculate,
	//	we should check this before trying to get proportions. If false, should probably
	//	put NoData in resulting cell...
	//--------------------------------------------------------------------------
	public final boolean canGetProportions() {
		return (mTotal > 0);
	}	
	
	//--------------------------------------------------------------------------
	public final float getProportionAg() {
		return (float)mCountAg / mTotal;
	}
	
	//--------------------------------------------------------------------------
	public final float getProportionForest() {
		return (float)mCountForest / mTotal;
	}

	//--------------------------------------------------------------------------
	public final float getProportionGrass() {
		return (float)mCountGrass / mTotal;
	}
}

