package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.Question;

public class Step4Red extends
		ReducerBase<NullWritable, LabeledExample, NullWritable, LabeledExample> {

	protected Question question;
	protected MultipleOutputs<NullWritable, LabeledExample> multiOut;

	protected void setup(Context context) throws IOException,
			InterruptedException {
		Configuration conf = context.getConfiguration();
		question = Question.fromConf(conf);
		multiOut = new MultipleOutputs<NullWritable, LabeledExample>(context);
	}

	protected void reduce(NullWritable nullWritable,
			Iterable<LabeledExample> examples, Context context)
			throws IOException, InterruptedException {
		for (LabeledExample labeledExample : examples) {
			try {
				if (question.ask(labeledExample.getExample())) {
					// multiOut.write(NullWritable.get(), labeledExample,
					// "left");
					multiOut.write("text", NullWritable.get(), labeledExample,
							"left");
				} else {
					// multiOut.write(NullWritable.get(), labeledExample,
					// "right");
					multiOut.write("text", NullWritable.get(), labeledExample,
							"right");
				}
			} catch (FAFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
