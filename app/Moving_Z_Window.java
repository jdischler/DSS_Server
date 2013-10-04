package util;

// This class calculate proportation of Ag, forest and grass with the user specified rectangle buffer 
// Inputs are location of cell, window size
// Output proportion of Ag, Forest, Grass

// It uses a cool idea by Amin to zig zag across the cells, subtracting only the old cells
//	that fall out of the moving window...and adding in the new cells at the leading edge
//	of the moving window.

// Since the z window manages the irregular movement process, it returns the coordinates of
//	where it is at to the caller. The caller must only issue a call to advance to move the window
// Version 08/20/2013

// Simple usage example
/*
Moving_Z_Window zWin = new Moving_Z_Window(windowSize, rasterData, rasterWidth, rasterHeight);

boolean moreCells = true;
while (!moreCells) {
	
	Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();
	float agProp = zWin.getPropAg();
	someDataArray[point.y][point.x] = agProp;
	
	moreCells = zWin.advance();
}
*/

//------------------------------------------------------------------------------
public final class Moving_Z_Window
{
	// small helper class.....
	public final class Z_WindowPoint
	{
		public int mX, mY;
		
		public Z_WindowPoint(int x, int y) {
			mX = x;
			mY = y;
		}
	}
	
	//
	private int[][] mRasterData;
	private int mRasterWidth, mRasterHeight;
	private int mHalfWindowSize; // half the size, in cells
	
	// Define working variables
	private int mbInitialized; // first call to 
	private int mUpLeft_X, mUpLeft_Y;
	private int mLowRight_X, mLowRight_Y;
	
	private int mTotal; // total data cells in window. NoData cells are NOT counted
	
	private int mCountAg, mAgMask;
	private int mCountForest, mForestMask;
	private int mCountGrass, mGrassMask;
	
	private int mAt_X, mAt_Y;
	private boolean mbMovingLeft; // set when the window should be moving LEFT
	private boolean mbShouldAdvance_Y; // set when the next move should be a DOWN movement
	private Z_WindowPoint mPoint;
	
	// Define moving Z window - it assumes starting at (x,y) = (0,0) - but that could be changed if needed
	//--------------------------------------------------------------------------
	public Moving_Z_Window(int win_sz, int [][] rasterData, int raster_w, int raster_h) {
		
		mRasterData = rasterData;
		mRasterWidth = raster_w;
		mRasterHeight = raster_h;
		mHalfWindowSize = win_sz / 2;
		
		mAt_X = 0;
		mAt_Y = 0;
		mbMovingLeft = false; // window moves right first...
		mbShouldAdvance_Y = false; // only gets set once per line once an edge is hit
		
		//mAgMask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		//mForestMask = 1024; // 11
		//mGrassMask = 128 + 256; // 8 and 9

		// Mask
		Layer_Integer cdl = (Layer_Integer)Layer_Base.getLayer("cdl_2012"); 
		// Grass
		int Grass_Mask = cdl.convertStringsToMask("grass");
		int Alfalfa_Mask = cdl.convertStringsToMask("alfalfa");
		mGrassMask = Grass_Mask | Alfalfa_Mask;	
		// Forest
		mForestMask = cdl.convertStringsToMask("woodland");
		// Ag
		int Corn_Mask = cdl.convertStringsToMask("corn");
		int Soy_Mask = cdl.convertStringsToMask("soy");
		mAgMask = 1 + 2 + 4 + 8 + 16384 + 32768 + 131072 + 262144;
		// Total Mask
		int TotalMask = mAgMask | mGrassMask;
		
		mPoint = new Z_WindowPoint(mAt_X, mAt_Y);
		
		calcWindowBounds();
		initCounts();
	}
	
	//--------------------------------------------------------------------------
	private void calcWindowBounds() {
		
		updateBoundsMoving_X();
		updateBoundsMoving_Y();
	}
	
