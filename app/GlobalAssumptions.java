package util;

import play.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

//------------------------------------------------------------------------------
public class GlobalAssumptions
{
	// Looks like this in memory:
	//	economic: { CategoryName: "someName", CategoryIcon: "someIcon",
	//						{ p_corn: { displayName: "someName", defaultValue: "someValue"},
	//							p_other: { ....
	private static ObjectNode mHierarchicalAssumptions;
	
	private ObjectNode mClientSentAssumptions;

	// FIXME: TODO: read from ./serverData/assumptions/assumptions.dat
	// ORDER is: variableName, Category, icon, displayName, defaultValue
	//--------------------------------------------------------------------------
	public static void initAssumptions() 
	{
		mHierarchicalAssumptions = JsonNodeFactory.instance.objectNode();

		// Net Income Model
		// Create some options that control how Crop prices can be tweaked on the client		
		ObjectNode options = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(options, 1, 10000);
		addOptionalStepSizeProperty(options, 20);
		addOptionalUnitLabelProperty(options, "Pre", "$"); // options are "Pre" and "Post"
		addOptionalHelpTextProperty(options, "Sell price per dry metric ton");
		
		// P - Crop prices into Net Income Model
		ObjectNode category = createAssumptionCategory("Net Income Model", "economic_icon.png");
		// Sell -> Price per tonne
		createAssumption(category,	"p_corn_s", 	"Corn Price", 			233.0f, options);
		createAssumption(category,	"p_stover_s", 	"Corn Stover Price", 	50.0f, 	options);
		createAssumption(category,	"p_soy_s", 		"Soy Price", 			640.0f, options);
		createAssumption(category,	"p_alfalfa_s", 	"Alfalfa Price", 		200.0f, options);
		createAssumption(category,	"p_grass_s", 	"Grass Price", 			75.0f, options);
		
		options = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(options, 1, 10000);
		addOptionalStepSizeProperty(options, 20);
		addOptionalUnitLabelProperty(options, "Pre", "$"); // options are "Pre" and "Post"
		addOptionalHelpTextProperty(options, "Production price per dry metric Ha");
		
		// Cost -> $ per ha cost
		createAssumption(category,	"p_corn_p", 	"Corn Production Cost", 		1135.0f, options);
		createAssumption(category,	"p_stover_p", 	"Corn Stover Production Cost", 	412.0f, options);
		createAssumption(category,	"p_soy_p", 		"Soy Production Cost", 			412.0f, options);
		createAssumption(category,	"p_alfalfa_p", 	"Alfalfa Production Cost", 		627.0f, options);
		createAssumption(category,	"p_grass_p", 	"Grass Production Cost", 		620.0f, options);
		
		// Net Energy Model
		// Create some options that control how input and output can be tweaked on the client		
		options = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(options, 1, 10000);
		addOptionalStepSizeProperty(options, 20);
		addOptionalUnitLabelProperty(options, "Pre", ""); // options are "Pre" and "Post"
		addOptionalHelpTextProperty(options, "Net Energy Input at Farm metric MJ/Ha");
		
		// Energy Input at Farm
		category = createAssumptionCategory("Net Energy Model", "energy_icon.png", false);
		createAssumption(category,	"e_corn", 		"Corn Energy at Farm", 			18151.0f, options);
		createAssumption(category,	"e_stover", 	"Corn Stover Energy at Farm", 	2121.0f, 	options);
		createAssumption(category,	"e_soy", 		"Soy Energy at Farm", 			6096.0f, options);
		createAssumption(category,	"e_alfalfa", 	"Alfalfa Energy at Farm", 		9075.0f, options);
		createAssumption(category,	"e_grass", 		"Grass Energy at Farm", 		7411.0f, options);
		
		//  Conversion Efficiency (L per Mg)
		options = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(options, 1, 10000);
		addOptionalStepSizeProperty(options, 10);
		addOptionalUnitLabelProperty(options, "Pre", ""); // options are "Pre" and "Post"
		addOptionalHelpTextProperty(options, "Conversion Efficiency metric L/Mg");
		
		// Cost -> $ per ha cost
		createAssumption(category,	"e_corn_ce", 		"Corn Conversion Efficiency", 		400.0f, options);
		createAssumption(category,	"e_stover_ce", 	"Corn Stover Conversion Efficiency", 	380.0f, options);
		createAssumption(category,	"e_soy_ce", 		"Soy Conversion Efficiency", 		200.0f, options);
		createAssumption(category,	"e_alfalfa_ce", 	"Alfalfa Conversion Efficiency", 	380.0f, options);
		createAssumption(category,	"e_grass_ce", 		"Grass Conversion Efficiency", 		380.0f, options);
		
		
		// YIELD MANIPULATION
		options = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(options, -99, 100);
		addOptionalStepSizeProperty(options, 5);
		addOptionalUnitLabelProperty(options, "Post", "%"); // options are "Pre" and "Post"
		addOptionalHelpTextProperty(options, "Yield modification as a perfcentage. E.g. 5 is a 5% increase in yield.");
		
		// YM - Yield modification/multiplier
		category = createAssumptionCategory("Yield Modification", "percent_icon.png", false); // false means closed
		createAssumption(category,	"ym_corn", 		"Corn Yield", 		0.0f,	options);
		createAssumption(category,	"ym_soy", 		"Soy Yield", 		0.0f,	options);
		createAssumption(category,	"ym_alfalfa", 	"Alfalfa Yield", 	0.0f,	options);
		createAssumption(category,	"ym_grass", 	"Grass Yield", 		0.0f,	options);
				
		ObjectNode multiplierOptions = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(multiplierOptions, 0, 2);
		addOptionalStepSizeProperty(multiplierOptions, 0.1f);
		addOptionalUnitLabelProperty(multiplierOptions, "Pre", "x"); // options are "Pre" and "Post"
		
		// SOIL LOSS MODEL Multipliers
		category = createAssumptionCategory("Soil Loss Model", "down.png", false); // false means closed category
		createAssumption(category,	"sl_t_annuals_C1", 	"Till - Annual Crops", 		0.12f,	multiplierOptions);
		createAssumption(category,	"sl_Contouring_P1", 	"With Contouring", 		0.50f,	multiplierOptions);
		createAssumption(category,	"sl_Terrace_P2",	"With Terrace", 0.65f,	multiplierOptions);
		createAssumption(category,	"sl_cc_annuals",	"With Cover Crop - Annual Crops", 0.35f,	multiplierOptions);
		
		// PHOSPHORUS MODEL Multipliers
		category = createAssumptionCategory("Phosphorus Model", "down.png", false); // false means closed category
		createAssumption(category,	"p_m_annuals",		"Manure - Annual Crops",		1.03f,	multiplierOptions);
		createAssumption(category,	"p_fm_annuals",		"Fall Manure - Annual Crops",	1.06f,	multiplierOptions);
		createAssumption(category,	"p_t_annuals",		"Till - Annual Crops",		1.35f,	multiplierOptions);
		createAssumption(category,	"p_cc_annuals",		"With Cover Crop - Annual Crops",0.55f,	multiplierOptions);
		createAssumption(category,	"p_m_perennials",	"Manure - Perennial Crops",		1.02f,	multiplierOptions);
		createAssumption(category,	"p_fm_perennials",	"Fall Manure - Perennial Crops",1.04f,	multiplierOptions);
		
		// YIELD MODEL Multipliers
		category = createAssumptionCategory("Yield Model", "down.png", false); // false means closed category
		//createAssumption(category,	"y_m_annuals",		"Manure - Annual Crops",		1.05f,	multiplierOptions);
		//createAssumption(category,	"y_fm_annuals",		"Fall Manure - Annual Crops",	1.35f,	multiplierOptions);
		createAssumption(category,	"y_nt_annuals",		"No Till - Annual Crops",		0.90f,	multiplierOptions);
		createAssumption(category,	"y_cc_annuals",		"With Cover Crop - Annual Crops",1.10f,	multiplierOptions);
		//createAssumption(category,	"y_m_perennials",	"Manure - Perennial Crops",		1.02f,	multiplierOptions);
		//createAssumption(category,	"y_fm_perennials",	"Fall Manure - Perennial Crops",1.15f,	multiplierOptions);
		
		// TODO: ADD assumptions to yield model here
		
		// SOC MODEL Multipliers
		category = createAssumptionCategory("Soil Organic Carbon Model", "down.png", false); // false means closed category
		// TODO: ADD assumptions to SOC model here
		createAssumption(category,	"soc_m_annuals",		"Manure - Annual Crops",		1.40f,	multiplierOptions);
		createAssumption(category,	"soc_fm_annuals",		"Fall Manure - Annual Crops",	1.45f,	multiplierOptions);
		// NT increases SOC
		createAssumption(category,	"soc_nt_annuals",		"No Till - Annual Crops",		1.20f,	multiplierOptions);
		createAssumption(category,	"soc_cc_annuals",		"With Cover Crop - Annual Crops",1.05f,	multiplierOptions);
		createAssumption(category,	"soc_m_perennials",	"Manure - Perennial Crops",		1.30f,	multiplierOptions);
		createAssumption(category,	"soc_fm_perennials",	"Fall Manure - Perennial Crops",1.35f,	multiplierOptions);
		
		// N20 MODEL Multipliers
		category = createAssumptionCategory("Nitrous Oxide Model", "down.png", false); // false means closed category
		// TODO: ADD assumptions to N20 model here
		createAssumption(category,	"n_m_annuals",		"Manure - Annual Crops",		1.04f,	multiplierOptions);
		createAssumption(category,	"n_fm_annuals",		"Fall Manure - Annual Crops",	1.08f,	multiplierOptions);
		createAssumption(category,	"n_t_annuals",		"Till - Annual Crops",		1.30f,	multiplierOptions);
		createAssumption(category,	"n_cc_annuals",		"With Cover Crop - Annual Crops",0.90f,	multiplierOptions);
		createAssumption(category,	"n_m_perennials",	"Manure - Perennial Crops",		1.03f,	multiplierOptions);
		createAssumption(category,	"n_fm_perennials",	"Fall Manure - Perennial Crops",1.06f,	multiplierOptions);

		Logger.info(mHierarchicalAssumptions.toString());
	}

