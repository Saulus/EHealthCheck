package servlets;

import configuration.*;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

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
	
	public static String myfile = Init.getWebInfPath() + "/"+Consts.atccsv;
	private LinkedHashMap<String,String> mylist;
	private String myjson = "{}";
	private boolean hasError = false;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ATC() {
        super();
        
        try {
        	this.mylist=readInList(myfile, true, false);
        	Gson gson = new Gson(); 
        	this.myjson = gson.toJson(mylist);
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
		else response.getWriter().append(myjson);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	public static LinkedHashMap<String,String> readInList(String file, boolean addKeyPrefix, boolean addKeySuffix) throws Exception {
		LinkedHashMap<String,String> mylist = new LinkedHashMap<String,String>();
		Charset inputCharset = Charset.forName("ISO-8859-1");
		CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), inputCharset), ';', '"');
		List<String[]> readIn = reader.readAll();
		reader.close();
		//first line = header-line
		String[] headerline = readIn.get(0);
		//assign colnumbers for columns needed
		Integer keycol=null;
		Integer valuecol=null;
		for (int j=0; j<headerline.length; j++) {
			if (headerline[j].trim().toUpperCase().equals(Consts.atcKeyCol)) keycol=j;
			if (headerline[j].trim().toUpperCase().equals(Consts.atcValueCol)) valuecol=j;
		}
		readIn.remove(0);
		if (keycol == null || valuecol==null  || readIn.size()==0 )
			throw new Exception("Configuration File does not contain columns needed or is empty");
		String key;
		String val;
		for (String[] nextline : readIn) {
			if (!nextline[keycol].isEmpty()) {
				key= nextline[keycol].toUpperCase();
				val = nextline[valuecol];
				if (addKeyPrefix) val = key + " " + val;
				if (addKeySuffix) val = val + " ("+key+")";
				mylist.put(key, val);
			}
		}
		return mylist;
	}
	

}
