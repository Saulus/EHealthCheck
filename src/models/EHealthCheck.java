package models;


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
import configuration.Consts;

/**
 * Servlet implementation class EHealthCheck
 */
@WebServlet("/EHealthCheck")
public class EHealthCheck extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private HashMap<String,Knr> knrlist;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EHealthCheck() {
        super();
        try {
        	this.knrlist=getAllKnr();
        } catch (Exception e) {
        	System.out.println("Fehler gefunden beim Einlesen der KNR-Konfiguration aus Datei System.Property: " + Consts.configproperty);
			e.printStackTrace();
        }
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//atributenames 
		request.getAttributeNames();
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private HashMap<String,Knr> getAllKnr() throws Exception {
		HashMap<String,Knr> knrlist = new HashMap<String,Knr>();
		//reads from csv: knr;name;prävalenz;feature_no (i.e. number in feature matrix)
		//system property: java -Dconfig.ehcfile="D:/knr.csv"
		String configfile = System.getProperty(Consts.configproperty);
		CSVReader reader = new CSVReader(new FileReader(configfile), ';', '"');
		List<String[]> readIn = reader.readAll();
		reader.close();
		//first line = header-line
		String[] headerline = readIn.get(0);
		//assign colnumbers for columns needed
		Integer knrcol=null;
		Integer namecol=null;
		Integer praevcol=null;
		Integer featurecol=null;
		for (int j=0; j<headerline.length; j++) {
			if (headerline[j].toUpperCase().equals(Consts.knrCol)) knrcol=j;
			if (headerline[j].toUpperCase().equals(Consts.knrNameCol)) namecol=j;
			if (headerline[j].toUpperCase().equals(Consts.knrPraevCol)) praevcol=j;
			if (headerline[j].toUpperCase().equals(Consts.knrFeatureCol)) featurecol=j;
		}
		readIn.remove(0);
		if (knrcol == null || namecol==null || praevcol==null || featurecol==null || readIn.size()==0 )
			throw new Exception("Configuration File does not contain columns needed or is empty");
		Knr newknr;
		for (String[] nextline : readIn) {
			if (!nextline[knrcol].isEmpty()) {
				newknr = new Knr(nextline[namecol],Double.parseDouble(nextline[praevcol]),Integer.parseInt(nextline[featurecol]));
				knrlist.put(nextline[knrcol], newknr);
			}
		}
		return knrlist;
	}
	

}
