/** Classe DecisionTreeQuestion implémentant DecisionTree
 * Cette classe représente un noeud binaire de l'arbre de décision
 * Elle encapsule la question attachée à ce noeud et une référence vers chacun de ses fils
 */

package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.builder.Question;


public class DecisionTreeQuestion implements DecisionTree {
	private DecisionTree _yesTree;
	private DecisionTree _noTree;
	private Question _question;

	public DecisionTreeQuestion(Question q){
		_yesTree = new DecisionTreePending();
		_noTree = new DecisionTreePending();
		_question = q;
	}
	public DecisionTreeQuestion(Question q, DecisionTree dtyes, DecisionTree dtno) {
		_yesTree = dtyes;
		_noTree = dtno;
		_question = q;
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
				if(_yesTree.canOverwrite()){
					_yesTree = node;
				} else {
					throw new CannotOverwriteTreeException();
				}
			}
			
		};
	}
	public DecisionNodeSetter noSetter(){
		return new DecisionNodeSetter() {
			public void set(DecisionTree node) throws CannotOverwriteTreeException{
				if(_noTree.canOverwrite()){
					_noTree = node;
				} else {
					throw new CannotOverwriteTreeException();
				}
			}
			
		};
	}
	public Question getQuestion() {
		return _question;
	}
	public DecisionTree getYesTree() {
		return _yesTree;
	}
	public DecisionTree getNoTree() {
		return _noTree;
	}
}
