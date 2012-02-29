package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class Step1Red extends
		ReducerBase<Question, IntWritable, Question, ScoredDistributionVector> {

	@Override
	protected void reduce(Question q, Iterable<IntWritable> labelIndexes,
			Context context) throws IOException, InterruptedException {
		ScoredDistributionVector out = new ScoredDistributionVector(
				fs.nbEtiquettes());
		for (IntWritable i : labelIndexes) {
			out.incrStat(i.get());
		}
		out.rate(criterion);
		context.write(q, out);
	}
}
