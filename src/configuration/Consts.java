package configuration;


/**
Collected constants of general utility.
**/
public final class Consts {
	
	public static final String version = "1.0";
	
	public static final String modelcsv= "all_models.csv";
	public static final String knrcsv= "knrlist.csv";
	public static final String atccsv= "atc.csv";
	
	
	public static final String targetCol = "target";
	public static final String targetPraevCol = "mean";
	public static final String targetAucTrainCol = "auc_train";
	public static final String targetAucTestCol = "auc_test";
	public static final String targetIDCol = "model_id";
	
	public static final String knrKeyCol="knr";
	public static final String knrValueCol="bezeichnung";
	
	public static final String atcKeyCol="atc";
	public static final String atcValueCol="bezeichnung";
	
	public static final String khattribute="kh";
	
	 /**
 	 * Instantiates a new consts.
 	 */
 	private Consts(){
		    //this prevents even the native class from 
		    //calling this ctor as well :
		    throw new AssertionError();
	 }
}
