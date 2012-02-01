package fr.insarennes.fafdti.tree;

public class DecisionTreePending implements DecisionTree {

	@Override
	public void accept(DecisionTreeVisitor dtv) {
		dtv.visitPending(this);

	}

	@Override
	public boolean canOverwrite() {
		return true;
	}

}
