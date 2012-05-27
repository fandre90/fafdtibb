package fr.insarennes.fafdti.builder.nodebuilder;

import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;

public class BestQuestionSelector {
	private Question bestQuestion;
	private ScoreLeftDistribution bestSLDist;

	public BestQuestionSelector() {
		bestQuestion = null;
		bestSLDist = null;
	}
	
	public boolean addCandidate(Question q, ScoreLeftDistribution sLDist) {
		if (bestQuestion == null
				|| sLDist.getScore() < bestSLDist.getScore()) {
			bestQuestion = q;
			bestSLDist = sLDist;
			return true;
		}
		return false;
	}
	
	public Question getBestQuestion() {
		return this.bestQuestion;
	}

	public ScoreLeftDistribution getBestScoreLeftDistribution() {
		return this.bestSLDist;
	}
}
