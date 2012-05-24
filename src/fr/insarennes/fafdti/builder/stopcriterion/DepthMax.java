package fr.insarennes.fafdti.builder.stopcriterion;

import org.apache.log4j.Logger;
/** Classe encapsulant le critère de profondeur maximale de construction de l'arbre
 */
public class DepthMax implements StoppingCriterion {

	private static Logger log = Logger.getLogger(DepthMax.class);
	private int depthMax;
	
	public DepthMax(int depth){
		depthMax = depth;
	}
	@Override
	public boolean mustStop(StopCriterionUtils node) {
		boolean res = node.getDepth() >= depthMax;
		if(res)
			log.info("stopping criterion : max depth");
		return res;
	}

}
