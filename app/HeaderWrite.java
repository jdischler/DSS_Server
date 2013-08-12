package util;

import play.*;
import java.util.*;
import java.io.*;

public class HeaderWrite
{
	
	private PrintWriter out;

	// Write Header To The File
	public HeaderWrite(String name, int W, int H, String Output_Folder) 
	{
		
		try 
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/" + Output_Folder + "/" + name + ".asc")));
		} 
		catch (Exception err) 
		{
			Logger.info(err.toString());
		}
		
		out.println("ncols         " + Integer.toString(W));
		out.println("nrows         " + Integer.toString(H));
		out.println("xllcorner     -10062652.65061");
		out.println("yllcorner     5249032.6922889");
		out.println("cellsize      30");
		out.println("NODATA_value  -9999");
		
		//return out;
	}
	
	public PrintWriter getWriter() 
	{
		return out;
	}
}
