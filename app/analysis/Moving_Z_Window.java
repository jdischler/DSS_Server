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
public final class Moving_Z_Window extends Moving_Window
{
	private boolean mbMovingLeft; // set when the window should be moving LEFT
	private boolean mbShouldAdvance_Y; // set when the next move should be a DOWN movement
	
	// Define moving Z window - it assumes starting at (x,y) = (0,0) - but that could be changed if needed
	//--------------------------------------------------------------------------
	public Moving_Z_Window(int win_sz, int [][] rasterData, int raster_w, int raster_h) {
		
		super(win_sz, rasterData, raster_w, raster_h);

		mbMovingLeft = false; // window moves right first...
		mbShouldAdvance_Y = false; // only gets set once per line once an edge is hit
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
}
