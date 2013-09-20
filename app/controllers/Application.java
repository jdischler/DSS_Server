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
		
		
long modelTimeStart = System.currentTimeMillis();

		Logger.info("----- Model Process Started ----");
		//Logger.info("Called into Model:");
		//Logger.info(request().body().asJson().toString());
		
		File output = new File("./layerData/Client_ID");
		if(output.exists())
		{
			Logger.info("This folder already exists");
		}
		else
		{
			output.mkdir();
		}
		
		Layer_Base layer;
		int width, height;
		// Rotation
		int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();

		
long timeStart = System.currentTimeMillis();
		
		//int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		// Select the entire landscape before transform
		Selection selectionD = new Selection(width, height);

		// Create a new scenario and get a transformed crop rotation layer from it...
		Scenario scenario = new Scenario();
		scenario.getTransformedRotation(request().body().asJson());
		// Select based on user selection after transform
		Selection selectionT = scenario.mSelection;
		//Logger.info("Server Run The Models Request:");
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Transformation timing: " + Float.toString(timeSec));
		

timeStart = System.currentTimeMillis();
		
		// Model_Soil_Carbon
		// Default and Transform
		Model_Soil_Carbon Model_SOC_T = new Model_Soil_Carbon();
		TwoArrays Array_SOC = Model_SOC_T.Soil_Carbon(selectionD, Rotation, scenario.mNewRotation);
		float SOC_D[] = Array_SOC.a;
		float SOC_T[] = Array_SOC.b;
		float Min_SOC_D = Array_SOC.Min_a;
		float Max_SOC_D = Array_SOC.Max_a;
		float Min_SOC_T = Array_SOC.Min_b;
		float Max_SOC_T = Array_SOC.Max_b;
		
		// Selection for default
		Model_Selection SOCD = new Model_Selection();
		JsonNode SendBack_SOCD = SOCD.Selection(selectionD, selectionT, SOC_D);
		// Selection for transform
		Model_Selection SOCT = new Model_Selection();
		JsonNode SendBack_SOCT = SOCT.Selection(selectionD, selectionT, SOC_T);
		
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Soil Carbon timing:  " + Float.toString(timeSec));


timeStart = System.currentTimeMillis();
		// Model_Nitrous_Oxide_Emissions
		// Default
		Model_Nitrous_Oxide_Emissions NOED = new Model_Nitrous_Oxide_Emissions();
		float ArrayNOE_D[] = NOED.Nitrous_Oxide_Emissions(selectionD, Rotation);
		Model_Selection NOE_D = new Model_Selection();
		JsonNode SendBack_NOED = NOE_D.Selection(selectionD, selectionT, ArrayNOE_D);
		ArrayNOE_D = null;
		
		// Transform
		Model_Nitrous_Oxide_Emissions NOET = new Model_Nitrous_Oxide_Emissions();
		float ArrayNOE_T[] = NOET.Nitrous_Oxide_Emissions(selectionD, scenario.mNewRotation);
		Model_Selection NOE_T = new Model_Selection();
		JsonNode SendBack_NOET = NOE_T.Selection(selectionD, selectionT, ArrayNOE_T);
		ArrayNOE_T = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model Nitrous Oxide Emissions timing: " + Float.toString(timeSec));


