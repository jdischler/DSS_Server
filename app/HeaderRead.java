package util;

import play.*;
import java.util.*;
import java.io.*;

public class HeaderRead
{
	// Read The Header of The File
	private BufferedReader br;
	
	public HeaderRead(String name, int W, int H, String Input_Folder) 
	{
		
		try 
		{
			br = new BufferedReader(new FileReader("./layerData/" + Input_Folder + "/" + name + ".asc"));
			String line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
		} 
		catch (Exception err) 
		{
			Logger.info(err.toString());
		}
		
		//return br;
	}

	public BufferedReader getReader() 
	{
		return br;
	}
}
