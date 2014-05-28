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

		// CROP PRICING
		// Create some options that control how Crop prices can be tweaked on the client		
		ObjectNode options = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(options, 1, 10000);
		addOptionalStepSizeProperty(options, 20);
		addOptionalUnitLabelProperty(options, "Pre", "$"); // options are "Pre" and "Post"
		addOptionalHelpTextProperty(options, "Dollars per dry metric ton");
		
		// P - Crop prices into Economic category
		ObjectNode category = createAssumptionCategory("Economic", "economic_icon.png");
		createAssumption(category,	"p_corn", 		"Corn Price", 			233.0f, options);
		createAssumption(category,	"p_stover", 	"Corn Stover Price", 	50.0f, 	options);
		createAssumption(category,	"p_soy", 		"Soy Price", 			640.0f, options);
		createAssumption(category,	"p_alfalfa", 	"Alfalfa Price", 		200.0f, options);
		createAssumption(category,	"p_grass", 		"Grass Price", 			75.0f, options);

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
