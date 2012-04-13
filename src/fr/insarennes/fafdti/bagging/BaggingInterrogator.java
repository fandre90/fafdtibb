/** Classe permettant d'interroger un {@link BaggingTrees}
 * Cette classe interroge chaque arbre du {@link BaggingTrees} puis fait la moyenne
 * des résultats
 */

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
	
	/**
	 * @param qe la question posée
	 * @return le résultat sous forme de distribution
	 */
	public LeafLabels query(QuestionExample qe){
		List<LeafLabels> labels = new ArrayList<LeafLabels>();
		int nbTrees = trees.getSize();
		for(int i=0 ; i<nbTrees ; i++){
			Interrogator inter = new Interrogator(qe);
			trees.getTree(i).accept(inter);
			labels.add(inter.getResult());
		}
		//on fait la moyenne des résultats
		Map<String, Double> res = new HashMap<String, Double>();
		
		Iterator<LeafLabels> it = labels.iterator();
		while(it.hasNext()){
			Map<String, Double> map = it.next().getLabels();
			Set<Entry<String, Double>> mapentr = map.entrySet();
			Iterator<Entry<String, Double>> it2 = mapentr.iterator();
			while(it2.hasNext()){
				Entry<String, Double> entry = it2.next();
				String key = entry.getKey();
				if(res.containsKey(key)){
					res.put(key, (res.get(key) + entry.getValue()));
				}
				else{
					res.put(key,  entry.getValue());
				}
			}
		}
		Set<Entry<String, Double>> sres = res.entrySet();
		for(Entry<String, Double> e : sres){
			String key = e.getKey();
			res.put(key,e.getValue() / nbTrees);
			//System.out.println(res.get(key));
		}
			
		//on construit le LeafLabels	
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
