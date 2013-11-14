package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//import org.codehaus.jackson.*;
//import org.codehaus.jackson.node.*;

import com.fasterxml.jackson.core.*;

// USAGE SAMPLE
/*
	File dssFile = new File("./layerData/some.dss");
	Binary_Reader myReader = new Binary_Reader(dssFile);
	
	if (myReader.readHeader()) {
		int width = myReader.getWidth();
		int height = myReader.getHeight();
		
		for (int y=0; y < height; y++) {
			ByteBuffer buff = myReader.readLine();
			if (buff != null) {
				for (int x=0; x < width; x++) {
					float data = buffer.getFloat(x * 4); // blah, 4 = size of float, ie 32bit
				}
			}
		}
		myReader.close();
	}
*/

// Reader class for our DSS files...
//------------------------------------------------------------------------------
public class Binary_Reader
{
	// FILE reading management related
	private File mInputFile;
	private FileInputStream mFileStream;
	private ReadableByteChannel mFileChannel;
	private ByteBuffer mLineBuffer;
	
	// HEADER related
	private final static int mBinaryWriteVersion = 1; // NOTE: update version for each new header version change
	
	private int mWidth, mHeight;
	private float mCellSize, mCornerX, mCornerY;
	private int mNoDataValue;

	// CONSTRUCTOR
	//--------------------------------------------------------------------------
	public Binary_Reader(File dssFile) {
		mInputFile = dssFile;
	}
	
	//--------------------------------------------------------------------------
	public int getWidth() {
		return mWidth;
	}
	public int getHeight() {
		return mHeight;
	}
	
	// Opens binary DSS file and loads the header. Returns FALSE if file does not exist or
	//	some other problem occurs	
	//--------------------------------------------------------------------------
	public boolean readHeader() {
		
		if (!mInputFile.exists()) {
			return false;
		}
		
		try {
			mFileStream = new FileInputStream(mInputFile);
			mFileChannel = mFileStream.getChannel();
			
			Logger.info("  Reading header...");
			ByteBuffer buf = ByteBuffer.allocateDirect(4); // FIXME: size of int (version)?
			mFileChannel.read(buf); 
			buf.rewind();
			Logger.info("  - Binary file version: " + Integer.toString(buf.getInt()));
				
			buf = ByteBuffer.allocateDirect(6 * 4); // FIXME: size of header * size of int?
			mFileChannel.read(buf); 
			buf.rewind();
			
			mWidth = buf.getInt();
			mHeight = buf.getInt();
			mCornerX = buf.getFloat();
			mCornerY = buf.getFloat();
			mCellSize = buf.getFloat();
			mNoDataValue = buf.getInt();
			
			Logger.info("  - Width: " + Integer.toString(mWidth) 
							+ "  Height: " + Integer.toString(mHeight));
		}
		catch (Exception e) {
			Logger.info(e.toString());
			return false;
		}
		
		mLineBuffer = ByteBuffer.allocateDirect(mWidth * 4); // FIXME: size of int?
		return true;
	}
	
	//--------------------------------------------------------------------------
	public ByteBuffer readLine() {

		try {		
			mLineBuffer.clear();
			mFileChannel.read(mLineBuffer);
			mLineBuffer.rewind();
			return mLineBuffer;
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	public void close() {

		try {
			mFileChannel.close();
			mFileStream.close();
			mFileStream = null;
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		finally {
			if (mFileStream != null) {
				try {
					mFileStream.close();
				}
				catch (Exception e) {
					Logger.info(e.toString());
				}
			}
		}
	}
}