	// We put properties into Categories, generally, for user convenience in find things...
	//--------------------------------------------------------------------------
	private static ObjectNode createAssumptionCategory(String categoryName, String icon)
	{
		return createAssumptionCategory(categoryName,icon,true); // default to open
	}

	// We put properties into Categories, generally, for user convenience in find things...
	//--------------------------------------------------------------------------
	private static ObjectNode createAssumptionCategory(String categoryName, String icon, boolean open)
	{
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("CategoryName", categoryName);
		node.put("CategoryIcon", icon);
		node.put("CategoryOpen", open);

		mHierarchicalAssumptions.put(categoryName, node);
		
		return node;		
	}
	
	// These assumptions are properties that go into a category..
	//--------------------------------------------------------------------------
	private static JsonNode createAssumption(ObjectNode category, String lookupName,
								String displayName, float defaultValue, ObjectNode options) 
	{
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("DisplayName", displayName);
		node.put("DefaultValue", defaultValue);

		if (options != null) {
			node.putAll(options);
		}
		
		category.put(lookupName, node);
		return node;		
	}

	// These assumptions are properties that go into a category..
	//--------------------------------------------------------------------------
	private static JsonNode createAssumption(ObjectNode category, String lookupName,
								String displayName, float defaultValue) 
	{
		return createAssumption(category, lookupName, displayName, defaultValue, null);
	}
	
