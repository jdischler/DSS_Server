package util;

import play.*;
import java.util.*;
import java.io.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class ModelResult
{
	public String mName;
	public String mDestinationFolder; // for saving
	public float [][] mRasterData;
	public int mWidth, mHeight;

	//----------------------------------------------------------------------
	public ModelResult(String name, String folder, float [][] data, int width, int height) {

		mName = name;
		mDestinationFolder = folder;
		mRasterData = data;
		mWidth = width;
		mHeight = height;
	}
}
	
