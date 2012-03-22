package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.tree.*;

public class Checker implements DecisionTreeVisitor{
	protected int nbPending;
	protected boolean visitAllAnyway;
	
	public Checker(){
		nbPending = 0;
		visitAllAnyway = false;
	}
	public Checker(boolean visitAll){
		nbPending = 0;
		visitAllAnyway = visitAll;
	}
	public boolean checkOK(){
		return nbPending==0;
	}
	public int getNbPending(){
		return nbPending;
	}
	public void visitQuestion(DecisionTreeQuestion dtq){
		if(!visitAllAnyway && nbPending!=0)	return;
		dtq.getYesTree().accept(this);
		dtq.getNoTree().accept(this);
	}
	public void visitLeaf(DecisionTreeLeaf dtl){
	}
	public void visitPending(DecisionTreePending dtp){
		nbPending++;
	}
}
