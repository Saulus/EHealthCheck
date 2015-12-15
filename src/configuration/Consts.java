package configuration;


/**
Collected constants of general utility.
**/
public final class Consts {
	
	public static final String version = "1.0";
	
	public static final String modelcsv= "all_models.csv";
	public static final String knrcsv= "knrlist.csv";
	public static final String atccsv= "atc.csv";
	public static final String featurecsv= "allfeatures.csv";
	
	
	public static final String targetCol = "TARGET";
	public static final String targetPraevCol = "MEAN";
	public static final String targetAucTrainCol = "AUC_TRAIN";
	public static final String targetAucTestCol = "AUC_TEST";
	public static final String targetIDCol = "MODEL_ID";
	
	public static final String knrKeyCol="KNR";
	public static final String knrValueCol="BEZEICHNUNG";
	
	public static final String atcKeyCol="ATC";
	public static final String atcValueCol="BEZEICHNUNG";
	
	public static final String alterattribute="ALTER";
	public static final String geschlechtattribute="GESCHLECHT";
	public static final String knrattributePrefix="KNR";
	public static final String khattributePrefix="KH_KNR";
	public static final String atcattributePrefix="ATC_";
	
	public static final String knrTargetPrefix="T_KNR";
	public static final String khTargetPrefix="T_KH_KNR";
	public static final String todTargetPrefix="T_TOD";
	
	 /**
 	 * Instantiates a new consts.
 	 */
 	private Consts(){
		    //this prevents even the native class from 
		    //calling this ctor as well :
		    throw new AssertionError();
	 }
}
