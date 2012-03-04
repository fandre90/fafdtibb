import java.util.Date;
import java.util.Formatter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.EntropyCriterion;
import fr.insarennes.fafdti.builder.FeatureSpec;
import fr.insarennes.fafdti.hadoop.ContinuousAttrLabelPair;
import fr.insarennes.fafdti.hadoop.QuestionDistVectorPair;
import fr.insarennes.fafdti.hadoop.Step3Map;
import fr.insarennes.fafdti.hadoop.Step3Red;


public class TestStep3 {
	public static void main(String[] args) throws Exception {

		String file = "/home/fabien/Bureau/Hadoop/data_test/in/test1";
		String questionDir = "/home/fabien/Bureau/Hadoop/data_test/in_select";
		Formatter format = new Formatter();
		String outputDir0 = "/home/fabien/Bureau/Hadoop/data_test/step3-" + 
				format.format("%1$tY-%1$tm-%1$td %1$tHh%1$tM", new Date());

		FileSystem fs = FileSystem.get(new Configuration());//utilisé pour lire les fichiers
		FeatureSpec featureSpec = new FeatureSpec(new Path(file+".names"), fs);
		Criterion criterion = new EntropyCriterion();
		// Étape 1
		Configuration conf = new Configuration();
	
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf,
				"Génération des questions discrètes et textuelles");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(QuestionDistVectorPair.class);

		job.setMapperClass(Step3Map.class);
		job.setCombinerClass(Step3Red.class);
		job.setReducerClass(Step3Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(questionDir));
		FileOutputFormat.setOutputPath(job, new Path(outputDir0));

		job.waitForCompletion(false);
	}
}