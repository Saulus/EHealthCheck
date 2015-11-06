package servlets;

import configuration.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Servlet implementation class EHealthCheck
 * 
 * h2o classes go into: /WEB-INF/classes/
 * allmodels.csv goes into: /WEB-INF/allmodels.csv 
 */
@WebServlet("/ATC")
public class ATC extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private LinkedHashMap<String,String> mylist;
	JSONObject myjson;
	private boolean hasError = false;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ATC() {
        super();
        
        String myfile = Consts.atccsv;
        
        try {
        	myfile = Init.getWebInfPath() + "/"+myfile;
        	this.mylist=readInKnr(myfile);
        	this.myjson = new JSONObject(mylist);
        } catch (Exception e) {
        	System.err.println("Fehler gefunden beim Einlesen der Konfiguration aus Datei " + myfile);
			e.printStackTrace();
			this.hasError=true;
        }
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Content-Type", "application/json");
		response.setCharacterEncoding("UTF-8");
		
		if (hasError) response.sendError(520, "Init-Fehler");
		else response.getWriter().append(myjson.toString());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private LinkedHashMap<String,String> readInKnr(String file) throws Exception {
		LinkedHashMap<String,String> mylist = new LinkedHashMap<String,String>();
		CSVReader reader = new CSVReader(new FileReader(file), ';', '"');
		List<String[]> readIn = reader.readAll();
		reader.close();
		//first line = header-line
		String[] headerline = readIn.get(0);
		//assign colnumbers for columns needed
		Integer keycol=null;
		Integer valuecol=null;
		for (int j=0; j<headerline.length; j++) {
			if (headerline[j].trim().toLowerCase().equals(Consts.atcKeyCol)) keycol=j;
			if (headerline[j].trim().toLowerCase().equals(Consts.atcValueCol)) valuecol=j;
		}
		readIn.remove(0);
		if (keycol == null || valuecol==null  || readIn.size()==0 )
			throw new Exception("Configuration File does not contain columns needed or is empty");
		for (String[] nextline : readIn) {
			if (!nextline[keycol].isEmpty()) {
				mylist.put(nextline[keycol], nextline[valuecol]);
			}
		}
		return mylist;
	}
	

}
