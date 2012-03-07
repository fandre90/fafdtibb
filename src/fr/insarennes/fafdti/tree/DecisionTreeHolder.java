package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.FAFException;

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
