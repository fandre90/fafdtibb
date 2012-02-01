package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.Question;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.DecisionTreeVisitor;

public class Interrogator implements DecisionTreeVisitor {

	private QuestionExample qExample;
	private String label;
	
	public Interrogator(QuestionExample qe){
		qExample = qe;
		label = new String("__");
	}
	@Override
	public void visitQuestion(DecisionTreeQuestion dtq) {
		Question q = dtq.getQuestion();
		if(q.ask(qExample.getValue(dtq.getLabel())))	
				dtq.getYesTree().accept(this);
		else	dtq.getNoTree().accept(this);
	}

	@Override
	public void visitLeaf(DecisionTreeLeaf dtl) {
		label = dtl.getLabel();
	}
	
	@Override
	public void visitPending(DecisionTreePending dtl) throws InvalidCallException{
		throw new InvalidCallException();
	}
	public String getLabel(){
		return label;
	}
}
