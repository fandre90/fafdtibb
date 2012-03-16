package fr.insarennes.fafdti.visitors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.DecisionTreeVisitor;

public class StatNumExamplesClassified implements DecisionTreeVisitor {

	private static Logger log = Logger.getLogger(StatNumExamplesClassified.class);
	private int nbExamplesClassified;
	private int nbExamplesTotal;
	
	public StatNumExamplesClassified(int total){
		nbExamplesTotal = total;
		nbExamplesClassified = 0;
	}
	public double getResult(){
		return (double)nbExamplesClassified/(double)nbExamplesTotal;
	}
	@Override
	public void visitQuestion(DecisionTreeQuestion dtq) {
		dtq.getYesTree().accept(this);
		dtq.getNoTree().accept(this);
		
	}

	@Override
	public void visitLeaf(DecisionTreeLeaf dtl) {
		try {
			nbExamplesClassified+=dtl.getNbClassified();
		} catch (FAFException e) {
			log.log(Level.ERROR, e.getMessage());
		}
	}

	@Override
	public void visitPending(DecisionTreePending dtp)
			throws InvalidCallException {
		
	}

}
