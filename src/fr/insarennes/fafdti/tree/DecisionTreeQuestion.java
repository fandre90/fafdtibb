package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.builder.Question;
/** Classe DecisionTreeQuestion implémentant DecisionTree
 * Cette classe représente un noeud binaire de l'arbre de décision
 * Elle encapsule la question attachée à ce noeud et une référence vers chacun de ses fils
 */

public class DecisionTreeQuestion implements DecisionTree {
	protected DecisionTree yesTree;
	protected DecisionTree noTree;
	protected Question question;

	public DecisionTreeQuestion(Question q){
		yesTree = new DecisionTreePending();
		noTree = new DecisionTreePending();
		question = q;
	}
	public DecisionTreeQuestion(Question q, DecisionTree dtyes, DecisionTree dtno) {
		yesTree = dtyes;
		noTree = dtno;
		question = q;
	}
	@Override
	public void accept(DecisionTreeVisitor dtv) {
		dtv.visitQuestion(this);
		
	}

	@Override
	public boolean canOverwrite() {
		return false;
	}
	
	public DecisionNodeSetter yesSetter(){
		return new DecisionNodeSetter() {
			public void set(DecisionTree node) throws CannotOverwriteTreeException{
				if(yesTree.canOverwrite()){
					yesTree = node;
				} else {
					throw new CannotOverwriteTreeException();
				}
			}
			
		};
	}
	public DecisionNodeSetter noSetter(){
		return new DecisionNodeSetter() {
			public void set(DecisionTree node) throws CannotOverwriteTreeException{
				if(noTree.canOverwrite()){
					noTree = node;
				} else {
					throw new CannotOverwriteTreeException();
				}
			}
			
		};
	}
	public Question getQuestion() {
		return question;
	}
	public DecisionTree getYesTree() {
		return yesTree;
	}
	public DecisionTree getNoTree() {
		return noTree;
	}
}
