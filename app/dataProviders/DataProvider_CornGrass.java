package util;

import play.*;

//------------------------------------------------------------------------------
public class DataProvider_CornGrass extends DataProvider {

	// Inputs
	private Asc_Reader mCornProdFile, mGrassProdFile;
	
	// convenience constants
	public static final int CORN_IDX = 0, GRASS_IDX = 1;

	//--------------------------------------------------------------------------
	public DataProvider.EDataProvider getProviderType() {
		
		return DataProvider.EDataProvider.EDP_CornGrass_Provider;
	}
	
	//--------------------------------------------------------------------------
	public void init() {
		
		mCornProdFile = new Asc_Reader("corn_production", null);
		mGrassProdFile = new Asc_Reader("grass_production", null);
		
		mProcessedLine = new float[2][];
		mProcessedLine[CORN_IDX] = new float[mCornProdFile.getWidth()];
		mProcessedLine[GRASS_IDX] = new float[mCornProdFile.getWidth()];
	};
	
	//--------------------------------------------------------------------------
	public float[][] getLine(int[][] rotationData) {
		
		String [] cornProd = null, grassProd = null;
		
		try {
//			cornProd = mCornProdFile.getSplitLine();
//			grassProd = mGrassProdFile.getSplitLine();
		}
		catch(Exception e) {
			Logger.error(e.toString());
		}
		
		int width = mCornProdFile.getWidth();
		for (int x = 0; x < width; x++ ) {
			mProcessedLine[CORN_IDX][x] = (float)x;//Float.parseFloat(cornProd[x]); 
			mProcessedLine[GRASS_IDX][x] = (float)x;//Float.parseFloat(grassProd[x]);
		}
	
		return mProcessedLine;
	};
	
	//--------------------------------------------------------------------------
	public void done() {

		mCornProdFile.close();
		mGrassProdFile.close();
	};
}

