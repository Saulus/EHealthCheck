package servlets;

import configuration.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVReader;
import hex.genmodel.easy.RowData;


class RequestSet {
	public String alter;
	public String geschlecht;
	public String[] knr;
	public String[] knr_kh;
	public String[] atc;
	
	public RequestSet() {
		
	}
	
	
}


class ResultSet {
	public double[] risk = {0,0}; //absolute risk: KNR / KH
	public double[] rrisk = {0,0}; //relative risk
	public double[] auc = {0,0};
	public double[] praevalenz = {0,0};
	public boolean isnew = true;
	
	public ResultSet() {	
	}
	
	public void roundMe () {
		for (int i=0; i< risk.length; i++) {
			risk[i] = (double) Math.round(risk[i]*1000)/1000;
			if (rrisk[i] != 0) rrisk[i] = (double) Math.round(rrisk[i]*100-100)/100;
			auc[i] = (double) Math.round(auc[i]*1000)/1000;
			praevalenz[i] = (double) Math.round(praevalenz[i]*1000)/1000;
		}
	}
}

/**
 * Servlet implementation class EHealthCheck
 * 
 * h2o classes go into: /WEB-INF/classes/
 * allmodels.csv goes into: /WEB-INF/allmodels.csv 
 */
@WebServlet("/EHealthCheck")
public class EHealthCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private HashMap<String,Target> targetlist;
	private LinkedHashMap<String,String> knrlist;
	private boolean hasError = false;
	private RowData baserow;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EHealthCheck() {
        super();
        
        String modelfile = Consts.modelcsv;
        
        //Read in Models
        try {
        	modelfile = Init.getWebInfPath() + "/classes/"+modelfile;
        	this.targetlist=getAllTargets(modelfile);
        } catch (Exception e) {
        	System.err.println("Fehler gefunden beim Einlesen der Target-Konfiguration aus Datei " + modelfile);
			e.printStackTrace();
			this.hasError=true;
        }
        
        //Build KNR List for translation
        try {
        	knrlist=Knr.readInList(Knr.myfile,false,true);
        } catch (Exception e) {
        	System.err.println("Fehler gefunden beim Einlesen der Konfiguration aus Datei " + Knr.myfile);
			e.printStackTrace();
			this.hasError=true;
        }
        
        if (!hasError) {
	        //Build Feature List for base row data
        	List<String> featurelist = new ArrayList<String>();
        	try {
				CSVReader reader = new CSVReader(new FileReader(Init.getWebInfPath() + "/"+Consts.featurecsv), ';', '"');
				List<String[]> readIn = reader.readAll();
				reader.close();
				//first line = header-line
				if (readIn.size()>0) readIn.remove(0);
				for (String[] nextline : readIn) 
					if (!nextline[0].isEmpty()) featurelist.add(nextline[0]);
        	} catch (Exception e) {}
        	baserow = buildBaseRow(featurelist);
        }
	        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//expects the following attributes (max):
				//Knr ambulant: knr=(1,2,8), Knr stationär: kh_knr=(9,10), ATC: atc=(AA2BB,AB4GF), alter=57, Geschlecht=2
				response.addHeader("Access-Control-Allow-Origin", "*");
				response.addHeader("Content-Type", "application/json");
				response.setCharacterEncoding("UTF-8");
				
				if (hasError) response.sendError(520, "Init-Fehler");
				else {
					Gson gson = new Gson(); 
					String jsonString = "{}";
					RowData row = (RowData) baserow.clone();
					HashSet<String> myknrs = new HashSet<String>();
					if ("POST".equalsIgnoreCase(request.getMethod())) {
						jsonString = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));

						RequestSet myrequest =  gson.fromJson(jsonString, RequestSet.class);
						
						//Alter
						if (myrequest.alter != null)
							row.put(Consts.alterattribute, myrequest.alter);
						//geschlecht
						if (myrequest.geschlecht != null)
							row.put(Consts.geschlechtattribute, myrequest.geschlecht);
	
						for (int i=0; i<myrequest.knr.length; i++) {
							row.put(Consts.knrattributePrefix+myrequest.knr[i],"1");
							myknrs.add(myrequest.knr[i]);
						}
						//KH_KNR
						for (int i=0; i<myrequest.knr_kh.length; i++) {
							row.put(Consts.khattributePrefix+myrequest.knr_kh[i],"1");
							myknrs.add(myrequest.knr_kh[i]);
						}
						//ATC
						for (int i=0; i<myrequest.atc.length; i++) {
							row.put(Consts.atcattributePrefix+myrequest.atc[i],"1");
						}
					}
					
					//Calculate Risks Scores
					HashMap<String,ResultSet> resultslist = new HashMap<String,ResultSet>();
					String knr;
					String knrkey;
					for (String key : targetlist.keySet()) {
						if (key.startsWith(Consts.knrTargetPrefix)) {
							//knr models
							knr = key.substring(Consts.knrTargetPrefix.length());
							knrkey = knrlist.get(knr);
							if (resultslist.get(knrkey) == null) resultslist.put(knrkey,new ResultSet());
							try { resultslist.get(knrkey).risk[0] = targetlist.get(key).getRiskFactor(row);} catch (Exception e) {}
							resultslist.get(knrkey).praevalenz[0] = targetlist.get(key).getPraevalenz();
							if (resultslist.get(knrkey).risk[0]==0) resultslist.get(knrkey).rrisk[0]=0;
							else resultslist.get(knrkey).rrisk[0] = resultslist.get(knrkey).risk[0]/resultslist.get(knrkey).praevalenz[0];
							resultslist.get(knrkey).auc[0] = targetlist.get(key).getAuc(true);
							if (myknrs.contains(knr)) resultslist.get(knrkey).isnew=false;
						} else if (key.startsWith(Consts.khTargetPrefix)) {
							//kh models
							knr = key.substring(Consts.khTargetPrefix.length());
							knrkey = knrlist.get(knr);
							if (resultslist.get(knrkey) == null) resultslist.put(knrkey,new ResultSet());
							try { resultslist.get(knrkey).risk[1] = targetlist.get(key).getRiskFactor(row);} catch (Exception e) {}
							resultslist.get(knrkey).praevalenz[1] = targetlist.get(key).getPraevalenz();
							if (resultslist.get(knrkey).risk[1]==0) resultslist.get(knrkey).rrisk[1]=0;
							else resultslist.get(knrkey).rrisk[1] = resultslist.get(knrkey).risk[1]/resultslist.get(knrkey).praevalenz[1];
							resultslist.get(knrkey).auc[1] = targetlist.get(key).getAuc(true);
						} else {
							//tod and other models
							knrkey = key;
							if (resultslist.get(knrkey) == null) resultslist.put(knrkey,new ResultSet());
							try { resultslist.get(knrkey).risk[0] = targetlist.get(key).getRiskFactor(row);} catch (Exception e) {}
							resultslist.get(knrkey).praevalenz[0] = targetlist.get(key).getPraevalenz();
							if (resultslist.get(knrkey).risk[0]==0) resultslist.get(knrkey).rrisk[0]=0;
							else resultslist.get(knrkey).rrisk[0] = resultslist.get(knrkey).risk[0]/resultslist.get(knrkey).praevalenz[0];
							resultslist.get(knrkey).auc[0] = targetlist.get(key).getAuc(true);
						}
					}
					//round all
					for (ResultSet rs : resultslist.values()) {
						rs.roundMe();
					}
					//clear all zero only
					/*
					for (Iterator<HashMap.Entry<String, ResultSet>> it = resultslist.entrySet().iterator(); it.hasNext();) {
						HashMap.Entry<String, ResultSet> entry = it.next();
						if (entry.getValue().rrisk[0]==0 && entry.getValue().rrisk[1]==0)
					       it.remove();
						else entry.getValue().roundMe();
					}*/
					//String json_data = "{ labels: ['resilience', 'maintainability', 'accessibility','uptime', 'functionality', 'impact'],series: [{label: '2012',values: [4, 8, 15, 16, 23, 42]},{label: '2013',values: [12, 43, 22, 11, 73, 25]},{label: '2014',values: [31, 28, 14, 8, 15, 21]}]}";
					String myresponse = gson.toJson(resultslist);
					response.getWriter().append(myresponse);
				}
	}
	
	private HashMap<String,Target> getAllTargets(String modelfile) throws Exception {
		HashMap<String,Target> mylist = new HashMap<String,Target>();
		//reads from csv: target;mean;auc_train;auc_test;model_id
		//system property: java -Dconfig.ehcfile="D:/all_models.csv"
		CSVReader reader = new CSVReader(new FileReader(modelfile), ';', '"');
		List<String[]> readIn = reader.readAll();
		reader.close();
		//first line = header-line
		String[] headerline = readIn.get(0);
		//assign colnumbers for columns needed
		Integer targetcol=null;
		Integer praevcol=null;
		Integer auc_traincol=null;
		Integer auc_testcol=null;
		Integer idcol=null;
		for (int j=0; j<headerline.length; j++) {
			if (headerline[j].trim().toUpperCase().equals(Consts.targetCol)) targetcol=j;
			if (headerline[j].trim().toUpperCase().equals(Consts.targetPraevCol)) praevcol=j;
			if (headerline[j].trim().toUpperCase().equals(Consts.targetAucTrainCol)) auc_traincol=j;
			if (headerline[j].trim().toUpperCase().equals(Consts.targetAucTestCol)) auc_testcol=j;
			if (headerline[j].trim().toUpperCase().equals(Consts.targetIDCol)) idcol=j;
		}
		readIn.remove(0);
		if (targetcol == null || praevcol==null || auc_traincol==null || auc_testcol==null || idcol==null || readIn.size()==0 )
			throw new Exception("Configuration File does not contain columns needed or is empty");
		Target newtarget;
		for (String[] nextline : readIn) {
			if (!nextline[targetcol].isEmpty()) {
				newtarget = new Target(nextline[targetcol].toUpperCase(),nextline[praevcol],nextline[auc_traincol],nextline[auc_testcol],nextline[idcol]);
				mylist.put(nextline[targetcol].toUpperCase(), newtarget);
			}
		}
		return mylist;
	}
	
	/*
	 * Builds base row data, i.e. includes all possible features: Alter, Geschlecht, KNR, KH_KNR, ATC and sets to 0
	 * -> POJO prediction gives "NaN" in case values are missing
	 */
	private RowData buildBaseRow(List<String> featurelist) {
		RowData baserow = new RowData();
		//Alter
		//baserow.put(Consts.alterattribute, "0");
		//geschlecht
		//baserow.put(Consts.geschlechtattribute, "0");
		for (String key: featurelist) {
			baserow.put(key,"0");
		}
		return baserow;
	}
	

}
