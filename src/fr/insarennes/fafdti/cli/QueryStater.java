package fr.insarennes.fafdti.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingInterrogator;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.tree.ImportXML;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.visitors.QuestionExample;

public class QueryStater {
	
	private static Logger log = Logger.getLogger(QueryStater.class);
	
	private BaggingTrees trees;
	private int nbError;
	private int nbSucess;
	
	public QueryStater(String xmlInput){
		nbError = 0;
		nbSucess = 0;
		ImportXML importer = new ImportXML(xmlInput);
		try {
			importer.launch();
		} catch (FAFException e) {
			log.error("Xml import failed");
		}
		trees = importer.getResult();
	}
	
	public void launch() throws IOException{
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		while((line=buffer.readLine()) != null){
			//parsing de la ligne
			int index = line.lastIndexOf(FAFQueryMode.DELIMITER);
			String question = line.substring(0, index - 1);
			String label = line.substring(index +1, line.length() - 1);
			//construction du QuestionExample
			ArrayList<String> values = new ArrayList<String>();
			StringTokenizer tokn = new StringTokenizer(question);
			while(tokn.hasMoreElements())
				values.add(tokn.nextToken(FAFQueryMode.DELIMITER));
			QuestionExample qe = new QuestionExample(values);
			//question
			BaggingInterrogator inter = new BaggingInterrogator(trees);
			LeafLabels res = inter.query(qe);
			String sres = res.getBestScore();
			//OK ou pas
			System.out.println(label);
			if(sres.equals(label))
				nbSucess++;
			else nbError++;
		}
	}
	
	public String getStats(){
		int total = nbError+nbSucess;
		String res = "Success classification rate = "+((double)nbSucess/(double)total)*100.0+"%";
		res += " | Error classification rate = "+((double)nbError/(double)total)*100.0+"%";
		return res;
	}

}
