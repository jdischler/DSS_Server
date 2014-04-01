package util;

import play.*;
import java.util.*;
import java.io.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

//------------------------------------------------------------------------------
public class Model_Base
{
	private static String mBasePath = "./layerData/";
	
	//--------------------------------------------------------------------------
	protected File getFileForPath(String subPath, String modelFile) {
		
		File testPath = new File(mBasePath + subPath + "/");
		if (!testPath.exists()) {
			Logger.info(testPath.toString() + " does not exist. Attempting to create...");
			testPath.mkdirs();
		}
		return new File(mBasePath + subPath + "/" + modelFile + ".dss");
	}
}

