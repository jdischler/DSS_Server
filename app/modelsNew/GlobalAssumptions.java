package util;

import play.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class GlobalAssumptions
{
	// List of all the assumptions the server knows about, in Json format
	// NOTE: don't confuse the global defaults that are passed to the client with
	//	the version that comes back from the client...
	private static ObjectNode mKnownAssumptions;
	
	private ObjectNode mClientSentAssumptions;
	
	//--------------------------------------------------------------------------
	private static JsonNode createAssumption(String category, String icon,
						String displayName, float defaultValue) 
	{

		ObjectNode node = JsonNodeFactory.instance.objectNode();
		node.put("Category", category);
		node.put("Icon", icon);
		node.put("DisplayName", displayName);
		node.put("DefaultValue", defaultValue);

		return node;		
	}
	
	// FIXME: TODO: read from ./serverData/assumptions/assumptions.dat
	// ORDER is: variableName, Category, icon, displayName, defaultValue
	//--------------------------------------------------------------------------
	public static void initAssumptions() 
	{
		ObjectNode results = JsonNodeFactory.instance.objectNode();
		
		// NOTE: these are ALL temp and placeholder...rename them, change values, etc!
		// NOTE: only one icon definition is probably needed per category? double check...
		// NOTE: we also put based on the variable name to make lookups trivial in later code...
		results.put("p_corn", createAssumption("Economic", "", "Corn Price", 274.0f));
		results.put("p_stover", createAssumption("Economic", "", "Stover Price", 70.0f));
		results.put("p_grass", createAssumption("Economic", "", "Grass Price", 107.0f));
		results.put("p_soy", createAssumption("Economic", "", "Soy Price", 249.0f));
		results.put("p_alfalfa", createAssumption("Economic", "economic_icon.png", "Alfalfa Price", 230.0f));
		//results.put("p_eth", createAssumption("Economic", "economic_icon.png", "Ethanol Price", 300.0f));
		
		//results.put("conv_corn", createAssumption("Conversion Rates", "", "Corn Grain", 1.8f));
		//results.put("conv_stover", createAssumption("Conversion Rates", "", "Stover", 1.4f));
		//results.put("conv_grass", createAssumption("Conversion Rates", "scenario_icon.png", "Grass", 1.2f));
		
		//results.put("av_temp", createAssumption("Climate", "", "Average Temperature", 76.1f));
		//results.put("av_rain", createAssumption("Climate", "climate_icon.png", "Average Rainfall", 19.2f));
		
		//results.put("temp", createAssumption("Other", "policy_icon.png", "Dunno", 1));

		mKnownAssumptions = results;
	}
	
	//--------------------------------------------------------------------------
	public static ObjectNode getAssumptionDefaultsForClient() {
		
		return mKnownAssumptions;
	}
	
	//--------------------------------------------------------------------------
	public GlobalAssumptions() {
		
		// set to defaults...but can override with client values...
		mClientSentAssumptions = mKnownAssumptions;
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
	}
	
	//--------------------------------------------------------------------------
	public float getAssumptionFloat(String variableName) throws Exception  {
		
		if (mClientSentAssumptions == null) {
			throw new Exception();
		}

		JsonNode res = mClientSentAssumptions.get(variableName);
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
		
		return (float)res.getDoubleValue();
	}

	//--------------------------------------------------------------------------
	public int getAssumptionInt(String variableName) throws Exception  {
		
		if (mClientSentAssumptions == null) {
			throw new Exception();
		}

		JsonNode res = mClientSentAssumptions.get(variableName);
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
		
		return res.getIntValue();
	}
	
	//--------------------------------------------------------------------------
	public String getAssumptionString(String variableName) throws Exception  {
		
		if (mClientSentAssumptions == null) {
			throw new Exception();
		}

		JsonNode res = mClientSentAssumptions.get(variableName);
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
		
		return res.getTextValue();
	}
}		

