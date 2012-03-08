package fr.insarennes.fafdti.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import fr.insarennes.fafdti.FAFException;

public class LeafLabels {
	//si la somme fait moins de 0.9 (90%) , il y a visiblement une erreur de calcul
	public static final double EPSILON_VALIDATION_PROBABILITY_COMPUTATION = 0.1;
	/* Map des labels liés à leur probabilité (entre 0 et 1) */
	private Map<String,Double> labels;
	
	public LeafLabels(Map<String,Double> lbls) throws InvalidProbabilityComputationException{
		labels = lbls;
		if (!isValid()) throw new InvalidProbabilityComputationException();
	}
	
	public String toString(){
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
	
	public Map<String,Double> getLabels(){
		//recopie
		return new HashMap<String,Double>(labels);
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
		return Math.abs(d - 1.0) <= EPSILON_VALIDATION_PROBABILITY_COMPUTATION;
	}
	public class InvalidProbabilityComputationException extends FAFException {

		public InvalidProbabilityComputationException(){
			super("Probabilities sum is too far from (1.0-"+LeafLabels.EPSILON_VALIDATION_PROBABILITY_COMPUTATION+")");
		}
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}
}
