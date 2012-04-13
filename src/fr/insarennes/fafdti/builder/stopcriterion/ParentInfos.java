/**
 * Classe encapsulant différentes informations utiles à la construction d'un DecisionTree
 * sur son parent (tel que la hauteur de son parent ou son identifiant)
 */

package fr.insarennes.fafdti.builder.stopcriterion;

public class ParentInfos {

	private int depth;
	private String id;
	private String baggingId;
	
	public ParentInfos(int depth, String id, String baggingId){
		this.depth = depth;
		this.id = id;
		this.baggingId = baggingId;
	}
	
	public int getDepth(){
		return depth;
	}
	
	public String getId(){
		return id;
	}
	
	public String getBaggingId(){
		return baggingId;
	}

	
}