	// Assumption properties can optionally add additional things onto itself to control various
	//	things on the client side, like range validation..
	//--------------------------------------------------------------------------
	private static void addOptionalRangeProperties(ObjectNode properties, float min, float max) {
		properties.put("Min", min);
		properties.put("Max", max);
	}

	//--------------------------------------------------------------------------
	private static void addOptionalStepSizeProperty(ObjectNode properties, float stepSize) {
		properties.put("StepSize", stepSize);
	}
	
	// Type can be "Pre" or "Post"
	//--------------------------------------------------------------------------
	private static void addOptionalUnitLabelProperty(ObjectNode properties, String type, String label) {
		properties.put( type + "Label", label);
	}

	//--------------------------------------------------------------------------
	private static void addOptionalHelpTextProperty(ObjectNode properties, String helpText) {
		properties.put("HelpText", helpText);
	}
	
	
	
	// Working with Hierarchical values is easier for the client...
	//--------------------------------------------------------------------------
	public static ObjectNode getAssumptionDefaultsForClient() {
		
		return mHierarchicalAssumptions;
	}
	
	//--------------------------------------------------------------------------
	public GlobalAssumptions() {
		
		// set to defaults...but can override with client values...
		mClientSentAssumptions = mHierarchicalAssumptions;
	}
	
