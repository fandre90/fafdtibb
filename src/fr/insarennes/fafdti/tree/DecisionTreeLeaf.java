/** Classe DecisionTreeLeaf implémentant DecisionTree
 * Elle représente les feuille de nos arbres
 * Elle encapsule un LeafLabels et le nombre d'exemples que l'on a classé dedans
 */

package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.FAFException;

public class DecisionTreeLeaf implements DecisionTree {
	
	private LeafLabels labels;
	private int nbExamplesClassified;
	
	/**
	 * @deprecated Utiliser l'autre constructeur
	 * @param lbl
	 */
	public DecisionTreeLeaf(LeafLabels lbl){
		labels = lbl;
		nbExamplesClassified = Integer.MIN_VALUE;
	}
	
	public DecisionTreeLeaf(LeafLabels lbl, int nbClassified){
		labels = lbl;
		nbExamplesClassified = nbClassified;
	}
	
	public int getNbClassified() throws FAFException{
		if(nbExamplesClassified==Integer.MIN_VALUE)
			throw new FAFException("Value has not been set, use the other constructor");
		return nbExamplesClassified;
	}
	@Override
	public void accept(DecisionTreeVisitor dtv) {
		dtv.visitLeaf(this);

	}

	@Override
	public boolean canOverwrite() {
		return false;
	}

	public LeafLabels getLabels(){	return labels;	}
}
