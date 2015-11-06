package servlets;

import configuration.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.bytecode.opencsv.CSVReader;
import hex.genmodel.easy.RowData;

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
	private boolean hasError = false;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EHealthCheck() {
        super();
        
        String modelfile = Consts.modelcsv;
        
        try {
        	modelfile = Init.getWebInfPath() + "/classes/"+modelfile;
        	this.targetlist=getAllTargets(modelfile);
        } catch (Exception e) {
        	System.err.println("Fehler gefunden beim Einlesen der Target-Konfiguration aus Datei " + modelfile);
			e.printStackTrace();
			this.hasError=true;
        }
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//expects the following attributes (max):
		//Knr ambulant: knr=(1,2,8), Knr stationär: kh_knr=(9,10), ATC: atc=(AA2BB,AB4GF), alter=57, Geschlecht=2
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Content-Type", "application/json");
		
		if (hasError) response.sendError(520, "Init-Fehler");
		else {
			//get attributes and transform to RowData
			/*
			Enumeration<String> att = request.getAttributeNames();
			
			while(att.hasMoreElements()) {
				response.getWriter().append((String) request.getAttribute(att.nextElement()));
				}
			
			RowData row = new RowData();
			 row.put("ALTER", "18");
			 row.put("GESCHLECHT", "2");
			 
			for (String key : targetlist.keySet()) {
				try {
					response.getWriter().append(key+": ").append("Risk: "+Double.toString(targetlist.get(key).getRelativeRiskFactor(row))).append("Prävalenz: "+Double.toString(targetlist.get(key).getPraevalenz()));
				} catch (Exception e) {}
			} */
			String json_data = "{ labels: ['resilience', 'maintainability', 'accessibility','uptime', 'functionality', 'impact'],series: [{label: '2012',values: [4, 8, 15, 16, 23, 42]},{label: '2013',values: [12, 43, 22, 11, 73, 25]},{label: '2014',values: [31, 28, 14, 8, 15, 21]},]}";
			response.getWriter().append(json_data);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
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
			if (headerline[j].trim().toLowerCase().equals(Consts.targetCol)) targetcol=j;
			if (headerline[j].trim().toLowerCase().equals(Consts.targetPraevCol)) praevcol=j;
			if (headerline[j].trim().toLowerCase().equals(Consts.targetAucTrainCol)) auc_traincol=j;
			if (headerline[j].trim().toLowerCase().equals(Consts.targetAucTestCol)) auc_testcol=j;
			if (headerline[j].trim().toLowerCase().equals(Consts.targetIDCol)) idcol=j;
		}
		readIn.remove(0);
		if (targetcol == null || praevcol==null || auc_traincol==null || auc_testcol==null || idcol==null || readIn.size()==0 )
			throw new Exception("Configuration File does not contain columns needed or is empty");
		Target newtarget;
		for (String[] nextline : readIn) {
			if (!nextline[targetcol].isEmpty()) {
				newtarget = new Target(nextline[targetcol],nextline[praevcol],nextline[auc_traincol],nextline[auc_testcol],nextline[idcol]);
				mylist.put(nextline[targetcol], newtarget);
			}
		}
		return mylist;
	}
	

}
