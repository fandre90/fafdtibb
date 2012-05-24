package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.DecisionTreeVisitor;
/**
 * Classe qui permet, en plus de checker l'arbre en entier, de compter le nombre
 * de noeuds et de feuilles qu'il contient
 */
public class StatChecker extends Checker{

	protected int nbLeafs;
	protected int nbNodes;
	
	public StatChecker(){
		//check the full tree
		super(true);
		nbLeafs = 0;
		nbNodes = 0;
	}
	
	public void visitQuestion(DecisionTreeQuestion dtq){
		super.visitQuestion(dtq);
		nbNodes++;
		
	}
	public void visitLeaf(DecisionTreeLeaf dtl){
		super.visitLeaf(dtl);
		nbLeafs++;
	}
	public void visitPending(DecisionTreePending dtp){
		super.visitPending(dtp);
	}
	public String toString(){
		return "Number of nodes = "+nbNodes+" ; Number of leafs = "+nbLeafs;
	}

}
