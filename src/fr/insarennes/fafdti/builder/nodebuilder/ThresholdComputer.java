package fr.insarennes.fafdti.builder.nodebuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class ThresholdComputer {
	private Criterion criterion;
	private ScoredDistributionVector parentDistribution;
	private DotNamesInfo namesInfo;

	public ThresholdComputer(DotNamesInfo namesInfo, ScoredDistributionVector
			parentDistribution, Criterion criterion) {
		this.criterion = criterion;
		this.parentDistribution = parentDistribution;
		this.namesInfo = namesInfo;
	}

	public Pair<Double, ScoreLeftDistribution> computeThreshold(
			SortedMap<Double, ScoredDistributionVector> valueDistMap)
			throws FAFException {
		Set<Map.Entry<Double, ScoredDistributionVector>> valueDistPairs = valueDistMap
				.entrySet();
		// Return imediately : It is impossible to generate a threshold
		if (valueDistMap.size() < 2) {
			throw new FAFException("Impossible to compute threshold with "
					+ "less than 2 values");
		}
		Iterator<Map.Entry<Double, ScoredDistributionVector>> valDistIt = valueDistPairs
				.iterator();
		Map.Entry<Double, ScoredDistributionVector> curValDistPair = valDistIt
				.next();
		double prevValue = curValDistPair.getKey();
		ScoredDistributionVector curDistVect = new ScoredDistributionVector(
				namesInfo.numOfLabel());
		try {
			curDistVect.add(curValDistPair.getValue());
		} catch (FAFException e) {
			e.printStackTrace();
		}
		double bestThreshold = 0;
		ScoreLeftDistribution bestScoreLeftDist = null;
		while (valDistIt.hasNext()) {
			curValDistPair = valDistIt.next();
			double threshold = (prevValue + curValDistPair.getKey()) / 2;
			prevValue = curValDistPair.getKey();
			curDistVect.rate(criterion);
			ScoredDistributionVector rightDist = parentDistribution
					.computeRightDistribution(curDistVect);
			rightDist.rate(criterion);
			// TEST REMOVE THIS WHEN TESTS ARE FINISHED
			for(Integer i: rightDist.getDistributionVector()) {
				if(i < 0) {
					System.out.println(i+"");
					System.out.println("Something went wrong.");
					System.out.println("Parent: " + parentDistribution.toString());
					System.out.println("Left: " + curDistVect);
					System.exit(127);
				}
			}
			double curScore = ScoreLeftDistribution.computeCombinedEntropy(
					curDistVect, rightDist);
			if (bestScoreLeftDist == null
					|| curScore < bestScoreLeftDist.getScore()) {
				bestScoreLeftDist = new ScoreLeftDistribution(curScore,
						(ScoredDistributionVector) curDistVect.clone());
				bestThreshold = threshold;
			}
			try {
				curDistVect.add(curValDistPair.getValue());
			} catch (FAFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Pair<Double, ScoreLeftDistribution>(
				bestThreshold, bestScoreLeftDist);
	}
}
