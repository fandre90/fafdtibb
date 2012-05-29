package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.TooManyRelaunchException;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;
import fr.insarennes.fafdti.hadoop.furious.Step1Map;
import fr.insarennes.fafdti.hadoop.furious.Step1Combiner;
import fr.insarennes.fafdti.hadoop.furious.Step1Red;

public class NodeBuilderFurious extends NodeBuilderCommonFurious {

	private final String job1outDir = "questions";

	public NodeBuilderFurious(Criterion criterion, DotNamesInfo namesInfo,
			Path inputDataPath, ScoredDistributionVector parentDistribution,
			String id, Path workingDir) throws IOException,
			InterruptedException, ClassNotFoundException, TooManyRelaunchException {
		super(criterion, namesInfo, inputDataPath, parentDistribution, id, workingDir);
	}

	@Override
	protected List<Path> generateQuestions() throws IOException,
			InterruptedException, ClassNotFoundException, TooManyRelaunchException {
		Job job1 = setupJob1();
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job1.getJobName());
		job1.waitForCompletion(false);
		int iRelaunch = 0;
		while (!job1.isSuccessful()) {
			iRelaunch++;
			if(iRelaunch>=RELAUNCH_JOB_LIMIT) {
				throw new TooManyRelaunchException(job1.getJobName());
			}
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job1.getJobName());
			fsUtils.deleteDir(new Path(workingDir,job1outDir));
			job1 = setupJob1();
			job1.waitForCompletion(false);
		}
		List<Path> outputDirList = new ArrayList<Path>();
		Path job1outputDir = new Path(workingDir, job1outDir);
		if(job1outputDir != null) {
			outputDirList.add(job1outputDir);
		}
		return outputDirList;
	}

	private Job setupJob1() throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
		Job job = new Job(conf, "(1) All questions generation");
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(WritableValueSDVSortedMap.class);

		job.setJarByClass(this.getClass());

		job.setMapperClass(Step1Map.class);
		job.setCombinerClass(Step1Combiner.class);
		job.setReducerClass(Step1Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job1outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}
}
