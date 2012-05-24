package fr.insarennes.fafdti.hadoop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;

public class Step2Red
		extends
		ReducerBase<IntWritable, ContinuousAttrLabelPair, Question, ScoreLeftDistribution> {

	protected ScoredDistributionVector parentDistribution;
	public static double EPSILON = 10e-9;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		this.parentDistribution = ScoredDistributionVector.fromConf(conf);

	}

	protected void reduce(IntWritable col,
			Iterable<ContinuousAttrLabelPair> valueLabelPairs, Context context)
			throws IOException, InterruptedException {
		SortedMap<Double, ScoredDistributionVector> valueDistMap = new TreeMap<Double, ScoredDistributionVector>();
		Iterator<ContinuousAttrLabelPair> vlPairIt = valueLabelPairs.iterator();
		// 1 Iterate over all values/label pairs and create distribution
		// vectors for each normalized (ie rounded, see normalizeValue) value
		while (vlPairIt.hasNext()) {
			ContinuousAttrLabelPair vlPair = vlPairIt.next();
			double normalizedValue = normalizeValue(
					vlPair.getContinuousValue(), EPSILON);
			ScoredDistributionVector curDist = null;
			// 1.1 Get the distribution vector for this value
			// for the map or create a new one if it was not yet
			// added to it.
			if (valueDistMap.containsKey(normalizedValue)) {
				curDist = valueDistMap.get(normalizedValue);
			} else {
				curDist = new ScoredDistributionVector(fs.numOfLabel());
				valueDistMap.put(normalizedValue, curDist);
			}
			// 1.2 Increment value in the distribution vector for
			// current label
			curDist.incrStat(vlPair.getLabelIndex());
		}
		// 2. Find the best threshold
		Set<Map.Entry<Double, ScoredDistributionVector>> valueDistPairs = valueDistMap
				.entrySet();
		// Return imediately : It is impossible to generate a threshold
		if (valueDistMap.size() < 2) {
			return;
		}
		// Get an iterator over the set of value/label pairs
		Iterator<Map.Entry<Double, ScoredDistributionVector>> valDistIt = valueDistPairs
				.iterator();
		Map.Entry<Double, ScoredDistributionVector> curValDistPair = valDistIt
				.next();
		double prevValue = curValDistPair.getKey();
		ScoredDistributionVector curDistVect = new ScoredDistributionVector(
				fs.numOfLabel());
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
		Question question = new Question(col.get(), AttrType.CONTINUOUS,
				bestThreshold);
		context.write(question, bestScoreLeftDist);
	}

	public static double normalizeValue(double value, double threshold) {
		double a = 1.0 / threshold;
		return Math.floor(value * a) / a;
	}
}