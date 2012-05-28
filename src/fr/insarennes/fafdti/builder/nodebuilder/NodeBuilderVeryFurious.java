package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Level;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.FSUtils;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.TooManyRelaunchException;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.cli.FAFExitCode;
import fr.insarennes.fafdti.hadoop.ContinuousAttrLabelPair;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.hadoop.SplitExampleMultipleOutputFormat;
import fr.insarennes.fafdti.hadoop.OldStep2Red;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;
import fr.insarennes.fafdti.hadoop.furious.Step0Map;
import fr.insarennes.fafdti.hadoop.furious.Step0Red;
import fr.insarennes.fafdti.hadoop.furious.Step2Map;
import fr.insarennes.fafdti.hadoop.furious.Step2Red;
import fr.insarennes.fafdti.hadoop.furious.Step3Map;
import fr.insarennes.fafdti.hadoop.furious.Step3Red;
import fr.insarennes.fafdti.hadoop.veryfurious.Step11Map;
import fr.insarennes.fafdti.hadoop.veryfurious.Step11Red;
import fr.insarennes.fafdti.hadoop.veryfurious.Step12Combiner;
import fr.insarennes.fafdti.hadoop.veryfurious.Step12Map;
import fr.insarennes.fafdti.hadoop.veryfurious.Step12Red;
import fr.insarennes.fafdti.tree.CannotOverwriteTreeException;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LinkedDecisionTreeQuestion;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class NodeBuilderVeryFurious extends NodeBuilderCommonFurious{

	public final int RELAUNCH_JOB_LIMIT = 5;
	private final String job11outDir = "discrete-text-questions";
	private final String job12outDir = "continuous-questions";

	public NodeBuilderVeryFurious(Criterion criterion, DotNamesInfo namesInfo,
			Path inputDataPath, ScoredDistributionVector parentDistribution,
			String id, Path workingDir) throws IOException,
			InterruptedException, ClassNotFoundException, TooManyRelaunchException {
		super(criterion, namesInfo, inputDataPath, parentDistribution, id, workingDir);
	}

	private Job setupJob11()
			throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
		Job job = new Job(conf, "(1.1) Discrete and text questions generation");
		job.setOutputKeyClass(Question.class);
		job.setOutputValueClass(IntWritable.class);

		job.setJarByClass(this.getClass());

		job.setMapperClass(Step11Map.class);
		job.setReducerClass(Step11Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job11outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	private Job setupJob12()
			throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
		Job job = new Job(conf, "(1.2) Continuous questions generation");
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(WritableValueSDVSortedMap.class);

		job.setJarByClass(this.getClass());

		job.setMapperClass(Step12Map.class);
		job.setCombinerClass(Step12Combiner.class);
		job.setReducerClass(Step12Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job12outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	@Override
	protected List<Path> generateQuestions() throws IOException, InterruptedException, ClassNotFoundException, TooManyRelaunchException {

		Job job11 = setupJob11();
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job11.getJobName());
		job11.waitForCompletion(false);
		int iRelaunch = 0;
		while (!job11.isSuccessful()) {
			iRelaunch++;
			if(iRelaunch>=RELAUNCH_JOB_LIMIT) {
				throw new TooManyRelaunchException(job11.getJobName());
			}
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job11.getJobName());
			fsUtils.deleteDir(new Path(workingDir,job11outDir));
			job11 = setupJob11();
			job11.waitForCompletion(false);
		}

		Job job12 = setupJob12();
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job12.getJobName());
		job12.waitForCompletion(false);
		iRelaunch = 0;
		while (!job12.isSuccessful()) {
			iRelaunch++;
			if(iRelaunch>=RELAUNCH_JOB_LIMIT) {
				throw new TooManyRelaunchException(job12.getJobName());
			}
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job12.getJobName());
			fsUtils.deleteDir(new Path(workingDir,job12outDir));
			job12 = setupJob12();
			job12.waitForCompletion(false);
		}
		List<Path> outputDirList = new ArrayList<Path>();
		Path job11outFullOutDir = new Path(workingDir, job11outDir);
		if(job11outFullOutDir != null) {
			outputDirList.add(job11outFullOutDir);
		}
		Path job12outFullOutDir = new Path(workingDir, job12outDir);
		if(job12outFullOutDir != null) {
			outputDirList.add(job12outFullOutDir);
		}
		return outputDirList;
	}

}
