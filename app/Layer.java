package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public interface Layer
{
	public void 	loadASC() throws Exception;
	public int[][] 	query(JsonNode queryNode, int[][] workArray);
}
	

