/** Interface DecisionTree
 * Un DecisionTree peut être un noeud ou une feuille
 */

package fr.insarennes.fafdti.tree;

public interface DecisionTree {
	
	/** Méthode de visite
	 * 
	 * @param dtv le visiteur qui souhaite visiter le DecisionTree
	 */
	void accept(DecisionTreeVisitor dtv);
	
	/** Permet de savoir si on peut encore assigner un arbre au DecisionTree
	 * @see DecisionNodeSetter
	 * @see DecisionTreeQuestion#getYesTree()
	 * 
	 * @return true si et seulement si on peut assigner l'arbre
	 */
	boolean canOverwrite();
}
