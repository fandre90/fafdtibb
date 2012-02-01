package fr.insarennes.fafdti;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

public class Step2Map extends Mapper<Object, Text, Question, ScoredDistributionVector> {
	Question attrValue = new Question();
	ScoredDistributionVector entAndStats;

	protected void map(Object key, Text dataLine, Context context)
			throws IOException, InterruptedException {
		String strLine = dataLine.toString();
		String[] lineTokens = strLine.split("\t");
		attrValue.fromString(lineTokens[0]);
		entAndStats = new ScoredDistributionVector(lineTokens[1]);
		context.write(attrValue, entAndStats);
	}

}
