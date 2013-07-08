package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Scenario 
{
	public class Sel {
		public int[][] mSelection;
		public int mHeight, mWidth;
		
		//----------------------------------------------------------------------
		public boolean isSelected(int atX, int atY) {
			return (mSelection[atY][atX] > 0);
		}
		
		// Takes the selected pixels in otherSel and adds them into this selection
		//----------------------------------------------------------------------
		public void combineSelection(Sel otherSel) {
			int x, y;
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					// flip the 1st bit
					mSelection[y][x] |= otherSel.mSelection[y][x];
				}
			}
		}
		
		// Takes the selected pixels in otherSel and removes them from this selection
		//----------------------------------------------------------------------
		public void removeSelection(Sel otherSel) {
			int x, y;
			for (y = 0; y < mHeight; y++) {
				for (x = 0; x < mWidth; x++) {
					// Flip/invert the first bit...
					mSelection[y][x] &= (otherSel.mSelection[y][x] ^ 1);
//					mSelection[y][x] &= (!otherSel.mSelection[y][x]);
				}
			}
		}
		
		
		//----------------------------------------------------------------------
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
	
	private Sel mSelection; 
	private String mOutputDir;
	private JsonNode mConfiguration;
	private int[][] mNewRotation; // copy of Rotation layer, but selection transformed
	
	//--------------------------------------------------------------------------
	public Scenario(JsonNode configuration, String outputDir) {
		
		mConfiguration = configuration;
		mOutputDir = outputDir;
	}
	
	//--------------------------------------------------------------------------
	public void run() {
		
		// FIXME: get from client
		int newCrop = 1;
		
		mNewRotation = duplicateRotation();
		transformRotation(mNewRotation, newCrop);
		
		Models model = new Models();
//		JsonNode SendBack = model.modeloutcome(request().body().asJson(), mSelection, mOutputDir);
//		return ok(SendBack);
	}
	
	//--------------------------------------------------------------------------
	private int[][] duplicateRotation() {
		
		int[][] rotation = Layer_Base.getLayer("Rotation").getIntData();
	 	int[][] newRotation = new int[mSelection.mHeight][mSelection.mWidth];
		
		int x, y;
		for (y = 0; y < mSelection.mHeight; y++) {
			for (x = 0; x < mSelection.mWidth; x++) {
				newRotation[y][x] = rotation[y][x];
			}
		}
		
		return newRotation;
	}

	//--------------------------------------------------------------------------
	private void transformRotation(int[][] rotationToTransform, int newCrop) {
	
/*		Query query = new Query();
		try {
			mSelection = query.execute(mConfiguration);
		} catch (Exception e) {
			Logger.info(e.toString());
		}
		
		int x, y;
		for (y = 0; y < mSelection.mHeight; y++) {
			for (x = 0; x < mSelection.mWidth; x++) {
				if (mSelection.isSelected(atX, atY)) {
					rotationToTransform[y][x] = newCrop;			
				}
			}
		}*/
	}
}

