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
public class Application extends Controller 
{
	
	//--------------------------------------------------------------------------
	public static Result index() {
		
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception {
		
		Query query = new Query();
		JsonNode result = query.exec(request().body().asJson());
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
	public static Result model() throws Exception 
	{
		
		Logger.info("Server got models request:");
			
//		return ok("http://dss.wei.wisc.edu:9000/app" + partialPath);
		return ok("http://localhost:9000");
	}
}