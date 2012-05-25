package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;
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
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.cli.FAFExitCode;
import fr.insarennes.fafdti.hadoop.ContinuousAttrLabelPair;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.hadoop.SplitExampleMultipleOutputFormat;
import fr.insarennes.fafdti.hadoop.Step0Map;
import fr.insarennes.fafdti.hadoop.Step0Red;
import fr.insarennes.fafdti.hadoop.Step1Map;
import fr.insarennes.fafdti.hadoop.Step1Red;
import fr.insarennes.fafdti.hadoop.Step2Map;
import fr.insarennes.fafdti.hadoop.Step2Red;
import fr.insarennes.fafdti.hadoop.Step3Map;
import fr.insarennes.fafdti.hadoop.Step3Red;
import fr.insarennes.fafdti.hadoop.Step4Map;
import fr.insarennes.fafdti.hadoop.Step4Red;
import fr.insarennes.fafdti.tree.CannotOverwriteTreeException;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LinkedDecisionTreeQuestion;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class NodeBuilderFat extends NodeBuilder implements INodeBuilder {
	private final String job0outDir = "initial-entropy";
	private final String job1outDir = "discrete-text-questions";
	private final String job2outDir = "continuous-questions";
	private final String job3outDir = "best-question";
	private final String job4outDir = "split";
	private QuestionScoreLeftDistribution qLeftDistribution;
	private Path workingDir;
	private String id;
	private Path inputDataPath;
	private ScoredDistributionVector parentDistribution;

	// root constructor
	public NodeBuilderFat(Criterion criterion, DotNamesInfo namesInfo,
			Path inputDataPath, ScoredDistributionVector parentDistribution,
			String id, Path workingDir) throws IOException, InterruptedException, ClassNotFoundException {
		super(namesInfo, criterion, id);
		this.inputDataPath = inputDataPath;
		this.workingDir = workingDir;
		if (parentDistribution == null) {
			log.info("NodeFat " + id + " : launching step0...");
			Job job0 = setupJob0();
			job0.waitForCompletion(false);
			// ****//
			while (!job0.isSuccessful()) {
				log.info("NodeFat " + id + " : RE-launching step0...");
				deleteDir(job0outDir);
				job0 = setupJob0();
				job0.waitForCompletion(false);
			}
			// ****//
			// get initial distribution
			parentDistribution = readParentDistribution();
			// FIXME : Move stats in TreeBuilder
			// stats.setTotalEx(parentDistribution.getTotal());
		}
		this.parentDistribution = parentDistribution;
	}

	public QuestionScoreLeftDistribution buildNode()
			throws IOException, InterruptedException, ClassNotFoundException,
			FAFException {
		// Set database and parent distribution
		if (parentDistribution == null) {
			throw new NullArgumentException("parentDistribution cannot be null");
		}
		// log.info("Thread "+id+" starting (launched by "+parentInfos.getId()+")");
		log.debug("parentDist = " + parentDistribution.toString());
		// JOB1
		Job job1 = setupJob1(parentDistribution);
		log.info("NodeFat " + id + " : launching step1...");
		job1.waitForCompletion(false);
		// ****//
		while (!job1.isSuccessful()) {
			log.info("NodeFat " + id + " : RE-launching step1...");
			deleteDir(job1outDir);
			job1 = setupJob1(parentDistribution);
			job1.waitForCompletion(false);
		}
		// ****//
		// JOB2
		Job job2 = setupJob2(parentDistribution);
		log.info("NodeFat " + id + " : launching step2...");
		job2.waitForCompletion(false);
		// ****//
		while (!job2.isSuccessful()) {
			log.info("NodeFat " + id + " : RE-launching step2...");
			deleteDir(job2outDir);
			job2 = setupJob2(parentDistribution);
			job2.waitForCompletion(false);
		}
		// ****//
		// JOB3
		Job job3 = setupJob3();
		log.info("NodeFat " + id + " : launching step3...");
		job3.waitForCompletion(false);
		// ****//
		while (!job3.isSuccessful()) {
			log.info("NodeFat " + id + " : RE-launching step3...");
			deleteDir(job3outDir);
			job3 = setupJob3();
			job3.waitForCompletion(false);
		}
		// ****//
		// get best question
		qLeftDistribution = readBestQuestion();
		return qLeftDistribution;
	}

	// ***************setup JOB methods********************//

	private Job setupJob0() throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "Initial entropy calculation");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ScoredDistributionVector.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setMapperClass(Step0Map.class);
		job.setReducerClass(Step0Red.class);

		job.setJarByClass(this.getClass());

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job0outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	private Job setupJob1(ScoredDistributionVector parentDistribution)
			throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
		Job job = new Job(conf, "Discrete and text questions generation");
		job.setOutputKeyClass(Question.class);
		job.setOutputValueClass(IntWritable.class);

		job.setJarByClass(this.getClass());

		job.setMapperClass(Step1Map.class);
		job.setReducerClass(Step1Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job1outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	private Job setupJob2(ScoredDistributionVector parentDistribution)
			throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
		Job job = new Job(conf, "Continuous questions generation");
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ContinuousAttrLabelPair.class);

		job.setJarByClass(this.getClass());

		job.setMapperClass(Step2Map.class);
		job.setReducerClass(Step2Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job2outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	private Job setupJob3() throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "Best question selection");

		job.setJarByClass(this.getClass());

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(QuestionScoreLeftDistribution.class);

		job.setMapperClass(Step3Map.class);
		// job.setCombinerClass(Step3Red.class);
		job.setReducerClass(Step3Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		Path inputDir1 = new Path(workingDir, job1outDir);
		FileInputFormat.addInputPath(job, inputDir1);
		Path inputDir2 = new Path(workingDir, job2outDir);
		FileInputFormat.addInputPath(job, inputDir2);
		Path outputDir = new Path(workingDir, job3outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	/*
	 * private Job setupJob4(Question bestQuestion) throws IOException {
	 * Configuration conf = new Configuration(); bestQuestion.toConf(conf); Job
	 * job = new Job(conf, "Positive and negative examples separation");
	 * 
	 * job.setOutputKeyClass(NullWritable.class);
	 * job.setOutputValueClass(LabeledExample.class);
	 * 
	 * job.setMapperClass(Step4Map.class);
	 * //job.setReducerClass(Step4Red.class);
	 * job.setInputFormatClass(TextInputFormat.class);
	 * job.setOutputFormatClass(NullOutputFormat.class);
	 * //MultipleOutputs.addNamedOutput(job, "text", TextOutputFormat.class, //
	 * NullWritable.class, LabeledExample.class);
	 * 
	 * FileInputFormat.addInputPath(job, inputDataPath); Path outputDir = new
	 * Path(workingDir, job4outDir); FileOutputFormat.setOutputPath(job,
	 * outputDir); return job; }
	 */

	@SuppressWarnings("deprecation")
	private JobConf setupJob4(Question bestQuestion) throws IOException {
		JobConf jobConf = new JobConf(NodeBuilder.class);
		bestQuestion.toConf(jobConf);
		jobConf.setOutputKeyClass(Text.class);
		jobConf.setOutputValueClass(LabeledExample.class);
		jobConf.setMapperClass(Step4Map.class);
		jobConf.setReducerClass(Step4Red.class);
		jobConf.setInputFormat(org.apache.hadoop.mapred.TextInputFormat.class);
		org.apache.hadoop.mapred.FileInputFormat.setInputPaths(jobConf,
				inputDataPath);
		jobConf.setOutputFormat(SplitExampleMultipleOutputFormat.class);
		Path outputDir = new Path(workingDir, job4outDir);
		org.apache.hadoop.mapred.FileOutputFormat.setOutputPath(jobConf,
				outputDir);
		return jobConf;
	}

	// ****************Reading results utils methods********//

	private FSDataInputStream getPartNonEmpty(Path inputDir) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fileSystem;
		fileSystem = FileSystem.get(conf);
		FileStatus[] files = fileSystem.listStatus(inputDir);
		FSDataInputStream in = null;
		for (int i = 0; i < files.length; i++) {
			Path tmp = files[i].getPath();
			if (tmp.getName().startsWith("part")
					&& fileSystem.getFileStatus(tmp).getLen() > 0)
				in = fileSystem.open(tmp);
		}
		if (in == null) {
			log.warn("No non-empty part file found");
			return null;
		}
		return in;
	}

	private String readFileFirstLine(Path inputDir) throws IOException {
		FSDataInputStream in = getPartNonEmpty(inputDir);

		if (in == null)
			return "";

		LineReader lr = new LineReader(in);
		Text line = new Text();
		lr.readLine(line);
		return line.toString();
	}

	private QuestionScoreLeftDistribution readBestQuestion() throws IOException {
		Path path = new Path(workingDir, job3outDir);
		String line = readFileFirstLine(path);
		if (line.trim().equals("")) {
			log.warn("No best question generated");
			return null;
		}
		String tokens[] = line.split("\\s+", 2);
		return new QuestionScoreLeftDistribution(tokens[1]);
	}

	private ScoredDistributionVector readParentDistribution()
			throws IOException {
		String line = readFileFirstLine(new Path(workingDir, job0outDir));
		String tokens[] = line.split("\\s+");
		return new ScoredDistributionVector(tokens[tokens.length - 1]);
	}

	@Override
	public Pair<String[][], String[][]> getSplitData() {
		return null;
	}
	
	@Override
	public Pair<Path, Path> getSplitPath() throws IOException {
		// JOB4
		// Old API
		JobConf job4Conf = setupJob4(qLeftDistribution.getQuestion());
		log.info("Thread " + id + " : launching step4...");
		RunningJob rj4 = JobClient.runJob(job4Conf);
		// ****//
		while (!rj4.isSuccessful()) {
			log.info("Thread " + id + " : RE-launching step4...");
			deleteDir(job4outDir);
			job4Conf = setupJob4(qLeftDistribution.getQuestion());
			rj4 = JobClient.runJob(job4Conf);
		}
		Path p = new Path(this.workingDir, job4outDir);
		return new Pair<Path, Path>(new Path(p, "left"), new Path(p, "right"));
	}

	@Override
	public void cleanUp() {
		deleteDir(job0outDir);
		deleteDir(job1outDir);
		deleteDir(job2outDir);
		deleteDir(job3outDir);
		deleteDir(job4outDir);
	}

	protected void deleteDir(String dir) {
		FileSystem fs = null;
		try {
			fs = FileSystem.get(new Configuration());
			fs.delete(new Path(workingDir, dir), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public ScoredDistributionVector getDistribution() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException {
		return this.parentDistribution;
	}

}
