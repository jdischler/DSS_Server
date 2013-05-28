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
	
	static int counter;
	// FIXME: not thread safe in the sense that multiple incoming queries will 'fight' with each other
	static int[][] mImgArray;
	static int mWidth, mHeight;
	
	//--------------------------------------------------------------------------
	public static Result index() {
		
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception {
		
		Query query = new Query();
		String urlPath = query.exec(request().body().asJson());
		
//		return ok("http://dss.wei.wisc.edu:9000/app" + partialPath);
		return ok("http://localhost:9000" + urlPath);
	}
}