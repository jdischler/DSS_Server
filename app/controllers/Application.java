package controllers;

import util.*;

import java.util.*;
import java.io.*;

import play.*;
import play.mvc.*;
import play.Logger;
import play.cache.*;

import views.html.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;
import javax.xml.bind.DatatypeConverter;

//------------------------------------------------------------------------------
public class Application extends Controller {
	
	//--------------------------------------------------------------------------
	public static Result index() {
		
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception {
		
		Query query = new Query();
		String urlPath = query.exec(request().body().asJson());
		return ok(urlPath);
	}

	//--------------------------------------------------------------------------
	public static Result layerParmRequest() throws Exception {
		
		JsonNode request = request().body().asJson();
		
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		String layername = request.get("name").getTextValue();
		String type = request.get("type").getTextValue();

		Layer_Base layer = Layer_Base.getLayer(layername);
		if (layer != null && type != null) {
			if (type.equals("layerRange")) {
				Layer_Continuous layerCont = (Layer_Continuous)layer;
				if (layerCont == null) {
					throw new Exception();
				}
				ret.put("layerMin", layerCont.getLayerMin());
				ret.put("layerMax", layerCont.getLayerMax());
			}
		}

		return ok(ret);
	}
}