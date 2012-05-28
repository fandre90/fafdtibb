package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
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

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.FSUtils;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.TooManyRelaunchException;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.hadoop.SplitExampleMultipleOutputFormat;
import fr.insarennes.fafdti.hadoop.furious.Step0Map;
import fr.insarennes.fafdti.hadoop.furious.Step0Red;
import fr.insarennes.fafdti.hadoop.furious.Step2Map;
import fr.insarennes.fafdti.hadoop.furious.Step2Red;
import fr.insarennes.fafdti.hadoop.furious.Step3Map;

public abstract class NodeBuilderCommonFurious extends NodeBuilder implements
		INodeBuilder {
	public final int MAPREDUCE_QUESTION_SELECTION_THRESHOLD = 10 * 1024 * 1024;
	public final int RELAUNCH_JOB_LIMIT = 5;
	protected final String job0outDir = "initial-entropy";
	private final String job2outDir = "best-question";
	private final String job3outDir = "split";
	protected FSUtils fsUtils;
	protected Path workingDir;
	protected Path inputDataPath;
	protected FileSystem fileSystem;
	protected ScoredDistributionVector parentDistribution;
	protected QuestionScoreLeftDistribution qLeftDistribution;

	public NodeBuilderCommonFurious(Criterion criterion,
			DotNamesInfo namesInfo, Path inputDataPath,
			ScoredDistributionVector parentDistribution, String id,
			Path workingDir) throws IOException, InterruptedException,
			ClassNotFoundException, TooManyRelaunchException {
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

	protected ScoredDistributionVector computeInitialDistribution()
			throws IOException, InterruptedException, ClassNotFoundException, TooManyRelaunchException {
		Job job0 = setupJob0();
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job0.getJobName());
		job0.waitForCompletion(false);
		int iRelaunch = 0;
		while (!job0.isSuccessful()) {
			iRelaunch++;
			if(iRelaunch>=RELAUNCH_JOB_LIMIT) {
				throw new TooManyRelaunchException(job0.getJobName());
			}
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job0.getJobName());
			fsUtils.deleteDir(new Path(workingDir,job0outDir));
			job0 = setupJob0();
			job0.waitForCompletion(false);
		}
		// Get initial distribution
		parentDistribution = readParentDistribution();
		return parentDistribution;
	}

	@Override
	public ScoredDistributionVector getDistribution() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException {
		return this.parentDistribution;
	}

	private ScoredDistributionVector readParentDistribution()
			throws IOException {
		String line = fsUtils.readNonEmptyPartFirstLine(new Path(workingDir,
				job0outDir));
		String tokens[] = line.split("\\s+");
		return new ScoredDistributionVector(tokens[tokens.length - 1]);
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

	public QuestionScoreLeftDistribution buildNode() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException {

		if (parentDistribution == null) {
			throw new NullArgumentException("parentDistribution cannot be null");
		}
		List<Path> outDirPath = generateQuestions();
		qLeftDistribution = selectBestQuestion(outDirPath);
		return qLeftDistribution;
	}

	protected abstract List<Path> generateQuestions() throws IOException,
			InterruptedException, ClassNotFoundException, TooManyRelaunchException;

	private QuestionScoreLeftDistribution selectBestQuestion(
			List<Path> outputDirList) throws IOException, InterruptedException,
			ClassNotFoundException, TooManyRelaunchException {
		int totalFilesSize = 0;
		for (Path outputDir : outputDirList) {
			Path outputPath = fsUtils.getPartNonEmptyPath(outputDir);
			if (outputPath != null) {
				totalFilesSize += fsUtils.getSize(outputPath);
			}
		}
		if (totalFilesSize > MAPREDUCE_QUESTION_SELECTION_THRESHOLD) {
			return mapReduceSelectBestQuestion(outputDirList);
		} else {
			log.info("Question candidates files total size is less than "
					+ MAPREDUCE_QUESTION_SELECTION_THRESHOLD + " bytes. "
					+ "Selecting best question in memory "
					+ "(Not running map reduce job)");
			return inMemorySelectBestQuestion(outputDirList);
		}
	}

	protected QuestionScoreLeftDistribution inMemorySelectBestQuestion(
			List<Path> outputPathList) throws IOException {
		BestQuestionSelector bestSelect = new BestQuestionSelector();
		for (Path outputDir : outputPathList) {
			Path outputPath = fsUtils.getPartNonEmptyPath(outputDir);
			if(outputPath != null) {
				readCandidatesQuestionsFile(outputPath, bestSelect);
			}
		}
		if(bestSelect.getBestQuestion() != null) {
			return new QuestionScoreLeftDistribution(bestSelect.getBestQuestion(),
					bestSelect.getBestScoreLeftDistribution());
		}
		return null;
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

	private QuestionScoreLeftDistribution mapReduceSelectBestQuestion(
			List<Path> outputPathList) throws IOException,
			InterruptedException, ClassNotFoundException, TooManyRelaunchException {
		Job job2 = setupJob2(outputPathList);
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job2.getJobName());
		job2.waitForCompletion(false);
		int iRelaunch = 0;
		while (!job2.isSuccessful()) {
			iRelaunch++;
			if(iRelaunch>=RELAUNCH_JOB_LIMIT) {
				throw new TooManyRelaunchException(job2.getJobName());
			}
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job2.getJobName());
			fsUtils.deleteDir(new Path(workingDir,job2outDir));
			job2 = setupJob2(outputPathList);
			job2.waitForCompletion(false);
		}
		QuestionScoreLeftDistribution qLeftDistribution = readBestQuestion();
		return qLeftDistribution;
	}

	private QuestionScoreLeftDistribution readBestQuestion() throws IOException {
		Path path = new Path(workingDir, job2outDir);
		String line = fsUtils.readNonEmptyPartFirstLine(path);
		if (line.trim().equals("")) {
			log.warn("No best question generated");
			return null;
		}
		String tokens[] = line.split("\\s+", 2);
		return new QuestionScoreLeftDistribution(tokens[1]);
	}

	private Job setupJob2(List<Path> inputPathList) throws IOException {
		Configuration conf = new Configuration();
		namesInfo.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "(2) Best question selection");

		job.setJarByClass(this.getClass());

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(QuestionScoreLeftDistribution.class);

		job.setMapperClass(Step2Map.class);
		// job.setCombinerClass(Step3Red.class);
		job.setReducerClass(Step2Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		for (Path inputPath : inputPathList) {
			FileInputFormat.addInputPath(job, inputPath);
		}
		Path outputDir = new Path(workingDir, job2outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	@Override
	public Pair<String[][], String[][]> getSplitData() {
		return null;
	}

	@Override
	public Pair<Path, Path> getSplitPath() throws IOException, TooManyRelaunchException {
		// JOB4
		// Old API
		JobConf job3Conf = setupJob3(qLeftDistribution.getQuestion());
		log.info(this.getClass().getName() + " " + id + " : Running "
				+ job3Conf.getJobName());
		RunningJob rj3 = JobClient.runJob(job3Conf);
		// ****//
		int iRelaunch = 0;
		while (!rj3.isSuccessful()) {
			iRelaunch++;
			if(iRelaunch>=RELAUNCH_JOB_LIMIT) {
				throw new TooManyRelaunchException(rj3.getJobName());
			}
			log.warn(this.getClass().getName() + " " + id + " : Re starting "
					+ job3Conf.getJobName());
			fsUtils.deleteDir(new Path(workingDir,job3outDir));
			job3Conf = setupJob3(qLeftDistribution.getQuestion());
			rj3 = JobClient.runJob(job3Conf);
		}
		Path p = new Path(this.workingDir, job3outDir);
		return new Pair<Path, Path>(new Path(p, "left"), new Path(p, "right"));
	}

	@SuppressWarnings("deprecation")
	private JobConf setupJob3(Question bestQuestion) throws IOException {
		JobConf jobConf = new JobConf(NodeBuilderVeryFurious.class);
		jobConf.setJobName("(3) Input file splitting");
		bestQuestion.toConf(jobConf);
		jobConf.setOutputKeyClass(Text.class);
		jobConf.setOutputValueClass(LabeledExample.class);
		jobConf.setMapperClass(Step3Map.class);
		// Map-only job. Force number of reducers to zero
		jobConf.setNumReduceTasks(0);
		jobConf.setInputFormat(org.apache.hadoop.mapred.TextInputFormat.class);
		org.apache.hadoop.mapred.FileInputFormat.setInputPaths(jobConf,
				inputDataPath);
		jobConf.setOutputFormat(SplitExampleMultipleOutputFormat.class);
		Path outputDir = new Path(workingDir, job3outDir);
		org.apache.hadoop.mapred.FileOutputFormat.setOutputPath(jobConf,
				outputDir);
		return jobConf;
	}

	@Override
	public void cleanUp() {
		fsUtils.deleteDir(workingDir);
	}
}
