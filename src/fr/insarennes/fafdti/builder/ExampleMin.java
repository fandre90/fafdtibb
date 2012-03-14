package fr.insarennes.fafdti.builder;

public class ExampleMin implements StoppingCriterion {

	private int exampleMin;
	
	public ExampleMin(int example){
		exampleMin = example;
	}
	public boolean mustStop(StopCriterionUtils node) {
		return node.getMinExamples() < exampleMin;
	}

}
