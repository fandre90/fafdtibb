package fr.insarennes.fafdti.tree;

public class DecisionNodeSetter {

	DecisionTree _tree;
	public DecisionNodeSetter(DecisionTree dt){
		_tree = dt;
	}
	public void set(DecisionTree node) throws CannotOverwriteTreeException{
		if(_tree.canOverwrite()){
			_tree = node;
		} else {
			throw new CannotOverwriteTreeException();
		}
	}
}
