package fr.insarennes.fafdti.tree;

public class DecisionTreeLeaf implements DecisionTree {
	
	private LeafLabels labels;
	
	DecisionTreeLeaf(LeafLabels lbl){
		labels = lbl;
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
