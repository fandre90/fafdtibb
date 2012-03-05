package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer.Context;

public class Step3Red extends
		ReducerBase<Text, QuestionDistVectorPair, Text, QuestionDistVectorPair> {

	protected void reduce(Text text,
			Iterable<QuestionDistVectorPair> questionDistVectorPairs,
			Context context) throws IOException, InterruptedException {
		QuestionDistVectorPair bestQDVPair = null;
		double bestCriterionValue = 0;
		for (QuestionDistVectorPair qDVPair : questionDistVectorPairs) {
			double curCriterionValue = qDVPair.getDistributionVector().getScore();
			if(bestQDVPair == null | curCriterionValue < bestCriterionValue) {
				bestCriterionValue = curCriterionValue;
				// FIXME We WILL have a clone problem here
				bestQDVPair = qDVPair;
			}
		}
		context.write(new Text("best"), bestQDVPair);
	}
}
