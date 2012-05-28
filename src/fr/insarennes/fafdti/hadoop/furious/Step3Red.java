package fr.insarennes.fafdti.hadoop.furious;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.stopcriterion.DepthMax;
import fr.insarennes.fafdti.builder.stopcriterion.ExampleMin;
import fr.insarennes.fafdti.builder.stopcriterion.GainMin;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.hadoop.veryfurious.Step11Map;

@SuppressWarnings("deprecation")
public class Step3Red extends MapReduceBase implements
	Reducer<Text, LabeledExample, Text, LabeledExample> {
	Logger log;

	@Override
	public void configure(JobConf jobConf) {
		log = Logger.getLogger(Step11Map.class);
	}

	@Override
	public void reduce(Text key, Iterator<LabeledExample> values,
			OutputCollector<Text, LabeledExample> output, Reporter reporter)
			throws IOException {
		log.info("Step4 Reducer Instanciated");
		while(values.hasNext()) {
			output.collect(key, values.next());
		}
	}
}

/*
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
*/
