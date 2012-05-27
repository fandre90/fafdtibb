package fr.insarennes.fafdti.builder.nodebuilder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.hadoop.Value;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;

public class ThresholdComputer {
	private Criterion criterion;
	private ScoredDistributionVector parentDistribution;
	private DotNamesInfo namesInfo;

	public static final double EPSILON = 10e-9;
	private static class ValueGetter {
		public double getValue(Value v) {
			return v.getDoubleValue();
		}
	}
	public ThresholdComputer(DotNamesInfo namesInfo, ScoredDistributionVector
			parentDistribution, Criterion criterion) {
		this.criterion = criterion;
		this.parentDistribution = parentDistribution;
		this.namesInfo = namesInfo;
	}

	public Pair<Double, ScoreLeftDistribution> computeThreshold(
			WritableValueSDVSortedMap valueDistMap) throws FAFException {
		return computeThreshold(valueDistMap, new ValueGetter());
	}
	
	public Pair<Double, ScoreLeftDistribution> computeThreshold(
			SortedMap<Double, ScoredDistributionVector> valueDistMap) throws FAFException {
		return computeThreshold(valueDistMap, null);
	}
	
	public Pair<Double, ScoreLeftDistribution> computeThreshold(
			SortedMap valueDistMap, ValueGetter vg)
			throws FAFException {
		Set<Map.Entry> valueDistPairs = valueDistMap
				.entrySet();
		// Return imediately : It is impossible to generate a threshold
		if (valueDistMap.size() < 2) {
			throw new FAFException("Impossible to compute threshold with "
					+ "less than 2 values");
		}
		Iterator<Map.Entry> valDistIt = valueDistPairs
				.iterator();
		Map.Entry curValDistPair = valDistIt
				.next();
		double prevValue = 0;
		if(vg == null) {
			prevValue = (Double) curValDistPair.getKey();
		} else {
			prevValue = vg.getValue( (Value) curValDistPair.getKey());
		}
		ScoredDistributionVector curDistVect = new ScoredDistributionVector(
				namesInfo.numOfLabel());
		try {
			curDistVect.add( (ScoredDistributionVector) curValDistPair.getValue());
		} catch (FAFException e) {
			e.printStackTrace();
		}
		double bestThreshold = 0;
		ScoreLeftDistribution bestScoreLeftDist = null;
		while (valDistIt.hasNext()) {
			curValDistPair = valDistIt.next();
			double curValue = 0;
			if(vg == null) {
				curValue = (Double) curValDistPair.getKey();
			} else {
				curValue = vg.getValue( (Value) curValDistPair.getKey());
			}
			double threshold = (prevValue + curValue) / 2;
			prevValue = curValue;
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
				curDistVect.add((ScoredDistributionVector) curValDistPair.getValue());
			} catch (FAFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new Pair<Double, ScoreLeftDistribution>(
				bestThreshold, bestScoreLeftDist);
	}
	
	public static double normalizeValue(double value, double threshold) {
		double a = 1.0 / threshold;
		return Math.floor(value * a) / a;
	}
}
