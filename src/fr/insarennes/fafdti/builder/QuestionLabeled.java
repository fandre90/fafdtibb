package fr.insarennes.fafdti.builder;

import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;

/**
 * 
 * @author momo
 * Classe utilisée pour construire une question lorsque l'on importe un arbre xml
 * Elle permet de sauvegarder en plus le nom de l'attribut de la question utilisé pour l'export dot/png
 */
public class QuestionLabeled extends Question {

	String name;
	
	public QuestionLabeled(int col, AttrType type, double value, String name){
		super(col,type,value);
		this.name = name;
	}
	public QuestionLabeled(int col, AttrType type, String value, String name) {
		super(col,type,value);
		this.name = name;
	}
	public QuestionLabeled(int col, AttrType type, SGram value, String name) {
		super(col,type,value);
		this.name = name;
	}
	public QuestionLabeled(int col, AttrType type, FGram value, String name) {
		super(col,type,value);
		this.name = name;
	}
	public String getName(){
		return this.name;
	}
}
