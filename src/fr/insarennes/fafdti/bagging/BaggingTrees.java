package fr.insarennes.fafdti.bagging;

import java.util.ArrayList;
import java.util.List;

import fr.insarennes.fafdti.tree.DecisionTree;

public class BaggingTrees {

	private List<DecisionTree> trees;
	
	public BaggingTrees(){
		trees = new ArrayList<DecisionTree>();
	}
	
	public BaggingTrees(int nbTrees){
		trees = new ArrayList<DecisionTree>(nbTrees);
	}
	
	public BaggingTrees(List<DecisionTree> trees){
		this.trees = trees;
	}
	
	public DecisionTree getTree(int index){
		return trees.get(index);
	}
	
	public void setTree(DecisionTree tree){
		trees.add(tree);
	}
	
	public void setTree(int index, DecisionTree tree){
		trees.add(index, tree);
	}
	
	public int getSize(){
		return trees.size();
	}
}
