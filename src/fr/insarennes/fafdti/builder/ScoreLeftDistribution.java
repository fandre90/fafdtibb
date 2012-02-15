package fr.insarennes.fafdti.builder;


public class ScoreLeftDistribution {
	private double score;
	private ScoredDistributionVector distribution;
	
	public ScoreLeftDistribution(double score,
			ScoredDistributionVector distribution) {
		super();
		this.score = score;
		this.distribution = distribution;
	}

	public double getScore() {
		return score;
	}

	public ScoredDistributionVector getDistribution() {
		return distribution;
	}
}
