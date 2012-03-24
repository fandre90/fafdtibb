package fr.insarennes.fafdti.bagging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;
import fr.insarennes.fafdti.visitors.Interrogator;
import fr.insarennes.fafdti.visitors.QuestionExample;

public class BaggingInterrogator {
	
	private static Logger log = Logger.getLogger(BaggingInterrogator.class);
	
	private BaggingTrees trees;
	
	public BaggingInterrogator(BaggingTrees trees){
		this.trees = trees;
	}
	
	public LeafLabels query(QuestionExample qe){
		List<LeafLabels> labels = new ArrayList<LeafLabels>();
		for(int i=0 ; i<trees.getSize() ; i++){
			Interrogator inter = new Interrogator(qe);
			trees.getTree(i).accept(inter);
			labels.add(inter.getResult());
		}
		//on fait la moyenne des résultats
		Map<String, Double> res = new HashMap<String, Double>();
		Map<String, Integer> howmany = new HashMap<String, Integer>();
		
		Iterator<LeafLabels> it = labels.iterator();
		while(it.hasNext()){
			Map<String, Double> map = it.next().getLabels();
			Set<Entry<String, Double>> mapentr = map.entrySet();
			Iterator<Entry<String, Double>> it2 = mapentr.iterator();
			while(it2.hasNext()){
				Entry<String, Double> entry = it2.next();
				String key = entry.getKey();
				if(res.containsKey(key)){
					Integer hm = howmany.get(key);
					res.put(key, ((res.get(key)*hm) + entry.getValue())/(hm+1));
					howmany.put(key, hm+1);
				}
				else{
					res.put(key,  entry.getValue());
					howmany.put(key, 1);
				}
			}
		}
		LeafLabels ll = null;
		try {
			ll = new LeafLabels(res);
		} catch (InvalidProbabilityComputationException e) {
			log.error("BaggingInterrogator process failed");
			log.error(e.getMessage());
		}
		return ll;
	}
	
}
