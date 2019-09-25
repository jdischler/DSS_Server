package util;

import play.*;
import java.util.*;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.apache.commons.io.FileUtils; 

//------------------------------------------------------------------------------
public class QueuedWriter implements Runnable {

	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}

	private static List<ScenarioSetupResult> mScenarioSetupsToWrite;
	private static List<ModelResult> mResultsToWrite;

	private static Thread mThreadHandle;
	
	private static QueuedWriter mWriter;
	
	// NOTE: setting this to false may cause analysis and heatmapping to break. But if you
	//	just want the server to write ASC (or other formats) for analysis, then that might be ok.
	private static final boolean mbWriteBinary_DSS = true; 
	private static final boolean mbWriteText_ASC = false;
	
	private static final boolean mbWriteLandscape = false; // thinking this is mostly for debugging at this point?
	
	//--------------------------------------------------------------------------
	public final static void beginWatchWriteQueue() {
	
		if (mWriter == null) {
			Logger.info("");
			Logger.info(" ... Creating a new file writer queue ...");
			mWriter = new QueuedWriter();
			
			if (mScenarioSetupsToWrite == null) {
				mScenarioSetupsToWrite = new ArrayList<ScenarioSetupResult>();
			}
			if (mResultsToWrite == null) {
				mResultsToWrite = new ArrayList<ModelResult>();
			}
			mThreadHandle = new Thread(mWriter);
			detailedLog(" ...Queued writer priority is: " + Integer.toString(mThreadHandle.getPriority()));
			// Sanity check, seems to almost never happen? 
			if (mThreadHandle.getPriority() > Thread.NORM_PRIORITY) {
				detailedLog(" ...Thread priority pretty, high, reducing to Normal");
				mThreadHandle.setPriority(Thread.NORM_PRIORITY);
				detailedLog(" ...Queued writer priority is now: " + Integer.toString(mThreadHandle.getPriority()));
			}
			
			mThreadHandle.start();
		}
	}
	
	//--------------------------------------------------------------------------
	public final static void queueResults(List<ModelResult> results) {
		
		mWriter.queueResultsInternal(results);
	}

	//--------------------------------------------------------------------------
	public final static void queueResults(ScenarioSetupResult result) {
		
		mWriter.queueResultsInternal(result);
	}
	
	//--------------------------------------------------------------------------
	public final static boolean doesWriteQueueHaveFiles() {
		
		return (mResultsToWrite.size() > 0 || mScenarioSetupsToWrite.size() > 0);
	}
	
	// Internal funcs, don't use...
	//--------------------------------------------------------------------------
	private synchronized final void queueResultsInternal(List<ModelResult> results) {
		
		detailedLog(" ... Adding ModelResults to writer queue. Notify queue to wake up.....");
		mResultsToWrite.addAll(results);
		notifyAll();
	}

	// Internal funcs, don't use...
	//--------------------------------------------------------------------------
	private synchronized final void queueResultsInternal(ScenarioSetupResult result) {
		
		detailedLog(" ... Adding ScenarioSetupResult to writer queue. Notify queue to wake up.....");
		mScenarioSetupsToWrite.add(result);
		notifyAll();
	}
	
	// Internal funcs, don't use...
	// FIXME: TODO: should turn some of this code dump into subfunctions to break this up....
	//--------------------------------------------------------------------------
	public synchronized void run() {
		
		Logger.info(" ... Writer thread is watching the writer queue");
		while (true) {
			
			if (!doesWriteQueueHaveFiles()) {
				detailedLog("QueuedWriter queue has no results, waiting......");
				try {
					wait(90000);
				}
				catch(Exception e) {
					// TODO: this a bad thing to end up with this kind of exception
					Logger.info("Writer queue exception: " + e.toString());
				};
			}
			else {
				
				if (mResultsToWrite.size() > 0) {
					ModelResult result = mResultsToWrite.remove(0);
					File writeFolder = new File("./layerData/" + result.mDestinationFolder + "/");
					if (writeFolder.exists() == false) {
						detailedLog(" ... Writer queue creating directory: " + writeFolder.toString());
						try {
							FileUtils.forceMkdir(writeFolder);
						}
						catch (Exception err) {
							Logger.info(err.toString());
						}
						//writeFolder.mkdirs();
						if (writeFolder.exists() == false) {
							Logger.error(" Error - Writer queue directory creation failed!!");
						}
					}
					
					// ---WRITE BINARY DSS?
					if (mbWriteBinary_DSS) {
						File writeFile = new File(writeFolder, result.mName + ".dss");
						detailedLog(" ... Writer queue writing DSS: " + writeFile.toString());
						
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
							File writeFile = new File(writeFolder, result.mName + ".asc");
							detailedLog(" ... Writer queue writing ASC: " + writeFile.toString());
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
					result.mRasterData = null;
				}
				
				if (mScenarioSetupsToWrite.size() > 0) {
					
					ScenarioSetupResult result = mScenarioSetupsToWrite.remove(0);
					File writeFolder = new File("./layerData/"+ result.mDestinationFolder + "/");
					try {
						writeFolder = new File(writeFolder.getCanonicalPath());
					}
					catch (Exception err) {
						Logger.info(err.toString());
					}
					
					if (writeFolder.exists() == false) {
						detailedLog(" ... Writer queue creating directory: " + writeFolder.toString());
						try {
							FileUtils.forceMkdir(writeFolder);
						}
						catch (Exception err) {
							Logger.info(err.toString());
						}
						if (writeFolder.exists() == false) {
							Logger.error(" Error - Writer queue directory creation failed!!");
						}
					}

					// ---WRITE Selection in Binary --- 
					if (true) {
						File writeFile = new File(writeFolder, "selection.sel");
						detailedLog(" ... Writer queue writing sel: " + writeFile.toString());
						
						Binary_Writer writer = new Binary_Writer(writeFile, result.mWidth, result.mHeight);
						ByteBuffer writeBuffer = writer.writeHeader(1);
					
						for (int y=0; y < result.mHeight; y++) {
							for (int x=0; x < result.mWidth; x++) {
								writeBuffer.put(x, result.mSelectionData[y][x]);
							}
							writer.writeLine();
						}
						writer.close();
						result.mSelectionData = null;
					}
					
					// ---WRITE BINARY DSS for New Transformed Landscape?
					if (mbWriteBinary_DSS && mbWriteLandscape) {
						File writeFile = new File(writeFolder, "cdl_transformed.dss");
						detailedLog(" ... Writer queue writing DSS: " + writeFile.toString());
						
						Binary_Writer writer = new Binary_Writer(writeFile, result.mWidth, result.mHeight);
						ByteBuffer writeBuffer = writer.writeHeader();
					
						for (int y=0; y < result.mHeight; y++) {
							for (int x=0; x < result.mWidth; x++) {
								writeBuffer.putInt(x * 4, result.mLandscapeData[y][x]);
							}
							writer.writeLine();
						}
						writer.close();
					}
					
					// ---WRITE TEXT ASC for New Transformed Landscape?
					if (mbWriteText_ASC && mbWriteLandscape) {
						PrintWriter ascOut = null;
						int width = result.mWidth, height = result.mHeight;
						try {
							File writeFile = new File(writeFolder, "cdl_transformed.asc");
							detailedLog(" ... Writer queue writing ASC: " + writeFile.toString());
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
									int data = result.mLandscapeData[y][x];
									if (data > -9999) {
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
					result.mLandscapeData = null;
				}
			}
		}
	}
}

