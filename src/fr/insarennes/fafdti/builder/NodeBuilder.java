package fr.insarennes.fafdti.builder;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MultipleOutputs;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.LineReader;

import fr.insarennes.fafdti.hadoop.ContinuousAttrLabelPair;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
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

public class NodeBuilder implements Runnable {

	protected Path inputNamesPath;
	protected Path inputDataPath;
	protected Path workingDir;
	protected UUID nodeUUID;
	protected FeatureSpec featureSpec;
	protected Criterion criterion;

	private final String job0outDir = "initial-entropy";
	private final String job1outDir = "discrete-text-questions";
	private final String job2outDir = "continuous-questions";
	private final String job3outDir = "best-question";
	private final String job4outDir = "split";

	public NodeBuilder(String inputNamesPath, String inputDataPath,
			String workingDir, Criterion criterion) {
		this.inputNamesPath = new Path(inputNamesPath);
		this.inputDataPath = new Path(inputDataPath);
		this.nodeUUID = UUID.randomUUID();
		this.workingDir = new Path(workingDir, nodeUUID.toString());
		this.criterion = criterion;
	}

	@Override
	public void run() {
		Configuration conf = new Configuration();
		FileSystem fileSystem;
		try {
			fileSystem = FileSystem.get(conf);
			featureSpec = new FeatureSpec(inputNamesPath, fileSystem);
			Job job0 = setupJob0();
			job0.waitForCompletion(false);
			ScoredDistributionVector parentDistribution = readParentDistribution();
			Job job1 = setupJob1(parentDistribution);
			job1.submit();
			Job job2 = setupJob2();
			job2.waitForCompletion(false);
			Job job3 = setupJob3();
			job3.waitForCompletion(false);
			QuestionScoreLeftDistribution qDVPair = readBestQuestion();
			Job job4 = setupJob4(qDVPair.getQuestion());
			job4.waitForCompletion(false);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Job setupJob0() throws IOException {
		Configuration conf = new Configuration();
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "Initial entropy calculation");
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(ScoredDistributionVector.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setMapperClass(Step0Map.class);
		job.setReducerClass(Step0Red.class);

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
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
		Job job = new Job(conf, "Discrete and text questions generation");
		job.setOutputKeyClass(Question.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(Step1Map.class);
		job.setReducerClass(Step1Red.class);

		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job1outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}

	private Job setupJob2() throws IOException {
		Configuration conf = new Configuration();
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "Continuous questions generation");
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(ContinuousAttrLabelPair.class);

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
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "Best question selection");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(QuestionScoreLeftDistribution.class);

		job.setMapperClass(Step3Map.class);
		job.setCombinerClass(Step3Red.class);
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

	private String readFileFirstLine(Path inputDir) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fileSystem;
		fileSystem = FileSystem.get(conf);
		FileStatus[] files = fileSystem.listStatus(inputDir);
		Path inputFile = files[0].getPath();
		FSDataInputStream in = fileSystem.open(inputFile);
		LineReader lr = new LineReader(in);
		Text line = new Text();
		lr.readLine(line);
		return line.toString();
	}

	private QuestionScoreLeftDistribution readBestQuestion() throws IOException {
		String line = readFileFirstLine(new Path(workingDir, job3outDir));
		String tokens[] = line.split("\\s+", 2);
		return new QuestionScoreLeftDistribution(tokens[1]);
	}

	private ScoredDistributionVector readParentDistribution()
			throws IOException {
		String line = readFileFirstLine(new Path(workingDir, job0outDir));
		String tokens[] = line.split("\\s+");
		return new ScoredDistributionVector(tokens[tokens.length - 1]);
	}

	private Job setupJob4(Question bestQuestion) throws IOException {
		Configuration conf = new Configuration();
		bestQuestion.toConf(conf);
		Job job = new Job(conf, "Positive and negative examples separation");

		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(LabeledExample.class);

		job.setMapperClass(Step4Map.class);
		job.setReducerClass(Step4Red.class);
		job.setInputFormatClass(TextInputFormat.class);
		// job.setOutputFormatClass(TextOutputFormat.class);
		MultipleOutputs.addNamedOutput(job, "text", TextOutputFormat.class,
				NullWritable.class, LabeledExample.class);

		FileInputFormat.addInputPath(job, inputDataPath);
		Path outputDir = new Path(workingDir, job4outDir);
		FileOutputFormat.setOutputPath(job, outputDir);
		return job;
	}
}
