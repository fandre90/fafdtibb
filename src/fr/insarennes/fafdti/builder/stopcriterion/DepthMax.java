package fr.insarennes.fafdti.builder.stopcriterion;

public class DepthMax implements StoppingCriterion {

	private int depthMax;
	
	public DepthMax(int depth){
		depthMax = depth;
	}
	@Override
	public boolean mustStop(StopCriterionUtils node) {
		return node.getDepth() >= depthMax;
	}

}
