package fr.insarennes.fafdti.builder;

public class ParentInfos {

	private double entropy;
	private int depth;
	
	public ParentInfos(double entropy, int depth){
		this.entropy = entropy;
		this.depth = depth;
	}
	
	public double getEntropy(){
		return entropy;
	}
	
	public int getDepth(){
		return depth;
	}
	
}
