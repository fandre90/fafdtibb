/** Classe représentant un arbre en cours de construction
 * Cette classe est utilisé pour assigner les fils d'un noeud en attendant qu'il
 * soit effectivement construit et affecté par le DecisionNodeSetter
 */

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
