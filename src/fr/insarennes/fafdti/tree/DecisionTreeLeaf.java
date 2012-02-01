package fr.insarennes.fafdti.tree;

public class DecisionTreeLeaf implements DecisionTree {
	
	private String label;
	
	DecisionTreeLeaf(String lbl){
		label = lbl;
	}
	@Override
	public void accept(DecisionTreeVisitor dtv) {
		dtv.visitLeaf(this);

	}

	@Override
	public boolean canOverwrite() {
		return false;
	}

	public String getLabel(){	return label;	}
}
