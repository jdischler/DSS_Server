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
		addOptionalHelpTextProperty(options, "Dollar amount in bushels per acre"); // TODO:validate
		
		// P - Crop prices into Economic category
		ObjectNode category = createAssumptionCategory("Economic", "economic_icon.png");
		createAssumption(category,	"p_corn", 		"Corn Price", 			200.0f, options);
		createAssumption(category,	"p_stover", 	"Corn Stover Price", 	80.0f, 	options);
		createAssumption(category,	"p_soy", 		"Soy Price", 			450.0f, options);
		createAssumption(category,	"p_alfalfa", 	"Alfalfa Price", 		200.0f, options);
		createAssumption(category,	"p_grass", 		"Grass Price", 			100.0f, options);

		// YIELD MANIPULATION
		options = JsonNodeFactory.instance.objectNode();
		addOptionalRangeProperties(options, -99, 100);
		addOptionalStepSizeProperty(options, 5);
		addOptionalUnitLabelProperty(options, "Post", "%"); // options are "Pre" and "Post"
		addOptionalHelpTextProperty(options, "Yield modification as a perfcentage. E.g. 5 is a 5% increase in yield.");
		
		// YM - Yield modification/multiplier
		category = createAssumptionCategory("Yield Modification", "percent_icon.png");
		createAssumption(category,	"ym_corn", 		"Corn Yield", 		0.0f,	options);
		createAssumption(category,	"ym_soy", 		"Soy Yield", 		0.0f,	options);
		createAssumption(category,	"ym_alfalfa", 	"Alfalfa Yield", 	0.0f,	options);
		createAssumption(category,	"ym_grass", 	"Grass Yield", 		0.0f,	options);
		
		Logger.info(mHierarchicalAssumptions.toString());
	}

	// We put properties into Categories, generally, for user convenience in find things...
	//--------------------------------------------------------------------------
	private static ObjectNode createAssumptionCategory(String categoryName, String icon)
	{
		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("CategoryName", categoryName);
		node.put("CategoryIcon", icon);

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