timeStart = System.currentTimeMillis();
		// Corn and Grass Production for D
		Model_Crop_Yield Model_CGD = new Model_Crop_Yield();
		float YID[] = Model_CGD.Crop_Y(selectionD, Rotation);
		
		// Regular Models
		// Default
		Model_Ethanol_Net_Energy_Income ENENID = new Model_Ethanol_Net_Energy_Income();
		ThreeArrays ArrayD = ENENID.Ethanol_Net_Energy_Income(YID, selectionD, Rotation);
		float ArrayED[] = ArrayD.a;
		float ArrayNED[] = ArrayD.b;
		float ArrayNID[] = ArrayD.c_float;
		float Min_ED = ArrayD.Min_a;
		float Max_ED = ArrayD.Max_a;
		float Min_NED = ArrayD.Min_b;
		float Max_NED = ArrayD.Max_b;
		float Min_NID = ArrayD.Min_c;
		float Max_NID = ArrayD.Max_c;
		
		Model_Selection ED = new Model_Selection();
		JsonNode SendBack_ED = ED.Selection(selectionD, selectionT, ArrayED);
		Model_Selection NED = new Model_Selection();
		JsonNode SendBack_NED = NED.Selection(selectionD, selectionT, ArrayNED);
		Model_Selection NID = new Model_Selection();
		JsonNode SendBack_NID = NID.Selection(selectionD, selectionT, ArrayNID);
		ArrayD = null;
		ArrayNID = null;
		ArrayED = null;
		ArrayNED = null;
		YID = null;
		System.gc();
		
		// Corn and Grass Production for T
		Model_Crop_Yield Model_CGT = new Model_Crop_Yield();
		float YIT[] = Model_CGT.Crop_Y(selectionD, scenario.mNewRotation);
		
		// Transform
		Model_Ethanol_Net_Energy_Income ENENIT = new Model_Ethanol_Net_Energy_Income();
		ThreeArrays ArrayT = ENENIT.Ethanol_Net_Energy_Income(YIT, selectionD, scenario.mNewRotation);
		float ArrayET[] = ArrayT.a;
		float ArrayNET[] = ArrayT.b;
		float ArrayNIT[] = ArrayT.c_float;
		float Min_ET = ArrayT.Min_a;
		float Max_ET = ArrayT.Max_a;
		float Min_NET = ArrayT.Min_b;
		float Max_NET = ArrayT.Max_b;
		float Min_NIT = ArrayT.Min_c;
		float Max_NIT = ArrayT.Max_c;
		
		Model_Selection ET = new Model_Selection();
		JsonNode SendBack_ET = ET.Selection(selectionD, selectionT, ArrayET);
		Model_Selection NET = new Model_Selection();
		JsonNode SendBack_NET = NET.Selection(selectionD, selectionT, ArrayNET);
		Model_Selection NIT = new Model_Selection();
		JsonNode SendBack_NIT = NIT.Selection(selectionD, selectionT, ArrayNIT);
		ArrayT = null;
		ArrayET = null;
		ArrayNET = null;
		ArrayNIT = null;
		YIT = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Ethanol / Energy / Income timing: " + Float.toString(timeSec));
		
		
timeStart = System.currentTimeMillis();
		// Model_Habitat_Index
		// Default
		Model_Habitat_Index HID = new Model_Habitat_Index();
		float ArrayHI_D[] = HID.Habitat_Index(selectionD, Rotation);
		Model_Selection HIDS = new Model_Selection();
		JsonNode SendBack_HID = HIDS.Selection(selectionD, selectionT, ArrayHI_D);
		ArrayHI_D = null;
		
		// Transform
		Model_Habitat_Index HIT = new Model_Habitat_Index();
		float ArrayHI_T[] = HIT.Habitat_Index(selectionD, scenario.mNewRotation);
		Model_Selection HITS = new Model_Selection();
		JsonNode SendBack_HIT = HITS.Selection(selectionD, selectionT, ArrayHI_T);
		ArrayHI_T = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Habitat Index timing: " + Float.toString(timeSec));
		
		
timeStart = System.currentTimeMillis();
		// Models at watershed scale for Nitrogen and Phosphorus
		// Default
		Model_Nitrogen_Phosphorus N_P_D = new Model_Nitrogen_Phosphorus();
		ThreeArrays ArrayN_P_D = N_P_D.Nitrogen_Phosphorus(selectionD, Rotation);
		float ArrayND[] = ArrayN_P_D.a;
		float ArrayPHD[] = ArrayN_P_D.b;
		int TotalD[] = ArrayN_P_D.c_int;
		float Min_ND = ArrayN_P_D.Min_a;
		float Max_ND = ArrayN_P_D.Max_a;
		float Min_PHD = ArrayN_P_D.Min_b;
		float Max_PHD = ArrayN_P_D.Max_b;
		float Min_TD = ArrayN_P_D.Min_c;
		float Max_TD = ArrayN_P_D.Max_c;
		
		Model_Selection_N_P ND = new Model_Selection_N_P();
		JsonNode SendBack_ND = ND.Selection_N_P(ArrayND, TotalD);
		Model_Selection_N_P PHD = new Model_Selection_N_P();
		JsonNode SendBack_PHD = PHD.Selection_N_P(ArrayPHD, TotalD);
		ArrayN_P_D = null;
		ArrayND = null;
		ArrayPHD = null;
		TotalD = null;
		System.gc();
		
		// Transform
		Model_Nitrogen_Phosphorus N_P_T = new Model_Nitrogen_Phosphorus();
		ThreeArrays ArrayN_P_T = N_P_T.Nitrogen_Phosphorus(selectionD, scenario.mNewRotation);
		float ArrayNT[] = ArrayN_P_T.a;
		float ArrayPHT[] = ArrayN_P_T.b;
		int TotalT[] = ArrayN_P_T.c_int;
		float Min_NT = ArrayN_P_T.Min_a;
		float Max_NT = ArrayN_P_T.Max_a;
		float Min_PHT = ArrayN_P_T.Min_b;
		float Max_PHT = ArrayN_P_T.Max_b;
		float Min_TT = ArrayN_P_T.Min_c;
		float Max_TT = ArrayN_P_T.Max_c;
		
		Model_Selection_N_P NT = new Model_Selection_N_P();
		JsonNode SendBack_NT = NT.Selection_N_P(ArrayNT, TotalT);
		Model_Selection_N_P PHT = new Model_Selection_N_P();
		JsonNode SendBack_PHT = PHT.Selection_N_P(ArrayPHT, TotalT);
		ArrayN_P_T = null;
		ArrayNT = null;
		ArrayPHT = null;
		TotalT = null;
		System.gc();
timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Nitrogen / Phosphorus timing: " + Float.toString(timeSec));
		

timeStart = System.currentTimeMillis();
		// Model_Pollinator_Pest_Suppression
		// Default
		Model_Pollinator_Pest_Suppression PPS_D = new Model_Pollinator_Pest_Suppression();
		TwoArrays ArrayP_PS_D = PPS_D.Pollinator_Pest_Suppression(selectionD, Rotation);
		float ArrayPOD[] = ArrayP_PS_D.a;
		float ArrayPSD[] = ArrayP_PS_D.b;
		float Min_POD = ArrayP_PS_D.Min_a;
		float Max_POD = ArrayP_PS_D.Max_a;
		float Min_PSD = ArrayP_PS_D.Min_b;
		float Max_PSD = ArrayP_PS_D.Max_b;
		
		Model_Selection POD = new Model_Selection();
		JsonNode SendBack_POD = POD.Selection(selectionD, selectionT, ArrayPOD);
		Model_Selection PSD = new Model_Selection();
		JsonNode SendBack_PSD = PSD.Selection(selectionD, selectionT, ArrayPSD);
		ArrayP_PS_D = null;
		ArrayPOD = null;
		ArrayPSD = null;
		System.gc();
		
		// Transform
		Model_Pollinator_Pest_Suppression PPS_T = new Model_Pollinator_Pest_Suppression();
		TwoArrays ArrayP_PS_T = PPS_T.Pollinator_Pest_Suppression(selectionD, scenario.mNewRotation);
		float ArrayPOT[] = ArrayP_PS_T.a;
		float ArrayPST[] = ArrayP_PS_T.b;
		float Min_POT = ArrayP_PS_T.Min_a;
		float Max_POT = ArrayP_PS_T.Max_a;
		float Min_PST = ArrayP_PS_T.Min_b;
		float Max_PST = ArrayP_PS_T.Max_b;
		
		Model_Selection POT = new Model_Selection();
		JsonNode SendBack_POT = POT.Selection(selectionD, selectionT, ArrayPOT);
		Model_Selection PST = new Model_Selection();
		JsonNode SendBack_PST = PST.Selection(selectionD, selectionT, ArrayPST);
		ArrayP_PS_T = null;
		ArrayPOT = null;
		ArrayPSD = null;
		System.gc();

timeEnd = System.currentTimeMillis();
timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model - Pollinator / Pest timing: " + Float.toString(timeSec));


		// SendBack to Client
		ObjectNode SendBack  = JsonNodeFactory.instance.objectNode();
		ObjectNode SendBackD = JsonNodeFactory.instance.objectNode();
		ObjectNode SendBackT = JsonNodeFactory.instance.objectNode();
		
		SendBackD.put("Ethanol", SendBack_ED);
		SendBackT.put("Ethanol", SendBack_ET);
		SendBackD.put("Net_Energy", SendBack_NED);
		SendBackT.put("Net_Energy", SendBack_NET);
		SendBackD.put("Net_Income", SendBack_NID);
		SendBackT.put("Net_Income", SendBack_NIT);
		SendBackD.put("Habitat_Index", SendBack_HID);
		SendBackT.put("Habitat_Index", SendBack_HIT);
		SendBackD.put("Nitrogen", SendBack_ND);
		SendBackT.put("Nitrogen", SendBack_NT);
		SendBackD.put("Phosphorus", SendBack_PHD);
		SendBackT.put("Phosphorus", SendBack_PHT);
		SendBackD.put("Pest_Suppression", SendBack_PSD);
		SendBackT.put("Pest_Suppression", SendBack_PST);
		SendBackD.put("Pollinator", SendBack_POD);
		SendBackT.put("Pollinator", SendBack_POT);
		SendBackD.put("Soil_Carbon", SendBack_SOCD);
		SendBackT.put("Soil_Carbon", SendBack_SOCT);
		SendBackD.put("Nitrous_Oxide_Emissions", SendBack_NOED);
		SendBackT.put("Nitrous_Oxide_Emissions", SendBack_NOET);
		
		SendBack.put(  "Default", SendBackD);
		SendBack.put("Transform", SendBackT);
		
		Logger.info(SendBackD.toString());
		Logger.info(SendBackT.toString());
		
long modelTimeEnd = System.currentTimeMillis();
timeSec = (modelTimeEnd - modelTimeStart) / 1000.0f;
Logger.info("---- Model Process Finished ----");
Logger.info("   Model total time: " + Float.toString(timeSec) + "s");
		
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

