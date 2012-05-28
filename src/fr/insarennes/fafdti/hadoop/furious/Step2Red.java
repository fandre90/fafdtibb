package fr.insarennes.fafdti.hadoop.furious;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.hadoop.ReducerBase;

public class Step2Red extends
		ReducerBase<Text, QuestionScoreLeftDistribution, Text, QuestionScoreLeftDistribution> {

	protected void reduce(Text text,
			Iterable<QuestionScoreLeftDistribution> questionScoreLeftDists,
			Context context) throws IOException, InterruptedException {
		QuestionScoreLeftDistribution bestSLDist = null;
		double bestCriterionValue = 0;
		for (QuestionScoreLeftDistribution sLDist : questionScoreLeftDists) {
			ScoreLeftDistribution scoreLeftDist = sLDist.getScoreLeftDistribution();
			double curCriterionValue = scoreLeftDist.getScore();
			if(bestSLDist == null || curCriterionValue < bestCriterionValue) {
				//System.out.println("Current= " + curCriterionValue + "; Best= " + bestCriterionValue);
				bestCriterionValue = curCriterionValue;
				bestSLDist = (QuestionScoreLeftDistribution) sLDist.clone();
			}
		}
		context.write(new Text("best"), bestSLDist);
	}
}
