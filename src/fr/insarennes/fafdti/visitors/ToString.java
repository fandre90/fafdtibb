package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.tree.*;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class ToString implements DecisionTreeVisitor{
	private String str;
	
	
	public ToString(){
		str = new String("");
	}
	public void visitQuestion(DecisionTreeQuestion dtq) {
		str += "Question node : "+dtq.getQuestion().toString()+"  {\n";
		str += "Yes son : ";
		dtq.getYesTree().accept(this);
		str += "No son :";
		dtq.getNoTree().accept(this);
		str += "\n}";		
	}

	public void visitLeaf(DecisionTreeLeaf dtl) {
		try {
			str += "Leaf : \n"+dtl.getLabels().toStr();
		} catch (InvalidProbabilityComputationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void visitPending(DecisionTreePending dtp) {
		str += "Pending\n";
		
	}
	public String getStr() {
		return str;
	}

}
