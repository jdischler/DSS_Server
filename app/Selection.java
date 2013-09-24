package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Selection
{	
	// Set up to run the query...allocate memory...
	public byte[][] mSelection;
	public int mHeight, mWidth;
	
	// Constructor...
	//--------------------------------------------------------------------------
	public Selection(int width, int height) {
		
		mHeight = height;
		mWidth = width;
		mSelection = new byte[mHeight][mWidth];
		int x, y;
		// ...and initialize everything to 1 to prep for & (and) logic
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				mSelection[y][x] = 1;
			}
		}
	}

	//--------------------------------------------------------------------------
	public final int getWidth() {
		return mWidth;
	}
	public final int getHeight() {
		return mHeight;
	}
	
	//--------------------------------------------------------------------------
	public boolean isSelected(int atX, int atY) {
		
		return (mSelection[atY][atX] > 0);
	}

	//--------------------------------------------------------------------------
	public int countSelectedPixels() {
		
		int x, y, count = 0;
		
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				// NOTE: relies on mSelection containing 1's and 0's...
				//count += mSelection[y][x];
				// Otherwise do something like...
				count += (mSelection[y][x] > 0 ? 1 : 0);
			}
		}
		return count;
	}
	
	// Takes the selected pixels in otherSel and adds them into this selection
	//--------------------------------------------------------------------------
	public void combineSelection(Selection otherSel) {
		
		int x, y;
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				mSelection[y][x] |= otherSel.mSelection[y][x];
			}
		}
	}
	
	// Takes the selected pixels in otherSel and removes them from this selection
	//--------------------------------------------------------------------------
	public void removeSelection(Selection otherSel) {
		
		int x, y;
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				// Flip/invert the first bit... and AND that back in to remove
				mSelection[y][x] &= (otherSel.mSelection[y][x] ^ 1);
			}
		}
	}
	
	// Anything that is selected becomes NOT selected. Anything NOT selected
	//	becomes selected
	//--------------------------------------------------------------------------
	public void invertSelection() {
		
		int x, y;
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				// flip the 1st bit
				mSelection[y][x] ^= 1;
			}
		}
	}
}

