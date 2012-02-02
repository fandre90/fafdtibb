package fr.insarennes.fafdti.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class LeafLabels {
	private static final double EPSILON_VALIDATION_PROBABILITY_COMPUTATION = 0.1;
	/* Map des labels liés à leur probabilité (entre 0 et 1) */
	private Map<String,Double> labels;
	
	public LeafLabels(){
		labels = new HashMap<String,Double>();
	}
	
	public LeafLabels(Map<String,Double> lbls) throws InvalidProbabilityComputationException{
		labels = lbls;
		if (!isValid()) throw new InvalidProbabilityComputationException();
	}
	
	public String toStr() throws InvalidProbabilityComputationException{
		if(!isValid())	throw new InvalidProbabilityComputationException();
		String res = new String("");
		Set<String> lbls = labels.keySet();
		Iterator<String> it = lbls.iterator();
		while(it.hasNext()){
			String lbl = it.next();
			Double d = labels.get(lbl);
			d*=100;
			res += lbl+" : "+d.toString()+"%"+"\n";
		}
		return res;		
	}
	private boolean isValid(){
		double d = 0.0;
		Set<String> lbls = labels.keySet();
		Iterator<String> it = lbls.iterator();
		while(it.hasNext()){
			String lbl = it.next();
			Double tmp = labels.get(lbl);
			d+=tmp.doubleValue();
			//System.out.println(lbl+":"+tmp);
		}
		return Math.abs(d - 1.0) < EPSILON_VALIDATION_PROBABILITY_COMPUTATION;
	}
}
