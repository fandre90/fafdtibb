package fr.insarennes.fafdti.builder.stopcriterion;

import org.apache.log4j.Logger;
/** Classe encapsulant le critère du nombre minimal d'exemples par feuille
 *  pour la construction de l'arbre
 */
public class ExampleMin implements StoppingCriterion {

	private static Logger log = Logger.getLogger(ExampleMin.class);
	private int exampleMin;
	
	public ExampleMin(int example){
		exampleMin = example;
	}
	public boolean mustStop(StopCriterionUtils node) {
		boolean res = node.getMinExamples() < exampleMin;
		if(res)
			log.info("stopping criterion : examples min by leaf");
		return res;
	}

}
