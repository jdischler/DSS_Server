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

