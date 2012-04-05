package fr.insarennes.fafdti.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private Map<String, Integer> searchByLabel;
	private Map<String, Integer> foundByLabel;
	private Map<String, Integer> correctByLabel;
	private Map<String, Integer> errorByLabel;
	private List<Map<String,Integer>> listMaps;
	
	public QueryStater(BaggingTrees trees){
		nbError = 0;
		nbSucess = 0;
		this.trees = trees;
		searchByLabel = new HashMap<String, Integer>();
		foundByLabel = new HashMap<String, Integer>();
		correctByLabel = new HashMap<String, Integer>();
		errorByLabel = new HashMap<String, Integer>();
		listMaps = new ArrayList<Map<String,Integer>>();
		listMaps.add(searchByLabel);
		listMaps.add(correctByLabel);
		listMaps.add(errorByLabel);
		listMaps.add(foundByLabel);
	}
	
	public void launch() throws IOException, FAFException{
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		String line = "";
		while((line=buffer.readLine()) != null){
			//parsing de la ligne
			int index = line.lastIndexOf(FAFQueryMode.DELIMITER);
			String question = line.substring(0, index - 1);
			String label = line.substring(index + 1, line.length() - 1).trim();
			//construction du QuestionExample
			ArrayList<String> values = new ArrayList<String>();
			StringTokenizer tokn = new StringTokenizer(question);
			while(tokn.hasMoreElements())
				values.add(tokn.nextToken(FAFQueryMode.DELIMITER).trim());
			QuestionExample qe = new QuestionExample(values);
			//question
			BaggingInterrogator inter = new BaggingInterrogator(trees);
			LeafLabels res = inter.query(qe);
			String sres = res.getBestScore();
			//OK ou pas
			//System.out.println(label);
			if(sres.equals(label)){
				nbSucess++;
				incrInMap(correctByLabel, sres);
			}
			else {
				nbError++;
				incrInMap(errorByLabel, label);
			}
			incrInMap(searchByLabel, label);
			incrInMap(foundByLabel, sres);
		}
		if(getTotal()==0){
			throw new FAFException("Nothing found on standard input");
		}
	}
	
	private void incrInMap(Map<String, Integer> map, String label){
		Integer i = map.get(label);
		if(i==null){
			map.put(label, 1);
			for(Map<String,Integer> m : listMaps)
				if(!m.equals(map))
					m.put(label, 0);
		}
		else
			map.put(label, i+1);
	}
	
	public int getTotal(){
		return nbError+nbSucess;
	}
	public int getTotalError(){
		return nbError;
	}

	public String getFastResult() {
		return "Success classification rate = "+(double)nbSucess/(double)getTotal()+" | Error classification rate = "+(double)nbError/(double)getTotal();
	}
	
	public Map<String, Integer> getErrorByLabel(){
		return errorByLabel;
	}
	
	public Map<String, Integer> getCorrectByLabel(){
		return correctByLabel;
	}
	public Map<String, Integer> getSearchByLabel(){
		return searchByLabel;
	}
	public Map<String, Integer> getFoundByLabel(){
		return foundByLabel;
	}

}
