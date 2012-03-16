package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.FAFException;

public class DecisionTreeLeaf implements DecisionTree {
	
	private LeafLabels labels;
	private int nbExamplesClassified;
	
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
