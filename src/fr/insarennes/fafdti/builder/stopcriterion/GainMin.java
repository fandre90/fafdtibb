package fr.insarennes.fafdti.builder.stopcriterion;

public class GainMin implements StoppingCriterion {

	private double gainMin;
	
	public GainMin(double gain){
		gainMin = gain;
	}
	@Override
	public boolean mustStop(StopCriterionUtils node) {
		return node.getCurrentGain() < gainMin;
	}

}
