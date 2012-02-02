package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Question;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.DecisionTreeVisitor;
import fr.insarennes.fafdti.tree.LeafLabels;

public class Interrogator implements DecisionTreeVisitor {

	private QuestionExample qExample;
	private LeafLabels labels;
	
	public Interrogator(QuestionExample qe){
		qExample = qe;
		labels = new LeafLabels();
	}
	@Override
	public void visitQuestion(DecisionTreeQuestion dtq) {
		Question q = dtq.getQuestion();
		try {
			if(q.ask(qExample.getValue(q.getCol())))	
					dtq.getYesTree().accept(this);
			else	dtq.getNoTree().accept(this);
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void visitLeaf(DecisionTreeLeaf dtl) {
		labels = dtl.getLabels();
	}
	
	@Override
	public void visitPending(DecisionTreePending dtl) throws InvalidCallException{
		throw new InvalidCallException(this.getClass().getName()+" cannot visit a DecisionTreePending");
	}
	public LeafLabels getResult(){
		return labels;
	}
}
