package fr.insarennes.fafdti.visitors;

import java.util.Map;

//Implémente la structure de données stockant une question sur un arbre
public class QuestionExample {
	/* clé = label, valeur = valeur de l'attribut de l'exemple posé*/
	private Map<String,String> qExample;
	
	public String getValue(String label) {
		return qExample.get(label);
	}

}
