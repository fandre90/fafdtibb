/* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 * WARNING WARNING WARNING WARNING
 * Unlike the other jobs, this job relies on the OLD Hadoop mapreduce API
 * (org.apache.hadoop.mapred) instead of (org.apache.hadoop.mapreduce).
 * because it is possible to have multiple output files only in the
 * old API.
 * WARNING WARNING WARNING WARNING
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.Question;

public class Step4Map extends Mapper<Object, Text, NullWritable, LabeledExample> {
	
	protected Question question;
	protected MultipleOutputs<NullWritable, LabeledExample> multiOut;

	protected void setup(Context context) throws IOException,
			InterruptedException {
		Configuration conf = context.getConfiguration();
		question = Question.fromConf(conf);
		multiOut = new MultipleOutputs<NullWritable, LabeledExample>(context);
	}

	protected void map(Object key, Text dataLine, Context context)
			throws IOException, InterruptedException {
		LabeledExample labeledExample = new LabeledExample(dataLine.toString());
		try {
			if (question.ask(labeledExample.getExample())) {
				multiOut.write(NullWritable.get(), labeledExample, "left");
			} else {
				multiOut.write(NullWritable.get(), labeledExample, "right");
			}
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

/*
 * public class Step4Map extends Mapper<Object, Text, Text, LabeledExample>{
 * protected Question question;
 * 
 * protected void setup(Context context) throws IOException,
 * InterruptedException { Configuration conf = context.getConfiguration();
 * question = Question.fromConf(conf); }
 * 
 * protected void map(Object key, Text dataLine, Context context) throws
 * IOException, InterruptedException { LabeledExample labeledExample = new
 * LabeledExample(dataLine.toString()); try {
 * if(question.ask(labeledExample.getExample())) { context.write(new
 * Text("yes"), labeledExample); } else { context.write(new Text("no"),
 * labeledExample); } } catch (FAFException e) { // TODO Auto-generated catch
 * block e.printStackTrace(); } } }
 */
