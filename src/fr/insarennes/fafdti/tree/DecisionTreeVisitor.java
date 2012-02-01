package fr.insarennes.fafdti.tree;

public interface DecisionTreeVisitor {
	public void visitQuestion(DecisionTreeQuestion dtq);
	public void visitLeaf(DecisionTreeLeaf dtl);
	public void visitPending(DecisionTreePending dtp);
}
