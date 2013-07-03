package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
	
public class Selection
{	
	// Set up to run the query...allocate memory...
	public int[][] mSelection;
	public int mHeight, mWidth;
	
	public Selection(int width, int height)
	{
		mHeight = height;
		mWidth = width;
		mSelection = new int[mHeight][mWidth];
		int x, y;
		// ...and initialize everything to 1 to prep for & (and) logic
		for (y = 0; y < mHeight; y++) {
			for (x = 0; x < mWidth; x++) {
				mSelection[y][x] = 1;
			}
		}
	}
}

