package controllers;

import play.*;
import play.mvc.*;
import java.util.*;
import java.io.*;

public class FileService extends Controller {

       static String path = "./public/dynamicFiles/";
       
       public static Result getFile(String file){
//              File myfile = new File (System.getenv("PWD")+path+file);
              File myfile = new File (path+file);
              return ok(myfile);
       }
}
