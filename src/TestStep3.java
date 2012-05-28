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
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.hadoop.ContinuousAttrLabelPair;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.hadoop.furious.Step2Map;
import fr.insarennes.fafdti.hadoop.furious.Step2Red;


public class TestStep3 {
	public static void main(String[] args) throws Exception {

		String file = "/home/fabien/Bureau/Hadoop/data_test/in/test1";
		String questionDir = "/home/fabien/Bureau/Hadoop/data_test/in_select";
		Formatter format = new Formatter();
		String outputDir0 = "/home/fabien/Bureau/Hadoop/data_test/step3-" + 
				format.format("%1$tY-%1$tm-%1$td %1$tHh%1$tM", new Date());

		FileSystem fs = FileSystem.get(new Configuration());//utilisé pour lire les fichiers
		DotNamesInfo featureSpec = new DotNamesInfo(new Path(file+".names"), fs);
		Criterion criterion = new EntropyCriterion();
		// Étape 1
		Configuration conf = new Configuration();
	
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf,
				"Génération des questions discrètes et textuelles");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(QuestionScoreLeftDistribution.class);

		job.setMapperClass(Step2Map.class);
		job.setCombinerClass(Step2Red.class);
		job.setReducerClass(Step2Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(questionDir));
		FileOutputFormat.setOutputPath(job, new Path(outputDir0));

		job.waitForCompletion(false);
	}
}
