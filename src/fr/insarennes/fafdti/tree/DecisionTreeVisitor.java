package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.visitors.InvalidCallException;
/** Interface des visiteurs d'un DecisionTree
 * 
 */
public interface DecisionTreeVisitor {
	public void visitQuestion(DecisionTreeQuestion dtq);
	public void visitLeaf(DecisionTreeLeaf dtl);
	public void visitPending(DecisionTreePending dtp) throws InvalidCallException;
}
