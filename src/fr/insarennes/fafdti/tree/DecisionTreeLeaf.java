package fr.insarennes.fafdti.tree;

public class DecisionTreeLeaf implements DecisionTree {

	@Override
	public void accept(DecisionTreeVisitor dtv) {
		dtv.visitLeaf(this);

	}

	@Override
	public boolean canOverwrite() {
		return false;
	}

}
