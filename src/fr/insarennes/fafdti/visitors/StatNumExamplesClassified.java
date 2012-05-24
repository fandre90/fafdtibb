package fr.insarennes.fafdti.visitors;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.DecisionTreeVisitor;
/**
 * Classe expérimentale qui peut servir à parcourir l'arbre afin de compter le nombre
 * d'exemples déjà classifier dans les feuilles, et de récupérer en plus la liste
 * des sous-arbre encore en construction afin de relancer des visites sur ceux-ci.
 * 
 * L'utilité de cette classe reste à prouver !!! ;-)
 */
public class StatNumExamplesClassified implements DecisionTreeVisitor {

	private static Logger log = Logger.getLogger(StatNumExamplesClassified.class);
	private int nbExamplesClassified;
	private List<DecisionTree> pending;
	
	public StatNumExamplesClassified(){
		nbExamplesClassified = 0;
		pending = new ArrayList<DecisionTree>();
	}
	public int getResult(){
		return nbExamplesClassified;
	}
	public List<DecisionTree> getPending(){
		//utils pour relancer des visites seulement sur ceux qui n'a pas encore été visité
		return pending;
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
		pending.add(dtp);
	}

}
