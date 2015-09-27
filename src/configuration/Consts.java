package configuration;


/**
Collected constants of general utility.
**/
public final class Consts {
	
	public static final String version = "1.0";
	
	public static final String configproperty= "config.ehcfile";
	public static final String knrCol = "KNR";
	public static final String knrNameCol = "NAME";
	public static final String knrPraevCol = "PRÄVALENZ";
	public static final String knrFeatureCol = "FEATURE_NO";
	
	
	
	 /**
 	 * Instantiates a new consts.
 	 */
 	private Consts(){
		    //this prevents even the native class from 
		    //calling this ctor as well :
		    throw new AssertionError();
	 }
}
