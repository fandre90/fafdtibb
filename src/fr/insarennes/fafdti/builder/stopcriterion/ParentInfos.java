package fr.insarennes.fafdti.builder.stopcriterion;

public class ParentInfos {

	private int depth;
	private String id;
	
	public ParentInfos(int depth, String id){
		this.depth = depth;
		this.id = id;
	}
	
	public int getDepth(){
		return depth;
	}
	
	public String getId(){
		return id;
	}

	
}
