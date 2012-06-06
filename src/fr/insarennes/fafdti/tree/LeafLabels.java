package fr.insarennes.fafdti.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.insarennes.fafdti.FAFException;
/** Classe encapsulant la distribution calculée attachée à un {@link DecisionTreeLeaf}
 * Une distribution est forcemment composée d'une (ou plusieurs) classes associées
 * à leur probabilités respectives (la somme de celles-ci devant obligatoirement être
 * égal (ou très proche) de 1)
 */
public class LeafLabels {
	//si la somme fait moins de 0.9 (90%) , il y a visiblement une erreur de calcul
	public static final double EPSILON_VALIDATION_PROBABILITY_COMPUTATION = 0.1;
	/* Map des labels liés à leur probabilité (entre 0 et 1) */
	private Map<String,Double> labels;
	
	public LeafLabels(Map<String,Double> lbls) throws InvalidProbabilityComputationException{
		//TODO ca serait pas mieux de faire une copie?
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
	
	/**
	 * @return la classe dont le score est le plus élevé
	 * Attention, le résultat est valide seulement si il est différent de "not_found"
	 */
	public String getBestScore(){
		double bestScore = 0.0;
		String res = "not_found";
		Set<Entry<String, Double>> set = labels.entrySet();
		for(Entry<String, Double> e : set){
			double tmp = e.getValue();
			if(tmp > bestScore){
				bestScore = tmp;
				res = e.getKey();	
			}
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
		return Math.abs(d - 1.0) <= EPSILON_VALIDATION_PROBABILITY_COMPUTATION;
	}
	
	/** Exception levée lorsque l'on essaie de construire une distribution
	 * contenant un ensemble de probabilités incohérentes, c'est-à-dire dont 
	 * la somme n'est pas assez proche de 1
	 *
	 */
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
