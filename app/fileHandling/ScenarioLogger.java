package fileHandling;

import play.*;
import util.Scenario;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.apache.commons.io.FileUtils; 

//------------------------------------------------------------------------------
public class ScenarioLogger implements Runnable {

	private static final boolean DETAILED_DEBUG_LOGGING = false;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}

	private static BlockingQueue<Scenario> mScenariosToWrite;

	private static Thread mThreadHandle;
	
	private static ScenarioLogger mWriter;
	
	// NOTE: setting this to false may cause analysis and heatmapping to break. But if you
	//	just want the server to write ASC (or other formats) for analysis, then that might be ok.
	private static final boolean mbWriteBinary_DSS = true; 
	private static final boolean mbWriteText_ASC = false;
	
	private static final boolean mbWriteLandscape = false; // thinking this is mostly for debugging at this point?
	
	private static Integer mFileID = 1;
	
	//--------------------------------------------------------------------------
	public final static void beginWatchWriteQueue() {
	
		if (mWriter == null) {
			Logger.info("");
			Logger.info(" ... Creating a new scenario Logger queue ...");
			mWriter = new ScenarioLogger();
			
			if (mScenariosToWrite == null) {
				mScenariosToWrite = new LinkedBlockingQueue<Scenario>();
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
		File writeFolder = new File("./scenarioLog/");
		if (writeFolder.exists() == false) {
			Logger.info(" ... ScenarioLogger: Writer queue creating directory: " + writeFolder.toString());
			try {
				FileUtils.forceMkdir(writeFolder);
			}
			catch (Exception err) {
				Logger.info(err.toString());
			}
			//writeFolder.mkdirs();
			if (writeFolder.exists() == false) {
				Logger.error(" Error - ScenarioLogger: Writer queue directory creation failed!!");
			}
		}
		
		// Try to create a new file
		while (true) {
			File writeFile = new File("./scenarioLog/results" + mFileID + ".csv");
			if (writeFile.exists()) {
				mFileID++;
			}
			else {
				break;
			}
		}
		
		// start a new log
		try ( BufferedWriter writer = new BufferedWriter(
		        new FileWriter("./scenarioLog/results" + mFileID + ".csv", false)  //Set FALSE to overwrite (create new)
		    )) { 

			writer.write(Scenario.logCSVHeader());
		}
		catch(Exception e) {
			// oof
		}
	}
	
	//--------------------------------------------------------------------------
	public final static void queueResults(Scenario setup) {
		
		detailedLog(" ... Adding Scenario to writer queue. Notify queue to wake up.....");
		mWriter.queueResultsInternal(setup);
	}

	// Internal funcs, don't use...
	//--------------------------------------------------------------------------
	private synchronized final void queueResultsInternal(Scenario setup) {
		
		detailedLog(" ... Adding Scenario to writer queue. Notify queue to wake up.....");
		mScenariosToWrite.add(setup);
	}

	// Internal funcs, don't use...
	// FIXME: TODO: should turn some of this code dump into subfunctions to break this up....
	//--------------------------------------------------------------------------
	public void run() {
		
		Logger.info(" ... ScenarioLogger thread is watching the ScenarioLogger queue");
		while (true) {
			
			detailedLog("ScenarioLogger maybe has results...");
			Scenario setup = null;
			try {
				setup = mScenariosToWrite.take();
			}
			catch(Exception e) {
				// ooooof
			}
			if (setup != null) {
				detailedLog("ScenarioLogger queue working, waiting......");				
				String out = setup.logCSV();
				try ( BufferedWriter writer = new BufferedWriter(
                        new FileWriter("./scenarioLog/results" + mFileID + ".csv", true)  //Set true for append mode
                    )) { 
					writer.write(out);
				}
				catch(Exception e) {
					// oof
				}
			}
		}
	}
}
