package fr.insarennes.fafdti.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingInterrogator;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.visitors.QuestionExample;
/** Classe effectuant la campagne de test sur l'entrée standard
 */
public class QueryStater {
	
	private static Logger log = Logger.getLogger(QueryStater.class);
	
	private BaggingTrees trees;
	private int nbError;
	private int nbSucess;
	private Map<String, Integer> searchByLabel;
	private Map<String, Integer> foundByLabel;
	private Map<String, Integer> correctByLabel;
	private Map<String, Integer> errorByLabel;
	private BufferedReader buffer;
	private int numChunk;
	
	private final String VALIDATION_REGEX = "(.+,)+(.+)\\.";
	private Pattern validationPattern;
	
	private static final int CHUNK_SIZE = 10000;
	
	public QueryStater(BaggingTrees trees, InputStream input){
		buffer = new BufferedReader(new InputStreamReader(System.in));
		numChunk = 0;
		nbError = 0;
		nbSucess = 0;
		this.trees = trees;
		searchByLabel = new HashMap<String, Integer>();
		foundByLabel = new HashMap<String, Integer>();
		correctByLabel = new HashMap<String, Integer>();
		errorByLabel = new HashMap<String, Integer>();
		
		validationPattern = Pattern.compile(VALIDATION_REGEX);
	}
	
	public void launch() throws IOException, FAFException{
		int cores = Runtime.getRuntime().availableProcessors();
		
		Vector<Consumer> consumers = new Vector<QueryStater.Consumer>();
		for(int i = 0; i < cores; i++){
			Consumer consumer = new Consumer();
			consumer.start();
			consumers.add(consumer);
		}

		try {
			for(Consumer consumer : consumers){
				consumer.join();
				nbError  += consumer.nbError_local;
				nbSucess += consumer.nbSucess_local;
				add(searchByLabel,  consumer.searchByLabel_local);
				add(foundByLabel,   consumer.foundByLabel_local);
				add(correctByLabel, consumer.correctByLabel_local);
				add(errorByLabel,   consumer.errorByLabel_local);
			}
		} catch (InterruptedException e) {
			log.error("InterruptedException "+e);
		}
		

		ArrayList<Map<String,Integer>> listMaps = new ArrayList<Map<String,Integer>>();
		listMaps.add(searchByLabel);
		listMaps.add(correctByLabel);
		listMaps.add(errorByLabel);
		listMaps.add(foundByLabel);
		
		checkAllExistance(listMaps);
	}
	
	private synchronized void fillBuffer(Vector<String> strings) throws IOException{
		strings.clear();
		String line = "";
		int numLine = 0;
		while(numLine < CHUNK_SIZE && (line=buffer.readLine()) != null){
			numLine++;
			strings.add(line);
		}
		log.info("load chunk "+(numChunk*CHUNK_SIZE)+"-"+((numChunk+1)*CHUNK_SIZE-1));
		numChunk++;
	}
	
	/*private void parse(String line) throws IOException, FAFException{
		//check pattern
		Matcher lineMatcher = validationPattern.matcher(line);
		if(!lineMatcher.matches()) {
			throw new FAFException("Invalid line : " + line + "\n"
						+ "Line must validate the regular expression : "
						+ VALIDATION_REGEX);
		}else{
			//parsing de la ligne
			int index = line.lastIndexOf(FAFQueryMode.DELIMITER);
			String question = line.substring(0, index);
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
			log.debug("question="+qe.toString());
			log.debug("label search="+label);
			log.debug("label found="+sres);
			//OK ou pas
			//System.out.println(label);
			
			synchronized(this){
				if(sres.equals(label)){
					nbSucess++;
					incrInMap(correctByLabel, sres);
				}
				else {
					nbError++;
					incrInMap(errorByLabel, sres);
				}
				incrInMap(searchByLabel, label);
				incrInMap(foundByLabel, sres);
			}
		}
	}*/
	
	private void incrInMap(Map<String, Integer> map, String label){
		Integer i = map.get(label);
		if(i==null)
			map.put(label, 1);
		else
			map.put(label, i+1);
	}
	
	private void checkAllExistance(ArrayList<Map<String,Integer>> maps){
		for(Map<String,Integer> map1 : maps){
			for(Map<String,Integer> map2 : maps){
				for(String key : map1.keySet()){
					if(!map2.containsKey(key))
						map2.put(key, 0);
				}
			}
		}
	}
	
	private void add(Map<String,Integer> dest, Map<String,Integer> src){
		for(String key : src.keySet()){
			if(dest.containsKey(key))
				dest.put(key, dest.get(key) + src.get(key));
			else
				dest.put(key, src.get(key));
		}
	}
	
	public int getTotal(){
		return nbError+nbSucess;
	}
	public int getTotalError(){
		return nbError;
	}
	
	public int getTotalSuccess(){
		return nbSucess;
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

	class Consumer extends Thread{
		private int nbError_local;
		private int nbSucess_local;
		private Map<String, Integer> searchByLabel_local;
		private Map<String, Integer> foundByLabel_local;
		private Map<String, Integer> correctByLabel_local;
		private Map<String, Integer> errorByLabel_local;
		
		public Consumer(){
			nbError_local = 0;
			nbSucess_local = 0;
			searchByLabel_local = new HashMap<String, Integer>();
			foundByLabel_local = new HashMap<String, Integer>();
			correctByLabel_local = new HashMap<String, Integer>();
			errorByLabel_local = new HashMap<String, Integer>();
		}
		
		public void run(){
			try{
				Vector<String> strings = new Vector<String>();
				do{
					fillBuffer(strings);
					Iterator<String> it = strings.iterator();
					while(it.hasNext()){
						String line = it.next();
						Matcher lineMatcher = validationPattern.matcher(line);
						if(!lineMatcher.matches()) {
							throw new FAFException("Invalid line : " + line + "\n"
										+ "Line must validate the regular expression : "
										+ VALIDATION_REGEX);
						}else{
							//parsing de la ligne
							int index = line.lastIndexOf(FAFQueryMode.DELIMITER);
							String question = line.substring(0, index);
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
							log.debug("question="+qe.toString());
							log.debug("label search="+label);
							log.debug("label found="+sres);
							//OK ou pas
							//System.out.println(label);
							if(sres.equals(label)){
								nbSucess_local++;
								incrInMap(correctByLabel_local, sres);
							}
							else {
								nbError_local++;
								incrInMap(errorByLabel_local, sres);
							}
							incrInMap(searchByLabel_local, label);
							incrInMap(foundByLabel_local, sres);
						}
					}
				}while(strings.size()>0);
			}catch(IOException e){
				log.error("IOException "+e);
			}catch(FAFException e){
				log.error("FAFException "+e);
			}
		}
	}
}
