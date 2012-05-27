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
import fr.insarennes.fafdti.FSUtils;
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
import fr.insarennes.fafdti.hadoop.NewStep2Map;
import fr.insarennes.fafdti.hadoop.NewStep2Red;
import fr.insarennes.fafdti.hadoop.NewStep2Combiner;
import fr.insarennes.fafdti.hadoop.Step2Red;
import fr.insarennes.fafdti.hadoop.Step3Map;
import fr.insarennes.fafdti.hadoop.Step3Red;
import fr.insarennes.fafdti.hadoop.Step4Map;
import fr.insarennes.fafdti.hadoop.Step4Red;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;
import fr.insarennes.fafdti.tree.CannotOverwriteTreeException;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LinkedDecisionTreeQuestion;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class NodeBuilderFurious extends NodeBuilder implements INodeBuilder {
	public final int MAPREDUCE_QUESTION_SELECTION_THRESHOLD = 10 * 1024 * 1024;
	public final int RELAUNCH_JOB_LIMIT = 5;
	private final String job0outDir = "initial-entropy";
	private final String job1outDir = "discrete-text-questions";
	private final String job2outDir = "continuous-questions";
	private final String job3outDir = "best-question";
	private final String job4outDir = "split";
	private QuestionScoreLeftDistribution qLeftDistribution;
	private Path workingDir;
	private Path inputDataPath;
	private FileSystem fileSystem;
	private FSUtils fsUtils;
	private ScoredDistributionVector parentDistribution;

	// root constructor
	public NodeBuilderFurious(Criterion criterion, DotNamesInfo namesInfo,
			Path inputDataPath, ScoredDistributionVector parentDistribution,
			String id, Path workingDir) throws IOException,
			InterruptedException, ClassNotFoundException {
		super(namesInfo, criterion, id);
		this.inputDataPath = inputDataPath;
		this.workingDir = workingDir;
		this.fileSystem = FileSystem.get(new Configuration());
		this.fsUtils = new FSUtils(this.fileSystem);
		if (parentDistribution == null) {
			parentDistribution = computeInitialDistribution();
		}
		this.parentDistribution = parentDistribution;
	}

	@Override
	public String getId() {
		return this.id;
	}

	private ScoredDistributionVector computeInitialDistribution()
			throws IOException, InterruptedException, ClassNotFoundException {
		Job job0 = setupJob0();
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job0.getJobName());
		job0.waitForCompletion(false);
		while (!job0.isSuccessful()) {
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job0.getJobName());
			deleteDir(job0outDir);
			job0 = setupJob0();
			job0.waitForCompletion(false);
		}
		// Get initial distribution
		parentDistribution = readParentDistribution();
		return parentDistribution;
		// FIXME : Move stats in TreeBuilder
		// stats.setTotalEx(parentDistribution.getTotal());
	}

	private Job setupJob0() throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "(0) Initial distribution calculation");
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

	@Override
	public ScoredDistributionVector getDistribution() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException {
		return this.parentDistribution;
	}

	public QuestionScoreLeftDistribution buildNode() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException {

		if (parentDistribution == null) {
			throw new NullArgumentException("parentDistribution cannot be null");
		}

		// JOB1
		Job job1 = setupJob1(parentDistribution);
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job1.getJobName());
		job1.waitForCompletion(false);
		while (!job1.isSuccessful()) {
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job1.getJobName());
			deleteDir(job1outDir);
			job1 = setupJob1(parentDistribution);
			job1.waitForCompletion(false);
		}

		// JOB2
		Job job2 = setupJob2(parentDistribution);
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job2.getJobName());
		job2.waitForCompletion(false);
		while (!job2.isSuccessful()) {
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job2.getJobName());
			deleteDir(job2outDir);
			job2 = setupJob2(parentDistribution);
			job2.waitForCompletion(false);
		}
		qLeftDistribution = selectBestQuestion();
		return qLeftDistribution;
	}

	// ***************setup JOB methods********************//

	private Job setupJob1(ScoredDistributionVector parentDistribution)
			throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
		Job job = new Job(conf, "(1.1) Discrete and text questions generation");
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
		Job job = new Job(conf, "(1.2) Continuous questions generation");
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(WritableValueSDVSortedMap.class);

		job.setJarByClass(this.getClass());

		job.setMapperClass(NewStep2Map.class);
		job.setCombinerClass(NewStep2Combiner.class);
		job.setReducerClass(NewStep2Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job2outDir);
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

	// ****************Reading results utils methods********//

	private QuestionScoreLeftDistribution selectBestQuestion()
			throws IOException, InterruptedException, ClassNotFoundException {
		int totalFilesSize = 0;
		Path job1OutPart = fsUtils.getPartNonEmptyPath(new Path(workingDir,
				job1outDir));
		if (job1OutPart != null) {
			totalFilesSize += fsUtils.getSize(job1OutPart);
		}
		Path job2OutPart = fsUtils.getPartNonEmptyPath(new Path(workingDir,
				job2outDir));
		if (job2OutPart != null) {
			totalFilesSize += fsUtils.getSize(job2OutPart);
		}
		if (totalFilesSize > MAPREDUCE_QUESTION_SELECTION_THRESHOLD) {
			return mapReduceSelectBestQuestion();
		} else {
			log.info("Question candidates files total size is less than "
					+ MAPREDUCE_QUESTION_SELECTION_THRESHOLD + " bytes. "
					+ "Selecting best question in memory "
					+ "(Not running map reduce job)");
			return inMemorySelectBestQuestion(job1OutPart, job2OutPart);
		}
	}

	private QuestionScoreLeftDistribution mapReduceSelectBestQuestion()
			throws IOException, InterruptedException, ClassNotFoundException {
		Job job3 = setupJob3();
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job3.getJobName());
		job3.waitForCompletion(false);
		while (!job3.isSuccessful()) {
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job3.getJobName());
			deleteDir(job3outDir);
			job3 = setupJob3();
			job3.waitForCompletion(false);
		}
		qLeftDistribution = readBestQuestion();
		return qLeftDistribution;
	}

	private Job setupJob3() throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "(2) Best question selection");

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

	private QuestionScoreLeftDistribution readBestQuestion() throws IOException {
		Path path = new Path(workingDir, job3outDir);
		String line = fsUtils.readNonEmptyPartFirstLine(path);
		if (line.trim().equals("")) {
			log.warn("No best question generated");
			return null;
		}
		String tokens[] = line.split("\\s+", 2);
		return new QuestionScoreLeftDistribution(tokens[1]);
	}

	private QuestionScoreLeftDistribution inMemorySelectBestQuestion(
			Path job1OutPart, Path job2OutPart) throws IOException {
		BestQuestionSelector bestSelect = new BestQuestionSelector();
		if (job2OutPart != null) {
			readCandidatesQuestionsFile(job2OutPart, bestSelect);
		} else if (job1OutPart != null) {
			readCandidatesQuestionsFile(job1OutPart, bestSelect);
		}
		return new QuestionScoreLeftDistribution(bestSelect.getBestQuestion(),
				bestSelect.getBestScoreLeftDistribution());
	}

	private void readCandidatesQuestionsFile(Path path,
			BestQuestionSelector bestSelect) throws IOException {
		FSDataInputStream in = fileSystem.open(path);
		LineReader lr = new LineReader(in);
		Text text = new Text();
		while (lr.readLine(text) != 0) {
			String[] tokens = text.toString().split("\t", 2);
			Question question = new Question(tokens[0]);
			ScoreLeftDistribution sLDist = new ScoreLeftDistribution(tokens[1]);
			bestSelect.addCandidate(question, sLDist);
		}
		lr.close();
	}

	private ScoredDistributionVector readParentDistribution()
			throws IOException {
		String line = fsUtils.readNonEmptyPartFirstLine(new Path(workingDir,
				job0outDir));
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

	@SuppressWarnings("deprecation")
	private JobConf setupJob4(Question bestQuestion) throws IOException {
		JobConf jobConf = new JobConf(NodeBuilderFurious.class);
		jobConf.setJobName("(3) Input file splitting");
		bestQuestion.toConf(jobConf);
		jobConf.setOutputKeyClass(Text.class);
		jobConf.setOutputValueClass(LabeledExample.class);
		jobConf.setMapperClass(Step4Map.class);
		// jobConf.setReducerClass(Step4Red.class);
		// Map-only job. Force number of reducers to zero
		jobConf.setNumReduceTasks(0);
		jobConf.setInputFormat(org.apache.hadoop.mapred.TextInputFormat.class);
		org.apache.hadoop.mapred.FileInputFormat.setInputPaths(jobConf,
				inputDataPath);
		jobConf.setOutputFormat(SplitExampleMultipleOutputFormat.class);
		Path outputDir = new Path(workingDir, job4outDir);
		org.apache.hadoop.mapred.FileOutputFormat.setOutputPath(jobConf,
				outputDir);
		return jobConf;
	}

	@Override
	public void cleanUp() {
		/*
		 * deleteDir(job0outDir); deleteDir(job1outDir); deleteDir(job2outDir);
		 * deleteDir(job3outDir); deleteDir(job4outDir);
		 */
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

}
