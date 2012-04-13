/** Interface DecisionNodeSetter qui offre le service d'assigner un DecisionTree
 * à l'objet propriétaire du DecisionNodeSetter à travers la méthode set()
 */

package fr.insarennes.fafdti.tree;

public abstract class DecisionNodeSetter {

	
	public DecisionNodeSetter(){
		
	}
	
	public void set(DecisionTree node) throws CannotOverwriteTreeException {
		
	}
}
