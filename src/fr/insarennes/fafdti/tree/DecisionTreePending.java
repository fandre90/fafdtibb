package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.visitors.InvalidCallException;

public class DecisionTreePending implements DecisionTree {

	@Override
	public void accept(DecisionTreeVisitor dtv) {
		try {
			dtv.visitPending(this);
		} catch (InvalidCallException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public boolean canOverwrite() {
		return true;
	}

}
