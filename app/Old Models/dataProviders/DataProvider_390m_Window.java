package util;

import play.*;

//------------------------------------------------------------------------------
public class DataProvider_390m_Window extends DataProvider {

	private int mAgMask, mForestMask, mGrassMask;
	
	// window 
	private int mULX, mULY, mLRX, mLRY;
	private static int mWindowSize = 13; // Count of Raster Cells
	private int mWidth, mHeight;
	private int mY;
	
	// convenience constants
	public static final int PROP_AG = 0, PROP_FOREST = 1, PROP_GRASS = 2;
	
	//--------------------------------------------------------------------------
	public DataProvider.EDataProvider getProviderType() {
		
		return DataProvider.EDataProvider.EDP_390m_Window_Provider;
	}
	
	//--------------------------------------------------------------------------
	public void init() {
		
		mWidth = Layer_Base.getLayer("Rotation").getWidth();
		mHeight = Layer_Base.getLayer("Rotation").getHeight();
		
		mAgMask = Layer_Integer.convertIndicesToMask(1, 2, 3, 4, 5, 6, 7, 10);
		mForestMask = Layer_Integer.convertIndicesToMask(11);
		mGrassMask = Layer_Integer.convertIndicesToMask(8, 9);
		
		mProcessedLine = new float[3][];
		mProcessedLine[PROP_AG] = new float[mWidth];
		mProcessedLine[PROP_FOREST] = new float[mWidth];
		mProcessedLine[PROP_GRASS] = new float[mWidth];
	};

	// Position window and clamp coordinates to fit inside of the raster grid
	//--------------------------------------------------------------------------
	private void calculateWindowBounds(int toX, int toY) {
		
		mULX = toX - mWindowSize / 2;
		mULY = toY - mWindowSize / 2;
		mLRX = toX + mWindowSize / 2;
		mLRY = toY + mWindowSize / 2;
		
		if (mULX < 0) { // Up Left X
			mULX = 0;
		}
		if (mULY < 0) { // Up Left Y
			mULY = 0;
		}
		if (mLRX > mWidth - 1) { // Low Right X
			mLRX = mWidth - 1;
		}
		if (mLRY > mHeight - 1) { // Low Right Y
			mLRY = mHeight - 1;
		}
	}
	
	//--------------------------------------------------------------------------
	public float[][] getLine(int[][] rotationData) {
		
		for (int x = 0; x < mWidth; x++ ) {
			
			calculateWindowBounds(x, mY);
				
			int total = 0;
			int countAg = 0, countForest = 0, countGrass = 0;
			float propAg = 0, propForest = 0, propGrass = 0;
			
			// wx (window x) to Width and wy (window y) to Height
			for (int wy = mULY; wy <= mLRY; wy++) 
			{
				for (int wx = mULX; wx <= mLRX; wx++) 
				{
					int rotation = rotationData[wy][wx];
					if (rotation > 0) // value of zero would be a NoData value, which we don't want
					{
						total++;
						if ((rotation & mAgMask) > 0) {
							countAg++;	
						}
						else if ((rotation & mForestMask) > 0) {
							countForest++;
						}
						else if ((rotation & mGrassMask) > 0) {
							countGrass++;
						}
					}
				}
			}
			
			if (total <= 0) {
				mProcessedLine[PROP_AG][x] = (float)countAg / total;
				mProcessedLine[PROP_FOREST][x] = (float)countForest / total;
				mProcessedLine[PROP_GRASS][x] = (float)countGrass / total;
			}
			else {
				mProcessedLine[PROP_AG][x] = 0;
				mProcessedLine[PROP_FOREST][x] = 0;
				mProcessedLine[PROP_GRASS][x] = 0;
			}
		}
		
		// Move down to the next line
		mY++;
		
		return mProcessedLine;
	};
}
	
