package fr.insarennes.fafdti.hadoop.furious;

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
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.namesinfo.AttrSpec;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.builder.namesinfo.TextAttrSpec;
import fr.insarennes.fafdti.builder.nodebuilder.BestQuestionSelector;
import fr.insarennes.fafdti.builder.nodebuilder.ThresholdComputer;
import fr.insarennes.fafdti.hadoop.ReducerBase;
import fr.insarennes.fafdti.hadoop.Value;
import fr.insarennes.fafdti.hadoop.ValueDistributionMapAggregator;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;

public class Step1Red
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
		this.bestSelect = new BestQuestionSelector();
	}

	protected void reduce(IntWritable col,
			Iterable<WritableValueSDVSortedMap> valueDistMaps, Context context)
			throws IOException, InterruptedException {
		ValueDistributionMapAggregator valueDistMapAgg = new ValueDistributionMapAggregator();
		try {
			AttrSpec attrSpec = fs.getAttrSpec(col.get());
			AttrType attrType = attrSpec.getType();
			valueDistMapAgg.aggregateAll(valueDistMaps);
			WritableValueSDVSortedMap aggregatedMap = valueDistMapAgg
					.getAggregatedMap();
			if (attrType == AttrType.CONTINUOUS) {
				if (aggregatedMap.size() >= 2) {
					ThresholdComputer thresholdComputer = new ThresholdComputer(
							fs, parentDistribution, criterion);
					Pair<Double, ScoreLeftDistribution> p = thresholdComputer
							.computeThreshold(aggregatedMap);
					Question question = new Question(col.get(),
							AttrType.CONTINUOUS, p.getFirst());
					writeIfBestQuestion(context, question, p.getSecond());
				}
			} else if (attrType == AttrType.DISCRETE
					|| attrType == AttrType.TEXT) {
					for(Map.Entry<Value, ScoredDistributionVector> entry: aggregatedMap.entrySet()) {
						Value value = entry.getKey();
						ScoredDistributionVector leftDist = entry.getValue();
						ScoredDistributionVector rightDist = parentDistribution
								.computeRightDistribution(leftDist);
						leftDist.rate(criterion);
						rightDist.rate(criterion);
						ScoreLeftDistribution scoreLeftDist = new ScoreLeftDistribution(
								leftDist, rightDist);
						Question question = null;
						if(attrType == AttrType.DISCRETE) {
							question = new Question(col.get(), attrType, value.getTextValue());
						} else {
							TextAttrSpec textAttr = (TextAttrSpec) attrSpec;
							if(textAttr.getExpertType() == GramType.SGRAM) {
								question = new Question(col.get(), attrType, value.getSGram());
							} else {
								question = new Question(col.get(), attrType, value.getFGram());
							}
						}
						writeIfBestQuestion(context, question, scoreLeftDist);
					}
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