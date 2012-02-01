package fr.insarennes.fafdti.tree;

public class DecisionTreeQuestion implements DecisionTree {
	private DecisionTree _yesTree;
	private DecisionTree _noTree;
	private Question _question;
	public DecisionTreeQuestion() {
		_yesTree = new DecisionTreePending();
		_noTree = new DecisionTreePending();
		_question = new Question();
	}
	public DecisionTreeQuestion(Question q, DecisionTree dtyes, DecisionTree dtno) {
		_question = q;
		_yesTree = dtyes;
		_noTree = dtno;
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

}
