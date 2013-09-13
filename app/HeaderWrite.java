package util;

import play.*;
import java.util.*;
import java.io.*;

public class HeaderWrite
{
	
	private PrintWriter mOut;

	// Write Header To The File - Output_Folder can be NULL to not write to a sub-folder
	public HeaderWrite(String name, int W, int H, String Output_Folder) 
	{
		// only add an output folder on if needed...
		String path = "./layerData/";
		if (Output_Folder != null) {
			path += Output_Folder + "/";
		}
		path += name + ".asc";
		
		try 
		{
			mOut = new PrintWriter(new BufferedWriter(new FileWriter(path)));
		} 
		catch (Exception err) 
		{
			Logger.info(err.toString());
		}
		
		mOut.println("ncols         " + Integer.toString(W));
		mOut.println("nrows         " + Integer.toString(H));
		mOut.println("xllcorner     -10062652.65061");
		mOut.println("yllcorner     5249032.6922889");
		mOut.println("cellsize      30");
		mOut.println("NODATA_value  -9999");
	}
	
	public PrintWriter getWriter() 
	{
		return mOut;
	}
}
