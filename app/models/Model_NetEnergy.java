package util;

import play.*;
import java.util.*;
import java.io.*;

import java.nio.*;
import java.nio.channels.*;

// Net_Energy Calculation (Mega Jul)
//------------------------------------------------------------------------------
public class Model_NetEnergy
{
	private FileOutputStream mFileOutStream;
	private WritableByteChannel mChannel;
	private ByteBuffer mByteBuffer;
	
	private int mCornMask, mGrassMask;
	private int mWidth, mHeight;
	private String mNoDataString;
	
	//--------------------------------------------------------------------------
	public Model_NetEnergy() {
		
		mCornMask = Layer_Integer.convertIndicesToMask(1);
		mGrassMask = Layer_Integer.convertIndicesToMask(8, 9);
		
		mWidth = Layer_Base.getLayer("Rotation").getWidth();
		mHeight = Layer_Base.getLayer("Rotation").getHeight();

		int NO_DATA = -9999;
		mNoDataString = Integer.toString(NO_DATA);
	}
	
/* 	// Only used for models that do not require some sort of intermediate or pre-processed
	// results. I.e., this function should only be used if the model calculation is
	//	truly stand-alone.
	public void run(int[][] rotationData) {
		
	}
*/
	//--------------------------------------------------------------------------
	public void initForLineProcessing(String folderOut) {
		
		String path = "./layerData/" + folderOut + "/" + "net_energy.dss";
		Logger.info("Writing Binary: " + path);
		File output = new File(path);

		try {
			mFileOutStream = new FileOutputStream(output);
			mChannel = mFileOutStream.getChannel();
			ByteBuffer buf = ByteBuffer.allocateDirect(3 * 4); // FIXME: size of int * header?
			
			buf.putInt(mWidth);
			buf.putInt(mHeight);
			buf.putFloat(-9999.0f);
			buf.flip();
			mChannel.write(buf);
		}
		catch(Exception e) {
			Logger.info(e.toString());
		}
		
		mByteBuffer = ByteBuffer.allocateDirect(mWidth * 4); // FIXME: size of int?
	}
	
	//--------------------------------------------------------------------------
	public void processLine(int lineY, int width, int[][] rotationData, float[][] providedData)
	{
		mByteBuffer.clear();
		
		for (int x = 0; x < width; x++) {				
			if (rotationData[lineY][x] > 0) {
				float ne = 0;

				if ((rotationData[lineY][x] & mCornMask) > 0) {
					float cp = providedData[DataProvider_CornGrass.CORN_IDX][x];
					ne = (cp * 0.5f * 0.4f * 1000.0f * 21.20f + cp * 0.25f * 0.38f * 1000.0f * 21.20f) 
						- (18.92f / 10000.0f * 900.0f + 7.41f / 10000.0f * 900.0f + 15.25f * cp * 0.5f * 0.4f * 1000.0f 
							+ 1.71f * cp * 0.25f * 0.38f * 1000.0f);
				}
				else if ((rotationData[lineY][x] & mGrassMask) > 0) {
					float gp = providedData[DataProvider_CornGrass.GRASS_IDX][x];
					ne = (gp * 0.38f * 1000.0f * 21.20f) 
						- (7.41f / 10000.0f * 900.0f + 1.71f * gp * 0.38f * 1000.0f);
				}

				mByteBuffer.putFloat(ne);
			}
			else {
				// Emit NO DATA value
				mByteBuffer.putFloat(-9999.0f);
			}
		}
		mByteBuffer.flip();
		try {
			mChannel.write(mByteBuffer);
		}
		catch(Exception e) {
			Logger.info(e.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	public void finishLineProcessing() {

		try {
			mChannel.close();
			mFileOutStream.close();
		}
		catch(Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info("Finished writing NetEnergy file");
	}
}

