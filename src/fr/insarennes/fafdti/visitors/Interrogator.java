package fr.insarennes.fafdti.visitors;

import java.util.HashMap;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.DecisionTreeVisitor;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class Interrogator implements DecisionTreeVisitor {

	private QuestionExample qExample;
	private LeafLabels labels;
	
	public Interrogator(QuestionExample qe){
		qExample = qe;
		HashMap<String,Double> lbls = new HashMap<String,Double>();
		lbls.put("warning -- unbuild", 1.0);
		try {
			labels = new LeafLabels(lbls);
		} catch (InvalidProbabilityComputationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
