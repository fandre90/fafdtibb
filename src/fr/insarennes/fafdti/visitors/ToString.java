/** Visiteur permettant de générer une String représentant l'arbre
 */

package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.tree.*;

public class ToString implements DecisionTreeVisitor{
	private String str;
	
	
	public ToString(){
		str = new String("");
	}
	public void visitQuestion(DecisionTreeQuestion dtq) {
		str += "Question node : "+dtq.getQuestion().toString()+"  {\n";
		str += "Yes son : ";
		dtq.getYesTree().accept(this);
		str += "No son : ";
		dtq.getNoTree().accept(this);
		str += "\n}";		
	}

	public void visitLeaf(DecisionTreeLeaf dtl) {
		str += "Leaf : \n"+dtl.getLabels().toString();		
	}

	public void visitPending(DecisionTreePending dtp) {
		str += "Pending\n";
		
	}
	public String getStr() {
		return str;
	}

}
