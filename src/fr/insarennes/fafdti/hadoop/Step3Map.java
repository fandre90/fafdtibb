package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.FeatureSpec;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class Step3Map extends
		MapperBase<Object, Text, Text, QuestionDistVectorPair> {

	protected void map(Object key, Text dataLine, Context context)
			throws IOException, InterruptedException {
		QuestionDistVectorPair qDVPair = new QuestionDistVectorPair(
				dataLine.toString());
		context.write(new Text("best"), qDVPair);
	}
}
