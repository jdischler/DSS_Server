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
	public static Result index() 
	{
		
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception 
	{
		
		Query query = new Query();
		JsonNode result = query.selection(request().body().asJson());
		return ok(result);
	}

	//--------------------------------------------------------------------------
	public static Result layerParmRequest() throws Exception 
	{
		
		JsonNode request = request().body().asJson();

		JsonNode ret = Layer_Base.getParameter(request);
		if (ret != null) 
		{
			return ok(ret);
		}
		
		return ok(); // derp, not really OK if gets here...or??
	}
	
	//----------------------------------------------------------------------
	public static Result wmsRequest() 
	{
		
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
		
		try 
		{
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
	public static JsonNode createAssumption(String category, String icon,
						String variableName, 
						String displayName, float defaultValue) 
	{
	
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("Category", category);
		node.put("Icon", icon);
		node.put("VariableName", variableName);
		node.put("DisplayName", displayName);
		node.put("DefaultValue", defaultValue);

		return node;		
	}
	
	// FIXME: TODO: read from ./serverData/assumptions/assumptions.dat
	//----------------------------------------------------------------------
	public static Result getAssumptions() throws Exception 
	{
				
		ArrayNode array = JsonNodeFactory.instance.arrayNode();
		array.add(createAssumption("Economic", "", "p_corn", "Corn Price", 120.0f));
		array.add(createAssumption("Economic", "", "p_grass", "Grass Price", 100.0f));
		array.add(createAssumption("Economic", "economic_icon.png", "p_eth", "Ethanol Price", 300.0f));
		
		array.add(createAssumption("Conversion Rates", "", "conv_corn", "Corn Grain", 1.8f));
		array.add(createAssumption("Conversion Rates", "", "conv_stover", "Stover", 1.4f));
		array.add(createAssumption("Conversion Rates", "scenario_icon.png", "conv_grass", "Grass", 1.2f));
		
		array.add(createAssumption("Climate", "", "av_temp", "Average Temperature", 76.1f));
		array.add(createAssumption("Climate", "climate_icon.png", "av_rain", "Average Rainfall", 19.2f));
		
		array.add(createAssumption("Other", "policy_icon.png", "temp", "Dunno", 1));

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("Assumptions", array);
		
		return ok(sendback);
	}

	//----------------------------------------------------------------------
	public static Result Models() throws Exception 
	{
		//Logger.info("Called into Model:");
		Logger.info(request().body().asJson().toString());

		File output = new File("./layerData/Client_ID");
		if(output.exists())
		{
			Logger.info("This folder already exists");
		}
		else
		{
			output.mkdir();
		}

		// Rotation
		//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		
		// Create a new scenario and get a transformed crop rotation layer from it...
		Scenario scenario = new Scenario();
		scenario.getTransformedRotation(request().body().asJson());
		Selection selection = scenario.mSelection;
		//Logger.info("Server Run The Models Request:");
		
		Layer_Base layer;
		int width, height;
		// Rotation
		int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();
		
		//Selection selection = new Selection(width, height);

		// Corn and Grass Production for D
		//Model_Corn_Grass_Production Model_CGD = new Model_Corn_Grass_Production();
		//TwoArrays ArrayD = Model_CGD.Corn_Grass_P(request().body().asJson(), selection, "Default", Rotation);
		//float ArrayCD[] = ArrayD.a;
		//float ArrayGD[] = ArrayD.b;
		
		// Corn and Grass Production for T
		//Model_Corn_Grass_Production Model_CGT = new Model_Corn_Grass_Production();
		//TwoArrays ArrayT = Model_CGT.Corn_Grass_P(selection, "Client_ID", scenario.mNewRotation);
		//float ArrayCT[] = ArrayT.a;
		//float ArrayGT[] = ArrayT.b;

		// Regular Models
		// Model_Ethanol
		//Model_Ethanol ED = new Model_Ethanol();
		//JsonNode SendBackED = ED.Ethanol(ArrayCD, ArrayGD, request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Ethanol
		//Model_Ethanol ET = new Model_Ethanol();
		//JsonNode SendBackET = ET.Ethanol(ArrayCT, ArrayGT, request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//ET.Ethanol(ArrayCT, ArrayGT, selection, "Client_ID", scenario.mNewRotation);
		
		// Model_Net_Energy
		//Model_Net_Energy NE_D = new Model_Net_Energy();
		//JsonNode SendBackNED = NE_D.Net_Energy(ArrayCD, ArrayGD, request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Net_Energy
		//Model_Net_Energy NE_T = new Model_Net_Energy();
		//JsonNode SendBackNET = NE_T.Net_Energy(ArrayCT, ArrayGT, request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//NE_T.Net_Energy(ArrayCT, ArrayGT, selection, "Client_ID", scenario.mNewRotation);
		
		// Model_Net_Income
		//Model_Net_Income NI_D = new Model_Net_Income();
		//JsonNode SendBackNID = NI_D.Net_Income(ArrayCD, ArrayGD, request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Net_Income
		//Model_Net_Income NI_T = new Model_Net_Income();
		//JsonNode SendBackNIT = NI_T.Net_Income(ArrayCT, ArrayGT, request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//NI_T.Net_Income(ArrayCT, ArrayGT, selection, "Client_ID", scenario.mNewRotation);

		//  Models with Moving Window
		// Model_Habitat_Index
		//Model_Habitat_Index HI_D = new Model_Habitat_Index();
		//JsonNode SendBackHID = HI_D.Habitat_Index(request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Habitat_Index
		//Model_Habitat_Index HI_T = new Model_Habitat_Index();
		//JsonNode SendBackHIT = HI_T.Habitat_Index(request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//HI_T.Habitat_Index(selection, "Client_ID", scenario.mNewRotation);
		
		// Model_Nitrogen
		//Model_Nitrogen N_D = new Model_Nitrogen();
		//JsonNode SendBackND = N_D.Nitrogen(request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Nitrogen
		//Model_Nitrogen N_T = new Model_Nitrogen();
		//JsonNode SendBackNT = N_T.Nitrogen(request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//N_T.Nitrogen(selection, "Client_ID", scenario.mNewRotation);

		// Model_Phosphorus
		//Model_Phosphorus PH_D = new Model_Phosphorus();
		//JsonNode SendBackPHD = PH_D.Phosphorus(request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Phosphorus
		//Model_Phosphorus PH_T = new Model_Phosphorus();
		//JsonNode SendBackPHT = PH_T.Phosphorus(request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//PH_T.Phosphorus(selection, "Client_ID", scenario.mNewRotation);
		
		// Model_Pest_Suppression
		//Model_Pest_Suppression PS_D = new Model_Pest_Suppression();
		//JsonNode SendBackPSD = PS_D.Pest_Suppression(request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Pest_Suppression
		//Model_Pest_Suppression PS_T = new Model_Pest_Suppression();
		//JsonNode SendBackPST = PS_T.Pest_Suppression(request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//PS_T.Pest_Suppression(selection, "Client_ID", scenario.mNewRotation);
		
		// Model_Pollinator
		//Model_Pollinator PO_D = new Model_Pollinator();
		//JsonNode SendBackPOD = PO_D.Pollinator(request().body().asJson(), selection, "Default", Rotation);
		
		// Model_Pollinator
		//Model_Pollinator PO_T = new Model_Pollinator();
		//JsonNode SendBackPOT = PO_T.Pollinator(request().body().asJson(), selection, "Client_ID", scenario.mNewRotation);
		//PO_T.Pollinator(selection, "Client_ID", scenario.mNewRotation);
		
		
		
		// Calculate Min and Max
		// float[] Max_Min_Ethanol = Max_Min("Ethanol", selection);
		// float[] Max_Min_Net_Energy = Max_Min("Net_Energy", selection);
		// float[] Max_Min_Net_Income = Max_Min("Net_Income", selection);
		// float[] Max_Min_Habitat_Index = Max_Min("Habitat_Index", selection);
		// float[] Max_Min_Nitrogen = Max_Min("Nitrogen", selection);
		// float[] Max_Min_Phosphorus = Max_Min("Phosphorus", selection);
		// float[] Max_Min_Pest_Suppression = Max_Min("Pest_Suppression", selection);
		// float[] Max_Min_Pollinator = Max_Min("Pollinator", selection);
		
		
		
		// Calculate Bins
		// JsonNode Ethanol = Bins(Max_Min_Ethanol, "Ethanol", selection);
		// JsonNode Net_Energy = Bins(Max_Min_Net_Energy, "Net_Energy", selection);
		// JsonNode Net_Income = Bins(Max_Min_Net_Income, "Net_Income", selection);
		// JsonNode Habitat_Index = Bins(Max_Min_Habitat_Index, "Habitat_Index", selection);
		// JsonNode Nitrogen = Bins(Max_Min_Nitrogen, "Nitrogen", selection);
		// JsonNode Phosphorus = Bins(Max_Min_Phosphorus, "Phosphorus", selection);
		// JsonNode Pest_Suppression = Bins(Max_Min_Pest_Suppression, "Pest_Suppression", selection);
		// JsonNode Pollinator = Bins(Max_Min_Pollinator, "Pollinator", selection);
		
		
		
		// SendBack to Client
		//ObjectNode SendBack = JsonNodeFactory.instance.objectNode();
		
		// SendBack.put("Ethanol", Ethanol);
		// SendBack.put("Net_Energy", Net_Energy);
		// SendBack.put("Net_Income", Net_Income);
		// SendBack.put("Habitat_Index", Habitat_Index);
		// SendBack.put("Nitrogen", Nitrogen);
		// SendBack.put("Phosphorus", Phosphorus);
		// SendBack.put("Pest_Suppression", Pest_Suppression);
		// SendBack.put("Pollinator", Pollinator);
		
		//ObjectNode SendBack = JsonNodeFactory.instance.objectNode();
		
		//SendBack.put("Ethanol_D", SendBackED);
		//SendBack.put("Ethanol_T", SendBackET);
		//SendBack.put("Net_Energy_D", SendBackNED);
		//SendBack.put("Net_Energy_T", SendBackNET);
		//SendBack.put("Net_Income_D", SendBackNID);
		//SendBack.put("Net_Income_T", SendBackNIT);
		//SendBack.put("Habitat_Index_D", SendBackHID);
		//SendBack.put("Habitat_Index_T", SendBackHIT);
		//SendBack.put("Nitrogen_D", SendBackND);
		//SendBack.put("Nitrogen_T", SendBackNT);
		//SendBack.put("Phosphorus_D", SendBackPHD);
		//SendBack.put("Phosphorus_T", SendBackPHT);
		//SendBack.put("Pest_Suppression_D", SendBackPSD);
		//SendBack.put("Pest_Suppression_T", SendBackPST);
		//SendBack.put("Pollinator_D", SendBackPOD);
		//SendBack.put("Pollinator_T", SendBackPOT);
		
		
		
		// Write Delat Files
		// WriteDelta("Ethanol", selection);
		// WriteDelta("Net_Energy", selection);
		// WriteDelta("Net_Income", selection);
		// WriteDelta("Habitat_Index", selection);
		// WriteDelta("Nitrogen", selection);
		// WriteDelta("Phosphorus", selection);
		// WriteDelta("Pest_Suppression", selection);
		// WriteDelta("Pollinator", selection);
		
		
		
		// Run the model with the old rotation....
		Models modelD = new Models();
		JsonNode SendBackD = modelD.modeloutcome(selection, "Default", Rotation);
		
		// Run the model with the new transformed rotation...
		Models modelT = new Models();
		JsonNode SendBackT = modelT.modeloutcome(selection, "Client_ID", scenario.mNewRotation);
		
		// Get some data to send back...
		ObjectNode SendBack = JsonNodeFactory.instance.objectNode();
		
		SendBack.put("Default", SendBackD);
		SendBack.put("Transform", SendBackT);
		
		// 
		// WriteDelta("Bird_Index", selection, model);
		// WriteDelta("Nitrogen", selection, model);
		// WriteDelta("Phosphorus", selection, model);
		// WriteDelta("Pest", selection, model);
		// WriteDelta("Pollinator", selection, model);

		return ok(SendBack);
	}
	
	//----------------------------------------------------------------------
	public static void WriteDelta(String InputFile, Selection selection)
	{
		// Open a file to write Delta for HI
		int w = selection.mWidth;
		int h = selection.mHeight;
		int NO_DATA = -9999;
		
		try
		{
			BufferedReader br1 = new HeaderRead(InputFile, w, h, "Default").getReader();
			BufferedReader br2 = new HeaderRead(InputFile, w, h, "Client_ID").getReader();
			
			File output = new File("./layerData/Client_ID/Delta");
			if(output.exists()) 
			{
				Logger.info("The Delta folder is already exist");
			}
			else 
			{
				output.mkdir();
			}
	
			// Buffer writer
			PrintWriter out1 = new HeaderWrite("Delta_" + InputFile, w, h, "Client_ID/Delta").getWriter();
			
			int y = 0;
		
			while (br1.ready() && br2.ready()) 
			{
				StringBuffer sb1 = new StringBuffer();
				
				if (y >= h) 
				{
					Logger.error("BAD READ - more lines than expected!");
					break;
				}
				
				String line1 = br1.readLine();
				String split1[] = line1.split("\\s+");
				String line2 = br2.readLine();
				String split2[] = line2.split("\\s+");
				
				for (int x = 0; x < split1.length; x++) 
				{	
					//if (Integer.valueOf(split1[x]) == (int)NO_DATA) 
					if (selection.mSelection[y][x] == 0)
					{
						sb1.append(Integer.toString(NO_DATA));
					}
					else
					{
						// Compare Default with After Transformation
						float Delta = Float.parseFloat(split1[x]) - Float.parseFloat(split2[x]);
						
						sb1.append(String.format("%.4f", Delta));
					}
					if (x != w - 1) 
					{
						sb1.append(" ");
					}
					
				}
				
				out1.println(sb1.toString());
				
				y++;
			}
			br1.close();
			br2.close();
		
			out1.close();
		}
		catch (Exception e)
		{
			Logger.info(e.toString());
		}
	}
	
	//----------------------------------------------------------------------
	public static float[] Max_Min(String InputFile, Selection selection)
	{
		// Open a file to write Delta
		int w = selection.mWidth;
		int h = selection.mHeight;
		int NO_DATA = -9999;
		float Max = -1000f;
		float Min =  1000f;
		float Min_Max[] = new float[2];
		
		try
		{
			BufferedReader br1 = new HeaderRead(InputFile, w, h, "Default").getReader();
			BufferedReader br2 = new HeaderRead(InputFile, w, h, "Client_ID").getReader();
			
			int y = 0;
		
			while (br1.ready() && br2.ready()) 
			{
				StringBuffer sb1 = new StringBuffer();
				
				if (y >= h) 
				{
					Logger.error("BAD READ - more lines than expected!");
					break;
				}
				
				String line1 = br1.readLine();
				String split1[] = line1.split("\\s+");
				String line2 = br2.readLine();
				String split2[] = line2.split("\\s+");
				
				for (int x = 0; x < split1.length; x++) 
				{	
					//if (Integer.valueOf(split1[x]) == (int)NO_DATA) 
					//if (selection.mSelection[y][x] == 0)
					if (Float.parseFloat(split1[x]) != -9999)
					{
						// Min
						if (Float.parseFloat(split1[x]) < Min)
						{
							Min = Float.parseFloat(split1[x]);
						}
						else if (Float.parseFloat(split2[x]) < Min)
						{
							Min = Float.parseFloat(split2[x]);
						}
						// Max
						if (Float.parseFloat(split1[x]) > Max)
						{
							Max = Float.parseFloat(split1[x]);
						}
						else if (Float.parseFloat(split2[x]) > Max)
						{
							Max = Float.parseFloat(split2[x]);
						}
					}
				}
				
				y++;
			}
			br1.close();
			br2.close();
		}
		catch (Exception e)
		{
			Logger.info(e.toString());
		}
		
		Min_Max[0] = Min;
		Min_Max[1] = Max;
		return Min_Max;
	}
	
	//----------------------------------------------------------------------
	public static JsonNode Bins(float[] Array_Min_Max, String InputFile, Selection selection)
	{
		// Open a file to write Delta
		int w = selection.mWidth;
		int h = selection.mHeight;
		int NO_DATA = -9999;
		float Min = Array_Min_Max[0];
		float Max = Array_Min_Max[1];
		int Bin = 10;
		int[] CountBin_D = new int [Bin];
		int[] CountBin_T = new int [Bin];
		float a = 0; float b = 0;
		int Value_A, Value_B;
		int Total_Cells = selection.countSelectedPixels();
		float Total_A = 0; float Total_B = 0;
		int i = 0;
		
		try
		{
			BufferedReader br1 = new HeaderRead(InputFile, w, h, "Default").getReader();
			BufferedReader br2 = new HeaderRead(InputFile, w, h, "Client_ID").getReader();
			
			int y = 0;
		
			while (br1.ready() && br2.ready()) 
			{
				StringBuffer sb1 = new StringBuffer();
				
				if (y >= h) 
				{
					Logger.error("BAD READ - more lines than expected!");
					break;
				}
				
				String line1 = br1.readLine();
				String split1[] = line1.split("\\s+");
				String line2 = br2.readLine();
				String split2[] = line2.split("\\s+");
				
				for (int x = 0; x < split1.length; x++) 
				{
					a = Float.parseFloat(split1[x]);
					b = Float.parseFloat(split2[x]);
					// Calculate Total for Entire Raster
					Total_A = Total_A + a;
					Total_B = Total_B + b;
					//if (Integer.valueOf(split1[x]) == (int)NO_DATA) 
					//if (selection.mSelection[y][x] == 0)
					if (a != -9999 && b != -9999)
					{
							Value_A = (int)((a - Min)/(Max - Min) * (Bin - 1));
							CountBin_D[Value_A]++;
							Value_B = (int)((b - Min)/(Max - Min) * (Bin - 1));
							CountBin_T[Value_B]++;
					}
				}
				
				y++;
			}
			br1.close();
			br2.close();
		}
		catch (Exception e)
		{
			Logger.info(e.toString());
		}
		
		// Data to return to the client	for Default and Transform	
		ObjectNode Obj = JsonNodeFactory.instance.objectNode();
		ObjectNode NodeD = JsonNodeFactory.instance.objectNode();
		ObjectNode NodeT = JsonNodeFactory.instance.objectNode();
		
		// Default
		ArrayNode Default = JsonNodeFactory.instance.arrayNode();
		for (i = 0; i < CountBin_D.length; i++) 
		{
			Default.add(CountBin_D[i]);
		}
		
		// Average of Default
		float Average_A = Total_A / Total_Cells;
		
		NodeD.put("Result", Default);
		NodeD.put("Min", Min);
		NodeD.put("Max", Max);
		NodeD.put("Average", Average_A);
		
		// Transform
		ArrayNode Transform = JsonNodeFactory.instance.arrayNode();
		for (i = 0; i < CountBin_T.length; i++) 
		{
			Transform.add(CountBin_T[i]);
		}
		
		// Average of Transform
		float Average_B = Total_B / Total_Cells;
		
		NodeT.put("Result", Transform);
		NodeT.put("Min", Min);
		NodeT.put("Max", Max);
		NodeT.put("Average", Average_B);
		
		ObjectNode SendBack = JsonNodeFactory.instance.objectNode();
		
		SendBack.put("Default", NodeD);
		SendBack.put("Transform", NodeT);
		
		return SendBack;
	}
}

