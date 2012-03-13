package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class Step1Red extends
		ReducerBase<Question, IntWritable, Question, ScoreLeftDistribution> {

	protected ScoredDistributionVector parentDistribution;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		Configuration conf = context.getConfiguration();
		this.parentDistribution = ScoredDistributionVector.fromConf(conf);

	}

	@Override
	protected void reduce(Question q, Iterable<IntWritable> labelIndexes,
			Context context) throws IOException, InterruptedException {
		ScoredDistributionVector leftDist = new ScoredDistributionVector(
				fs.numOfLabel());
		System.out.println("Got: " + q + " " + q.hashCode());
		for (IntWritable i : labelIndexes) {
			leftDist.incrStat(i.get());
		}
		// FIXME We should not need this here
		ScoredDistributionVector rightDist = this.parentDistribution
				.computeRightDistribution(leftDist);
		leftDist.rate(criterion);
		rightDist.rate(criterion);
		ScoreLeftDistribution scoreLeftDist = new ScoreLeftDistribution(
				leftDist, rightDist);
		context.write(q, scoreLeftDist);
	}
}
