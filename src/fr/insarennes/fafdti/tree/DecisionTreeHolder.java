package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.FAFException;
/** Classe contenant simplement un DecisionTree
 * Elle sert à l'initialisation du processus de construction, afin de fournir un
 * containeur permettant de stocker la racine de l'arbre et de fournir un DecisionNodeSetter
 */
public class DecisionTreeHolder {
	DecisionTree root;
	boolean done;
	
	public DecisionTreeHolder(){
		root = new DecisionTreePending();
		done = false;
	}
	
	public DecisionNodeSetter getNodeSetter(){
		return new DecisionNodeSetter(){
			public void set(DecisionTree tree) throws CannotOverwriteTreeException{
				if(!done) {
					root = tree;
					done = true;
				}
				else throw new CannotOverwriteTreeException();
			}
		};
	}
	
	public DecisionTree getRoot()throws FAFException{
		if(!done) throw new FAFException("Cannot return root because it's pending !");
		return root;
	}
}
