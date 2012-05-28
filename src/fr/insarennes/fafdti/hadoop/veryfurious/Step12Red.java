package fr.insarennes.fafdti.hadoop.veryfurious;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.builder.nodebuilder.BestQuestionSelector;
import fr.insarennes.fafdti.builder.nodebuilder.ThresholdComputer;
import fr.insarennes.fafdti.hadoop.ReducerBase;
import fr.insarennes.fafdti.hadoop.ValueDistributionMapAggregator;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;

public class Step12Red
		extends
		ReducerBase<IntWritable, WritableValueSDVSortedMap, Question, ScoreLeftDistribution> {

	private ScoredDistributionVector parentDistribution;
	private BestQuestionSelector bestSelect;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		this.parentDistribution = ScoredDistributionVector.fromConf(conf);

	}

	protected void reduce(
			IntWritable col,
			Iterable<WritableValueSDVSortedMap> valueDistMaps,
			Context context) throws IOException, InterruptedException {
		ValueDistributionMapAggregator valueDistMapAgg = new ValueDistributionMapAggregator();
		try {
			valueDistMapAgg.aggregateAll(valueDistMaps);
			WritableValueSDVSortedMap aggregatedMap = valueDistMapAgg
					.getAggregatedMap();
			if (aggregatedMap.size() >= 2) {
				ThresholdComputer thresholdComputer = new ThresholdComputer(fs,
						parentDistribution, criterion);
				Pair<Double, ScoreLeftDistribution> p = thresholdComputer
						.computeThreshold(aggregatedMap);
				Question question = new Question(col.get(),
						AttrType.CONTINUOUS, p.getFirst());
				writeIfBestQuestion(context, question, p.getSecond());
			}
		} catch (FAFException e) {
			e.printStackTrace();
		}
	}

	private void writeIfBestQuestion(Context context, Question q,
			ScoreLeftDistribution sLDist) throws IOException,
			InterruptedException {
		if(bestSelect.addCandidate(q, sLDist)) {
			context.write(q, sLDist);
		}
	}
}