package util;
	
public class ThreeArrays  
{  
	  public float[] a;  
	  public float[] b;  
	  public float[] c_float;  
	  public int[] c_int;
	  public float Min_a, Max_a;
	  public float Min_b, Max_b;
	  public float Min_c, Max_c;
	  
	  
	  public ThreeArrays(float[] a, float[] b, float[] c_float, float Min_a, float Max_a, float Min_b, float Max_b, float Min_c, float Max_c)  
	  {  
	    this.a = a; 
	    this.b = b;  
	    this.c_float = c_float; 
	    this.Min_a = Min_a;
	    this.Max_a = Max_a;
	    this.Min_b = Min_b;
	    this.Max_b = Max_b;
	    this.Min_c = Min_c;
	    this.Max_c = Max_c;
	  }  
	  
	  public ThreeArrays(float[] a, float[] b, int[] c_int, float Min_a, float Max_a, float Min_b, float Max_b, float Min_c, float Max_c)  
	  {  
	    this.a = a; 
	    this.b = b;  
	    this.c_int = c_int; 
	    this.Min_a = Min_a;
	    this.Max_a = Max_a;
	    this.Min_b = Min_b;
	    this.Max_b = Max_b;
	    this.Min_c = Min_c;
	    this.Max_c = Max_c;
	  } 
}
