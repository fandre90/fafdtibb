/** Visiteur permettant de compter le nombre de sous-arbre en construction à partir d'une
 * racine donnée ou simplement de vérifier qu'un arbre est complètement construit
 */

package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.tree.*;

public class Checker implements DecisionTreeVisitor{
	protected int nbPending;
	protected boolean visitAllAnyway;
	
	public Checker(){
		nbPending = 0;
		visitAllAnyway = false;
	}
	/**
	 * 
	 * @param visitAll true si on veut compter le nombre total de sous-arbre en construction
	 * Sinon on arrête la visite dès qu'on a detecté au moins un sous-arbre en construction
	 */
	public Checker(boolean visitAll){
		nbPending = 0;
		visitAllAnyway = visitAll;
	}
	/**
	 * 
	 * @return true si et seulement si l'arbre ne contient plus de sous-arbre en construction
	 */
	public boolean checkOK(){
		return nbPending==0;
	}
	/**
	 * 
	 * @return le nombre de sous-arbre encore en construction
	 * Cette méthode n'est cohérente que si le Checker a été construit ainsi Checker(true)
	 */
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
