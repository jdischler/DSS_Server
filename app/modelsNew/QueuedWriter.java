package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class QueuedWriter implements Runnable {

	private static List<ModelResult> mResultsToWrite;
	private static QueuedWriter mWriter;
	
	// NOTE: setting this to false may cause analysis and heatmapping to break. But if you
	//	just want the server to write ASC (or other formats) for analysis, then that might be ok.
	private static final boolean mbWriteBinary_DSS = true; 
	private static final boolean mbWriteText_ASC = false;
	
	//--------------------------------------------------------------------------
	public final static void beginWatchWriteQueue() {
	
		if (mWriter == null) {
			Logger.info("Creating a new file writer queue");
			mWriter = new QueuedWriter();
			if (mResultsToWrite == null) {
				mResultsToWrite = new ArrayList<ModelResult>();
			}
			new Thread(mWriter).start();		
		}
	}
	
	//--------------------------------------------------------------------------
	public final static void queueResults(List<ModelResult> results) {
		
		mWriter.queueResultsInternal(results);
	}
	
	//--------------------------------------------------------------------------
	public final static boolean doesWriteQueueHaveFiles() {
		
		return (mResultsToWrite.size() > 0);
	}
	
	// Internal funcs, don't use...
	//--------------------------------------------------------------------------
	private synchronized final void queueResultsInternal(List<ModelResult> results) {
		
		Logger.info("Adding ModelResults to writer queue, about to notify queue to wake up.....");
		mResultsToWrite.addAll(results);
		notifyAll();
	}
	
	// Internal funcs, don't use...
	//--------------------------------------------------------------------------
	public synchronized void run() {
		
		Logger.info("Watching writer queue");
		while (true) {
			
			if (doesWriteQueueHaveFiles()) {
				Logger.info("Writer queue has results to write!!");
				ModelResult result = mResultsToWrite.remove(0);
				File writeFolder = new File("./layerData/"+ result.mDestinationFolder + "/");
				if (writeFolder.exists() == false) {
					writeFolder.mkdirs();
				}
				
				// ---WRITE BINARY DSS?
				if (mbWriteBinary_DSS) {
					File writeFile = new File("./layerData/"+ result.mDestinationFolder + "/" 
												+ result.mName + ".dss");
					Binary_Writer writer = new Binary_Writer(writeFile, result.mWidth, result.mHeight);
					ByteBuffer writeBuffer = writer.writeHeader();
				
					for (int y=0; y < result.mHeight; y++) {
						for (int x=0; x < result.mWidth; x++) {
							writeBuffer.putFloat(x * 4, result.mRasterData[y][x]);
						}
						writer.writeLine();
					}
					writer.close();
				}
				
				// ---WRITE TEXT ASC?
				if (mbWriteText_ASC) {
					PrintWriter ascOut = null;
					int width = result.mWidth, height = result.mHeight;
					try {
						File writeFile = new File("./layerData/"+ result.mDestinationFolder + "/" 
												+ result.mName + ".asc");
						ascOut = new PrintWriter(new BufferedWriter(new FileWriter(writeFile)));
						ascOut.println("ncols         " + Integer.toString(width));
						ascOut.println("nrows         " + Integer.toString(height));
						ascOut.println("xllcorner     -10062652.65061");
						ascOut.println("yllcorner     5249032.6922889");
						ascOut.println("cellsize      30");
						ascOut.println("NODATA_value  -9999");
					} 
					catch (Exception err) {
						Logger.info(err.toString());
					}
					
					if (ascOut != null) {	
						String stringNoData = Integer.toString(-9999);

						for (int y = 0; y < height; y++) {
							StringBuilder ascLine = new StringBuilder(width * 10); // estimate 10 characters per x-raster
							for (int x=0; x < width; x++) {
								float data = result.mRasterData[y][x];
								if (data > -9999.0f) {
									ascLine.append(data);
								}
								else {
									ascLine.append(stringNoData);
								}
								if (x != width - 1) {
									ascLine.append(" ");
								}
							}
							ascOut.println(ascLine.toString());
						}
						try {
							ascOut.close();
						}
						catch (Exception err) {
							Logger.info(err.toString());
						}
					}
				}
				
				//--- TODO: other formats if desired? E.g., ArcGIS native-ish formats?
			}
			else {
				//Logger.info("Writer queue has no results, waiting......");
				try {
					wait(90000);
				}
				catch(Exception e) {
					// TODO: this a bad thing to end up with this kind of exception
				};
			}
		}
	}
}

