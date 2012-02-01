package fr.insarennes.fafdti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

public class Step2Red extends
		ReducerBase<IntWritable, ContinuousAttrLabelPair, Question, ScoredDistributionVector> {	

	public static double EPSILON = 10e-9;
	
	protected void reduce(IntWritable col, Iterable<ContinuousAttrLabelPair> valueLabelPairs,
			Context context) throws IOException, InterruptedException {
		List<Double> candidates = computeThresholdCandidates(valueLabelPairs);
	
		double bestScore = 0;
		double bestThreshold = 0;
		ScoredDistributionVector outDistributon = null;
		for(int i=0; i < candidates.size(); i++) {
			double threshold = candidates.get(i);
			ScoreLeftDistribution scoreLeftDist = 
					computeEntropyForThreshold(valueLabelPairs, threshold);
			double score = scoreLeftDist.getScore();
			// FIXME is score1 better then score2 
			// if score1 > score2 or if score1 < score2
			if(outDistributon == null
				|| score > bestScore) {
				bestScore = score;
				bestThreshold = threshold;
				outDistributon = scoreLeftDist.getDistribution();
			}
		}
		Question question = 
				new Question(col.get(), AttrType.CONTINUOUS, bestThreshold);
		context.write(question, outDistributon);
	}
	
	private static List<Double> computeThresholdCandidates(
		Iterable<ContinuousAttrLabelPair> valueLabelPairs) {
		List<Double> values = new ArrayList<Double>();
		List<Double> valuesUnique = new ArrayList<Double>();
		List<Double> candidates = new ArrayList<Double>();
		
		// 1. Build the ArrayList containing all possible
		// values for the current attribute (transmitted as Hadoop key)
		// from the list of (continuous value, label) pairs 
		// (transmitted as Hadoop list of values).
		for(ContinuousAttrLabelPair vlPair : valueLabelPairs) {
			values.add(vlPair.getContinuousValue());
		}
		// 2. Sort the ArrayList containing all values O(n log2 n)
		Collections.sort(values);
		// 3. Remove duplicates from the ArrayList O(n)
		// Important note : Two values are considered identical
		// if their difference is less than THRESHOLD
		Iterator<Double> valuesIt = values.iterator();
		// Return empty candidates list if there are no values
		// (this should never happen)
		if(! valuesIt.hasNext()) {
			return candidates;
		}
		double prevValue = valuesIt.next();
		valuesUnique.add(prevValue);
		while(valuesIt.hasNext()) {
			double curValue = valuesIt.next();
			if(curValue - prevValue > EPSILON) {
				valuesUnique.add(curValue);
			}
		}
		// Build threshold candidates list
		// Median of the elements two at a time
		Iterator<Double> valuesUniqueIt = valuesUnique.iterator();
		if(! valuesUniqueIt.hasNext()) {
			return candidates;
		}
		prevValue = valuesUniqueIt.next();
		while(valuesUniqueIt.hasNext()) {
			double curValue = valuesUniqueIt.next();
			candidates.add((curValue+prevValue)/2);
		}
		return candidates;
	}
	private ScoreLeftDistribution computeEntropyForThreshold(
			Iterable<ContinuousAttrLabelPair> valueLabelPairs,
			double threshold) {
		ScoredDistributionVector leftDist = 
				new ScoredDistributionVector(fs.nbEtiquettes());
		ScoredDistributionVector rightDist = 
				new ScoredDistributionVector(fs.nbEtiquettes());
		for(ContinuousAttrLabelPair vlPair : valueLabelPairs) {
			int labelIndex = vlPair.getLabelIndex();
			double contValue = vlPair.getContinuousValue();
			if(contValue - threshold > EPSILON) {
				leftDist.incrStat(labelIndex);
			} else {
				rightDist.incrStat(labelIndex);
			}
		}
		leftDist.rate(criterion);
		rightDist.rate(criterion);
		
		// FIXME : Toujours la même manière d'agréger l'entropie ?
		int nl = leftDist.getTotal();
		int nr = rightDist.getTotal();
		int N = nl + nr;
		double score = (nl * leftDist.getScore() + nr * rightDist.getScore()) / N;
		return new ScoreLeftDistribution(score, leftDist);
	}
}