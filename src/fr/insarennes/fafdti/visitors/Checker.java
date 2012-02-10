package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.tree.*;

class Checker implements DecisionTreeVisitor{
	private int nbPending;
	
	public Checker(){
		nbPending = 0;
	}
	public boolean checkOK(){
		return nbPending==0;
	}
	public void visitQuestion(DecisionTreeQuestion dtq){
		if(nbPending!=0)	return;
		dtq.getYesTree().accept(this);
		dtq.getNoTree().accept(this);
	}
	public void visitLeaf(DecisionTreeLeaf dtl){
	}
	public void visitPending(DecisionTreePending dtp){
		nbPending++;
	}
}
