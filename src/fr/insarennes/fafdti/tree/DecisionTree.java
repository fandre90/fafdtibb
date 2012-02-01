package fr.insarennes.fafdti.tree;

public interface DecisionTree {
	void accept(DecisionTreeVisitor dtv);
	boolean canOverwrite();
}
