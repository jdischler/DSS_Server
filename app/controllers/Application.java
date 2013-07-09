package controllers;

import util.*;

import java.util.*;
import java.io.*;
import java.net.*;

import play.*;
import play.mvc.*;
import play.Logger;
import play.cache.*;

import views.html.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import javax.xml.bind.DatatypeConverter;

//------------------------------------------------------------------------------
public class Application extends Controller 
{
	
	//--------------------------------------------------------------------------
	public static Result index() {
		
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception {
		
		Query query = new Query();
		JsonNode result = query.selection(request().body().asJson());
		return ok(result);
	}

	//--------------------------------------------------------------------------
	public static Result layerParmRequest() throws Exception {
		
		JsonNode request = request().body().asJson();

		JsonNode ret = Layer_Base.getParameter(request);
		if (ret != null) {
			return ok(ret);
		}
		
		return ok(); // derp, not really OK if gets here...or??
	}
	
	//----------------------------------------------------------------------
	public static Result wmsRequest() {
		
		// Open up request from client...
		JsonNode request = request().body().asJson();

		Logger.info(request.toString());
		// e.g., 'Vector:Watersheds-C'
		String layerName = request.get("layer").getTextValue();
		int x = request.get("x").getIntValue(); // 585
		int y = request.get("y").getIntValue(); // 273
		int width = request.get("width").getIntValue();
		int height = request.get("height").getIntValue();
		String bbox = request.get("bbox").getTextValue();

		BufferedReader rd = null;
		OutputStreamWriter wr = null;
		
		try {
			URL url = new URL("http://pgis.glbrc.org:8080/geoserver/Vector/wms" + 
				"?LAYERS=" + layerName + "&QUERY_LAYERS=" + layerName + 
				"&STYLES=&SERVICE=WMS&VERSION=1.1.1&SRS=EPSG:900913" +
				"&REQUEST=GetFeatureInfo&FEATURE_COUNT=10&INFO_FORMAT=application/vnd.ogc.gml/3.1.1" +
				"&BBOX=" + bbox +
				"&HEIGHT=" + Integer.toString(height) + 
				"&WIDTH=" + Integer.toString(width) +
				"&X=" + Integer.toString(x) + 
				"&Y=" + Integer.toString(y));
		
			Logger.info("------------------------------");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.flush();
			
			// get the response
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line = rd.readLine();
			if (line != null) {
				Logger.info(line);
				String line1 = rd.readLine();
				if (line1 != null) {
					return ok(line1);
				}
			}
			
			wr.close();
			rd.close();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info("WMS request failed");
		// TODO: return NOT ok?
		return ok();
	}
	
	//----------------------------------------------------------------------
	public static Result Models() throws Exception 
	{
		File output = new File("./layerData/Client_ID");
		if(output.exists())
		{
			Logger.info("This folder already exists");
		}
		else
		{
			output.mkdir();
		}
		
		Query query = new Query();
		Selection selection = null;
		
		try {
			selection = query.execute(request().body().asJson());
		} catch (Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info("Server Run The Models Request:");
		Models model = new Models();
		
		// Code for Transformation
		int I_Code = 1; // 1
		int S_Code = 256; // 9
		
		// Rotation
		int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		Layer_Base layer = Layer_Base.getLayer("Rotation");
		int width = layer.getWidth();
		int height = layer.getHeight();
		int[][] RotationT = new int [height][width];
		
		// Chnage Rotation to Rotation_T
		for (int y = 0; y < height; y++) 
		{
			for (int x = 0; x < width; x++) 
			{
				RotationT[y][x] = Rotation[y][x];
				if (RotationT[y][x] == I_Code)
				{
					RotationT[y][x] = S_Code;
				}
			}
		}
			
		JsonNode SendBackT = model.modeloutcome(request().body().asJson(), selection, "Client_ID", RotationT);
		
		JsonNode SendBackD = model.modeloutcome(request().body().asJson(), selection, "Default", Rotation);
		
		//JsonNode SendBackD = Global.GetDefaultSendBack();
		
		ObjectNode SendBack = JsonNodeFactory.instance.objectNode();
		
		SendBack.put("Default", SendBackD);
		SendBack.put("Transform", SendBackT);
		// Open a file to write Delta for HI
		int w = selection.mWidth;
		//Logger.info(integer.toString(w));
		int h = selection.mHeight;
		//Logger.info(integer.toString(h));
		int NO_DATA = -9999;
		float Habitat_Index_Delta = 0;
		float Nitrogen_Delta = 0;
		float Phosphorus_Delta = 0;
		float Pest_Delta = 0;
		float Pollinator_Delta = 0;
		
		// Buffer reader
		BufferedReader br1 = model.HeaderRead("Bird_Index", w, h, "Default");
		BufferedReader br2 = model.HeaderRead("Bird_Index", w, h, "Client_ID");
		BufferedReader br3 = model.HeaderRead("Nitrogen", w, h, "Default");
		BufferedReader br4 = model.HeaderRead("Nitrogen", w, h, "Client_ID");
		BufferedReader br5 = model.HeaderRead("Phosphorus", w, h, "Default");
		BufferedReader br6 = model.HeaderRead("Phosphorus", w, h, "Client_ID");
		BufferedReader br7 = model.HeaderRead("Pest", w, h, "Default");
		BufferedReader br8 = model.HeaderRead("Pest", w, h, "Client_ID");
		BufferedReader br9 = model.HeaderRead("Pollinator", w, h, "Default");
		BufferedReader br10 = model.HeaderRead("Pollinator", w, h, "Client_ID");
		
		output = new File("./layerData/Client_ID/Delta");
		if(output.exists()) {
			Logger.info("The Delta folder is already exist");
		}
		else {
			output.mkdir();
		}

		// Buffer writer
		PrintWriter out1 = model.HeaderWrite("Delta_Bird_Index", w, h, "Client_ID/Delta");
		PrintWriter out2 = model.HeaderWrite("Delta_Nitrogen", w, h, "Client_ID/Delta");
		PrintWriter out3 = model.HeaderWrite("Delta_Phosphorus", w, h, "Client_ID/Delta");
		PrintWriter out4 = model.HeaderWrite("Delta_Pest", w, h, "Client_ID/Delta");
		PrintWriter out5 = model.HeaderWrite("Delta_Pollinator", w, h, "Client_ID/Delta");
		
		int y = 0;
		
		while (br1.ready() && br2.ready() && br3.ready() && br4.ready() && br5.ready() && br6.ready() && br7.ready() && br8.ready() && br9.ready() && br10.ready()) 
		{
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			StringBuffer sb3 = new StringBuffer();
			StringBuffer sb4 = new StringBuffer();
			StringBuffer sb5 = new StringBuffer();
			
			if (y >= h) 
			{
				Logger.error("BAD READ - more lines than expected!");
				break;
			}
			
			String line1 = br1.readLine();
			String split1[] = line1.split("\\s+");
			String line2 = br2.readLine();
			String split2[] = line2.split("\\s+");
			String line3 = br3.readLine();
			String split3[] = line3.split("\\s+");
			String line4 = br4.readLine();
			String split4[] = line4.split("\\s+");
			String line5 = br5.readLine();
			String split5[] = line5.split("\\s+");
			String line6 = br6.readLine();
			String split6[] = line6.split("\\s+");
			String line7 = br7.readLine();
			String split7[] = line7.split("\\s+");
			String line8 = br8.readLine();
			String split8[] = line8.split("\\s+");
			String line9 = br9.readLine();
			String split9[] = line9.split("\\s+");
			String line10 = br10.readLine();
			String split10[] = line10.split("\\s+");
			
			for (int x = 0; x < split1.length; x++) 
			{	
				//if (Integer.valueOf(split1[x]) == (int)NO_DATA) 
				if (selection.mSelection[y][x] == 0)
				{
					sb1.append(Integer.toString(NO_DATA));
					sb2.append(Integer.toString(NO_DATA));
					sb3.append(Integer.toString(NO_DATA));
					sb4.append(Integer.toString(NO_DATA));
					sb5.append(Integer.toString(NO_DATA));
				}
				else
				{
					// Compare Default with After Transformation
					Habitat_Index_Delta = Float.parseFloat(split1[x]) - Float.parseFloat(split2[x]);
					Nitrogen_Delta = Float.parseFloat(split3[x]) - Float.parseFloat(split4[x]);
					Phosphorus_Delta = Float.parseFloat(split5[x]) - Float.parseFloat(split6[x]);
					Pest_Delta = Float.parseFloat(split7[x]) - Float.parseFloat(split8[x]);
					Pollinator_Delta = Float.parseFloat(split9[x]) - Float.parseFloat(split10[x]);
					
					sb1.append(String.format("%.4f", Habitat_Index_Delta));
					sb2.append(String.format("%.4f", Nitrogen_Delta));
					sb3.append(String.format("%.4f", Phosphorus_Delta));
					sb4.append(String.format("%.4f", Nitrogen_Delta));
					sb5.append(String.format("%.4f", Phosphorus_Delta));
				}
				if (x != w - 1) 
				{
					sb1.append(" ");
					sb2.append(" ");
					sb3.append(" ");
					sb4.append(" ");
					sb5.append(" ");
				}
				
			}
			
			out1.println(sb1.toString());
			out2.println(sb2.toString());
			out3.println(sb3.toString());
			out4.println(sb2.toString());
			out5.println(sb3.toString());
			
			y++;
		}
		br1.close();
		br2.close();
		br3.close();
		br4.close();
		br5.close();
		br6.close();
		br7.close();
		br8.close();
		br9.close();
		br10.close();
		
		out1.close();
		out2.close();
		out3.close();
		out4.close();
		out5.close();
		
		return ok(SendBack);
	}
}