	//--------------------------------------------------------------------------
	private final void updateBoundsMoving_X() {
		
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
	private final void updateBoundsMoving_Y() {
		
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
	private void initCounts() {
		
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
	public final Z_WindowPoint getPoint() {
		
		mPoint.mX = mAt_X;
		mPoint.mY = mAt_Y;
		return mPoint;
	}
	
	// Each call to run advances one cell in the direction the Z-win is moving in...
	//	since it uses a somewhat irregular pattern to move, this function will
	// 	return a class with the X, Y coordinates for where the Z_Window is at...
	// Returns FALSE the Z_Window is finished processing all cells in the raster...
	//--------------------------------------------------------------------------
	public final boolean advance() {
		
		//-----------
		// SUBTRACT
		//-----------
		// subtracts OLD cells in the proper direction based on our movement
		if (!mbShouldAdvance_Y) { // moving left/right
			int windowReal_X;
			if (mbMovingLeft) {
				// we are moving LEFT, so we would subtract off the RIGHT edge if needed
				windowReal_X = mAt_X + mHalfWindowSize;
			}
			else {
				// we are moving RIGHT, so we need to subtract off the LEFT edge if needed
				windowReal_X = mAt_X - mHalfWindowSize;
			}
			// if the real (non clipped) X value is in a valid array location, subtract...
			if (windowReal_X >= 0 && windowReal_X < mRasterWidth) {
				for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
					int cellValue = mRasterData[y][windowReal_X];
					if (cellValue != 0) {
						mTotal--;
						
						// Calculate count of land cover in the given moving window
						if ((cellValue & mAgMask) > 0) {
							mCountAg--;	
						}
						else if ((cellValue & mGrassMask) > 0) {
							mCountGrass--;
						}
						else if ((cellValue & mForestMask) > 0) {
							mCountForest--;
						}
					}
				}
			}			
		}
		else {
			// we are moving DOWN - remove old cells off the TOP of the window if needed
			//	this is needed when the REAL top of the window is validly IN the raster array (vs. CLIPPED off)
			int windowRealTop_Y = mAt_Y - mHalfWindowSize;
			if (windowRealTop_Y >= 0) {
				for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
					int cellValue = mRasterData[windowRealTop_Y][x]; 
					if (cellValue != 0) {
						mTotal--;
						
						// Calculate count of land cover in the given moving window
						if ((cellValue & mAgMask) > 0) {
							mCountAg--;	
						}
						else if ((cellValue & mGrassMask) > 0) {
							mCountGrass--;
						}
						else if ((cellValue & mForestMask) > 0) {
							mCountForest--;
						}
					}
				}
			}
		}

		//------
		// ADD
		//------
		// adds NEW cells in the proper direction based on our movement
		if (!mbShouldAdvance_Y) {
			if (mbMovingLeft) {
				mAt_X--;
				if (mAt_X <= 0) { // check for need to switch direction...or move in current direction
					mbShouldAdvance_Y = true;
				}
			}
			else {
				mAt_X++;
				if (mAt_X >= mRasterWidth - 1) { // check for need to switch direction
					mbShouldAdvance_Y = true;
				}
			}
			
			int windowReal_X;
			if (mbMovingLeft) {
				// we are moving LEFT, so we would ADD on the LEFT edge if needed
				windowReal_X = mAt_X - mHalfWindowSize;
			}
			else {
				// we are moving RIGHT, so we need to add on the RIGHT edge if needed
				windowReal_X = mAt_X + mHalfWindowSize;
			}
			// if the real (non clipped) X value is in a valid array location, subtract...
			if (windowReal_X >= 0 && windowReal_X < mRasterWidth) {
				for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
					int cellValue = mRasterData[y][windowReal_X];
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
			updateBoundsMoving_X();
		}
		else {
			mbShouldAdvance_Y = false; // we only ever move down once per line, turn off move down flag
			mbMovingLeft = !mbMovingLeft; // also change direction for the next advance call
		
			mAt_Y++;
			if (mAt_Y >= mRasterHeight) {
				// process is done...signal back to caller that there is no valid point
				return false;
			}
			
			// we are moving DOWN - ADD new cells on the BOTTOM of the window if needed
			//	this is needed when the REAL bottom of the window is validly IN the raster array (vs. CLIPPED off)
			int windowRealBottom_Y = mAt_Y + mHalfWindowSize;
			if (windowRealBottom_Y < mRasterHeight) {
				for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
					int cellValue = mRasterData[windowRealBottom_Y][x];
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
			updateBoundsMoving_Y();
		}
		
		return true;
	}
	
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

