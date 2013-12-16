package util;

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
public final class Moving_N_Window
{
	// small helper class.....
	public final class N_WindowPoint
	{
		public int mX, mY;
		
		public N_WindowPoint(int x, int y) {
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
	private boolean mbMovingUp; // set when the window should be moving UP
	private boolean mbShouldAdvance_X; // set when the next move should be a RIGHT movement
	private N_WindowPoint mPoint;
	
	// Define moving N window - it assumes starting at (x,y) = (0,0) - but that could be changed if needed
	//--------------------------------------------------------------------------
	public Moving_N_Window(int win_sz, int [][] rasterData, int raster_w, int raster_h) {
		
		mRasterData = rasterData;
		mRasterWidth = raster_w;
		mRasterHeight = raster_h;
		mHalfWindowSize = win_sz / 2;
		
		mAt_X = 0;
		mAt_Y = 0;
		mbMovingUp = false; // window moves down first...
		mbShouldAdvance_X = false; // only gets set once per line once an edge is hit
		
		mAgMask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
		mForestMask = 1024; // 11
		mGrassMask = 128 + 256; // 8 and 9
	
		mPoint = new N_WindowPoint(mAt_X, mAt_Y);
		
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
	public final N_WindowPoint getPoint() {
		
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
		if (!mbShouldAdvance_X) { // moving up/down
			int windowReal_Y;
			if (mbMovingUp) {
				// we are moving UP, so we would subtract off the BOTTOM edge if needed
				windowReal_Y = mAt_Y + mHalfWindowSize;
			}
			else {
				// we are moving RIGHT, so we need to subtract off the LEFT edge if needed
				windowReal_Y = mAt_Y - mHalfWindowSize;
			}
			// if the real (non clipped) Y value is in a valid array location, subtract...
			if (windowReal_Y >= 0 && windowReal_Y < mRasterHeight) {
				for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
					int cellValue = mRasterData[windowReal_Y][x];
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
			// we are moving RIGHT - remove old cells off the LEFT of the window if needed
			//	this is needed when the REAL left of the window is validly IN the raster array (vs. CLIPPED off)
			int windowRealTop_X = mAt_X - mHalfWindowSize;
			if (windowRealTop_X >= 0) {
				for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
					int cellValue = mRasterData[y][windowRealTop_X]; 
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
		if (!mbShouldAdvance_X) {
			if (mbMovingUp) {
				mAt_Y--;
				if (mAt_Y <= 0) { // check for need to switch direction...or move in current direction
					mbShouldAdvance_X = true;
				}
			}
			else {
				mAt_Y++;
				if (mAt_Y >= mRasterHeight - 1) { // check for need to switch direction
					mbShouldAdvance_X = true;
				}
			}
			
			int windowReal_Y;
			if (mbMovingUp) {
				// we are moving UP, so we would ADD on the DOWN edge if needed
				windowReal_Y = mAt_Y - mHalfWindowSize;
			}
			else {
				// we are moving DOWN, so we need to add on the UP edge if needed
				windowReal_Y = mAt_Y + mHalfWindowSize;
			}
			// if the real (non clipped) Y value is in a valid array location, subtract...
			if (windowReal_Y >= 0 && windowReal_Y < mRasterHeight) {
				for (int x = mUpLeft_X; x <= mLowRight_X; x++) {
					int cellValue = mRasterData[windowReal_Y][x];
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
		else {
			mbShouldAdvance_X = false; // we only ever move down once per line, turn off move down flag
			mbMovingUp = !mbMovingUp; // also change direction for the next advance call
		
			mAt_X++;
			if (mAt_X >= mRasterWidth) {
				// process is done...signal back to caller that there is no valid point
				return false;
			}
			
			// we are moving DOWN - ADD new cells on the BOTTOM of the window if needed
			//	this is needed when the REAL bottom of the window is validly IN the raster array (vs. CLIPPED off)
			int windowRealBottom_X = mAt_X + mHalfWindowSize;
			if (windowRealBottom_X < mRasterWidth) {
//				for (int y = mUpLeft_Y; y <= mLowRight_Y; y++) {
				// NOTE: Going in reverse order seems to be moderately more cache friendly.
				for (int y = mLowRight_Y; y >= mUpLeft_Y; y--) {
					int cellValue = mRasterData[y][windowRealBottom_X];
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
		
		return true;
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

