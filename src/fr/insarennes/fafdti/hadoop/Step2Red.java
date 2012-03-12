package fr.insarennes.fafdti.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

// FIXME FIXME FIXME Optimisation à faire :
// Stocker dans une Map (Attention elle doit être triée :
// [0,3 : (1, 1) ]
public class Step2Red
		extends
		ReducerBase<IntWritable, ContinuousAttrLabelPair, Question, ScoreLeftDistribution> {

	public static double EPSILON = 10e-9;

	protected void reduce(IntWritable col,
			Iterable<ContinuousAttrLabelPair> valueLabelPairs, Context context)
			throws IOException, InterruptedException {
		List<ContinuousAttrLabelPair> valueLabelPairsList = 
				new ArrayList<ContinuousAttrLabelPair>();
		Iterator<ContinuousAttrLabelPair> vlPairIt = valueLabelPairs.iterator();
		while (vlPairIt.hasNext()) {
			ContinuousAttrLabelPair vlPair = (ContinuousAttrLabelPair) vlPairIt.next().clone();
			valueLabelPairsList.add(vlPair);
			//System.out.println(valueLabelPairsList);
			//System.out.println(vlPair);
		}
		//System.out.println("vlpl: " + valueLabelPairsList);
		List<Double> candidates = 
				computeThresholdCandidates(valueLabelPairsList);

		double bestScore = 0;
		double bestThreshold = 0;
		ScoreLeftDistribution bestScoreLeftDist = null;
		for (int i = 0; i < candidates.size(); i++) {
			double threshold = candidates.get(i);
			ScoreLeftDistribution scoreLeftDist = computeEntropyForThreshold(
					valueLabelPairsList, threshold);
			double score = scoreLeftDist.getScore();
			if (bestScoreLeftDist == null || score < bestScore) {
				bestScore = score;
				bestThreshold = threshold;
				bestScoreLeftDist = (ScoreLeftDistribution) scoreLeftDist.clone();
			}
		}
		Question question = new Question(col.get(), AttrType.CONTINUOUS,
				bestThreshold);
		context.write(question, bestScoreLeftDist);
	}

	private static List<Double> computeThresholdCandidates(
			List<ContinuousAttrLabelPair> valueLabelPairs) {
		List<Double> values = new ArrayList<Double>();
		List<Double> valuesUnique = new ArrayList<Double>();
		List<Double> candidates = new ArrayList<Double>();
		//System.out.println("vl: " + valueLabelPairs);
		// 1. Build the ArrayList containing all possible
		// values for the current attribute (transmitted as Hadoop key)
		// from the list of (continuous value, label) pairs
		// (transmitted as Hadoop list of values).
		for (ContinuousAttrLabelPair vlPair : valueLabelPairs) {
			values.add(vlPair.getContinuousValue());
		}
		// 2. Sort the ArrayList containing all values O(n log2 n)
		Collections.sort(values);
		//System.out.println(values);
		// 3. Remove duplicates from the ArrayList O(n)
		// Important note : Two values are considered identical
		// if their difference is less than THRESHOLD
		Iterator<Double> valuesIt = values.iterator();
		// Return empty candidates list if there are no values
		// (this should never happen)
		if (!valuesIt.hasNext()) {
			return candidates;
		}
		double prevValue = valuesIt.next();
		valuesUnique.add(prevValue);
		while (valuesIt.hasNext()) {
			double curValue = valuesIt.next();
			if (curValue - prevValue > EPSILON) {
				valuesUnique.add(curValue);
			}
			prevValue = curValue;
		}
		// Build threshold candidates list
		// Median of the elements two at a time
		Iterator<Double> valuesUniqueIt = valuesUnique.iterator();
		if (!valuesUniqueIt.hasNext()) {
			return candidates;
		}
		prevValue = valuesUniqueIt.next();
		while (valuesUniqueIt.hasNext()) {
			double curValue = valuesUniqueIt.next();
			candidates.add((curValue + prevValue) / 2);
			prevValue = curValue;
		}
		//System.out.println("threshold candidates : " + candidates);
		return candidates;
	}

	private ScoreLeftDistribution computeEntropyForThreshold(
			List<ContinuousAttrLabelPair> valueLabelPairs, double threshold) {
		//System.out.println("Compute ent for thresh: " + threshold);
		//System.out.println("NbEtiq: " + fs.nbEtiquettes());
		ScoredDistributionVector leftDist = new ScoredDistributionVector(
				fs.nbEtiquettes());
		ScoredDistributionVector rightDist = new ScoredDistributionVector(
				fs.nbEtiquettes());
		for (ContinuousAttrLabelPair vlPair : valueLabelPairs) {
			int labelIndex = vlPair.getLabelIndex();
			double contValue = vlPair.getContinuousValue();
			//System.out.println("lbIdx, v: " + labelIndex + "," + contValue);
			if (contValue - threshold > EPSILON) {
				leftDist.incrStat(labelIndex);
			} else {
				rightDist.incrStat(labelIndex);
			}
		}
		leftDist.rate(criterion);
		rightDist.rate(criterion);
		return new ScoreLeftDistribution(leftDist, rightDist);
	}
}