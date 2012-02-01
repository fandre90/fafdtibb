package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.FAFException;
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
		label = dtl.getLabel();
	}
	
	@Override
	public void visitPending(DecisionTreePending dtl) throws InvalidCallException{
		throw new InvalidCallException();
	}
	public String getLabel() throws FAFException{
		if(label.equals("__"))	throw new FAFException("Asking result but questionning failed");
		return label;
	}
}