	//--------------------------------------------------------------------------
	public void setAssumptionsFromClient(JsonNode clientSend) throws Exception {
		
		JsonNode res = clientSend.get("assumptions");
		if (res == null) {
			throw new Exception();
		}
		else if (!res.isObject()) {
			throw new Exception();
		}
		mClientSentAssumptions = (ObjectNode)res;
		Logger.info(res.toString());
	}
	
	//--------------------------------------------------------------------------
	public float getAssumptionFloat(String variableName) throws Exception  {
		
		if (mClientSentAssumptions == null) {
			throw new Exception();
		}

		JsonNode res = mClientSentAssumptions.findValue(variableName);
		if (res == null) {
			throw new Exception();
		}
		if (!res.isObject()) {
			throw new Exception();
		}

		res = ((ObjectNode)res).get("DefaultValue");
		if (!res.isNumber()) {
			throw new Exception();
		}
		
		return (float)res.doubleValue();
	}

	//--------------------------------------------------------------------------
	public int getAssumptionInt(String variableName) throws Exception  {
		
		if (mClientSentAssumptions == null) {
			throw new Exception();
		}

		JsonNode res = mClientSentAssumptions.findValue(variableName);
		if (res == null) {
			throw new Exception();
		}
		if (!res.isObject()) {
			throw new Exception();
		}

		res = ((ObjectNode)res).get("DefaultValue");
		if (!res.isNumber()) {
			throw new Exception();
		}
		
		return res.intValue();
	}
	
	//--------------------------------------------------------------------------
	public String getAssumptionString(String variableName) throws Exception  {
		
		if (mClientSentAssumptions == null) {
			throw new Exception();
		}

		JsonNode res = mClientSentAssumptions.findValue(variableName);
		if (res == null) {
			throw new Exception();
		}
		if (!res.isObject()) {
			throw new Exception();
		}

		res = ((ObjectNode)res).get("DefaultValue");
		if (!res.isTextual()) {
			throw new Exception();
		}
		
		return res.textValue();
	}
}		


/*
{
	"Economic":
	{
		"CategoryName":"Economic",
		"CategoryIcon":"economic_icon.png",
		"p_corn":
			{"DisplayName":"Corn Price","DefaultValue":10},
		"p_stover":
			{"DisplayName":"Corn Stover Price","DefaultValue":20},
		"p_soy":
			{"DisplayName":"Soy Price","DefaultValue":30},
		"p_alfalfa":
			{"DisplayName":"Alfalfa Price","DefaultValue":40},
		"p_grass":
			{"DisplayName":"Grass Price","DefaultValue":50}
	},
	"Yield Modification":
	{
		"CategoryName":"Yield Modification",
		"CategoryIcon":"economic_icon.png",
		"ym_corn":
			{"DisplayName":"Alfalfa Yield","DefaultValue":1},
		"ym_soy":
			{"DisplayName":"Soy Yield","DefaultValue":2},
		"ym_alfalfa":
			{"DisplayName":"Alfalfa Yield","DefaultValue":3},
		"ym_grass":
			{"DisplayName":"Grass Yield","DefaultValue":4}
	}
}*/
