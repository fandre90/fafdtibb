package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.Question;

public class DecisionTreeQuestion implements DecisionTree {
	private DecisionTree _yesTree;
	private DecisionTree _noTree;
	private Question _question;
	private String label;
	public DecisionTreeQuestion() {
		_yesTree = new DecisionTreePending();
		_noTree = new DecisionTreePending();
	}
	public DecisionTreeQuestion(Question q, DecisionTree dtyes, DecisionTree dtno, String lbl) {
		_yesTree = dtyes;
		_noTree = dtno;
		_question = q;
		label = lbl;
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
		return new DecisionNodeSetter(_yesTree);
	}
	public DecisionNodeSetter noSetter(){
		return new DecisionNodeSetter(_noTree);
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
	public String getLabel() {
		return label;
	}

}
