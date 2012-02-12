package fr.insarennes.fafdti.visitors;

import java.util.List;

//Implémente la structure de données stockant une question sur un arbre
public class QuestionExample {
	private List<String> qExample;
	
	public QuestionExample(List<String> ex){
		qExample = ex;
	}
	public String getValue(int label) {
		return qExample.get(label);
	}
	
	public String toString(){
		String res = "";
		for(int i=0 ; i<qExample.size() ; i++)
			res+="Feature "+i+", Value="+qExample.get(i)+"\n";
		return res;
		
	}

}
