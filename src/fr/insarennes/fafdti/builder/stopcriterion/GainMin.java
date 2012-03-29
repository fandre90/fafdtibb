package fr.insarennes.fafdti.builder.stopcriterion;

import org.apache.log4j.Logger;

public class GainMin implements StoppingCriterion {

	private static Logger log = Logger.getLogger(GainMin.class);
	private double gainMin;
	
	public GainMin(double gain){
		gainMin = gain;
	}
	@Override
	public boolean mustStop(StopCriterionUtils node) {
		boolean res = node.getCurrentGain() < gainMin;
		if(res)
			log.info("stopping criterion : gain min");
		return res;
	}

}
