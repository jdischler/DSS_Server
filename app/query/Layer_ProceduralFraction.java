package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class Layer_ProceduralFraction extends Layer_Base
{
	private static final String NAME = "proceduralFraction";

	// TODO: adding a procedural layer type kind of changes the pattern and needs here...
	//	Consider refactoring the base such that there is a Layer_Data type class that
	//	is the base for disk related layers?
	//--------------------------------------------------------------------------
	protected void allocMemory() {}
	protected void onLoadEnd() {
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| Procedural Layer Added: " + mName);
		Logger.info("+-------------------------------------------------------+");
		Logger.info("");
	}
	protected void processASC_Line(int y, String lineElementsArray[]) {}
	protected void readCopy(ByteBuffer dataBuffer, int width, int atY) {}
	protected void writeCopy(ByteBuffer dataBuffer, int width, int atY) {}

	//--------------------------------------------------------------------------
	public Layer_ProceduralFraction() {
		super(NAME);
		mbIsProcedural = true;
	}
	
	//--------------------------------------------------------------------------
	protected Selection query(JsonNode queryNode, Selection selection) {

/*			fraction: this.getComponent('DSS_FractionOfLand').getValue(),
			gridCellSize: 900, // 900 meters x 900 meters
			seed: this.DSS_Seed
*/		
		Logger.info("Running Procedural Fraction Query");
		
		// Get fraction
		float fraction = 0.5f;
		JsonNode queryFraction = queryNode.get("fraction");
		if (queryFraction == null) {
			Logger.info(" ! fraction jsonNode not found, using default fraction value");
		}
		else {
			fraction = queryFraction.numberValue().floatValue() / 100.0f;
		}
		
		// Get seed
		int seed = 0;
		JsonNode querySeed = queryNode.get("seed");
		if (querySeed == null) {
			Logger.info(" ! fraction seed jsonNode not found, using default seed value");
		}
		else {
			seed = querySeed.numberValue().intValue();
		}

		// Get Cell Size for procedural grid...
		int gridSize = 30; // 30 raster cells wide...
		JsonNode queryCellSize = queryNode.get("gridCellSize");
		if (queryCellSize == null) {
			Logger.info(" ! fraction cell size jsonNode not found, using default cell size value");
		}
		else {
			gridSize = queryCellSize.numberValue().intValue();
			if (gridSize < 1) gridSize = 1;
			if (gridSize > 100) gridSize = 100;
		}

		// Now process it!!
		Random rand = new Random(seed);
		
		int cellY = 0;
		while(cellY < mHeight) {
			int cellX = 0;
			while(cellX < mWidth) {
				
				if (rand.nextFloat() > fraction) {
					
					// calculate how many times to iterate and clamp to grid...
					int toY = cellY + gridSize; if (toY >= mHeight) toY = mHeight;
					int toX = cellX + gridSize; if (toX >= mWidth) toX = mWidth;
					
					for (int y = cellY; y < toY; y++) {
						for (int x = cellX; x < toX; x++) {
							selection.mRasterData[y][x] = 0;
						}
					}
				}
				cellX += gridSize;
			}
			cellY += gridSize;
		}
		
		return selection;
	}
}

