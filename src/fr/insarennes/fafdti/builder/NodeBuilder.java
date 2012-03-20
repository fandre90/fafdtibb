package fr.insarennes.fafdti.builder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.util.ReaderInputStream;

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
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class NodeBuilder implements Runnable, StopCriterionUtils {
	
	protected static Logger log = Logger.getLogger(NodeBuilder.class);

	protected Path inputDataPath;
	protected Path workingDir;
	protected UUID nodeUUID;
	protected DotNamesInfo featureSpec;
	protected Criterion criterion;
	protected DecisionNodeSetter nodeSetter;
	protected List<StoppingCriterion> stopping;
	protected ParentInfos parentInfos;
	protected ScoredDistributionVector parentDistribution;
	protected QuestionScoreLeftDistribution qLeftDistribution;
	protected ScoredDistributionVector rightDistribution;
	
	private final String job0outDir = "initial-entropy";
	private final String job1outDir = "discrete-text-questions";
	private final String job2outDir = "continuous-questions";
	private final String job3outDir = "best-question";
	private final String job4outDir = "split";

	public NodeBuilder(DotNamesInfo featureSpec, String inputDataPath,
			String workingDir, Criterion criterion, 
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping) {
		this.featureSpec = featureSpec;
		this.inputDataPath = new Path(inputDataPath);
		this.nodeUUID = UUID.randomUUID();
		this.workingDir = new Path(workingDir, nodeUUID.toString());
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.parentInfos = new ParentInfos(0);
	}
	
	public NodeBuilder(DotNamesInfo featureSpec, String inputDataPath,
			String workingDir, Criterion criterion, 
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			ParentInfos parentInfos, ScoredDistributionVector parentDistribution) {
		this.featureSpec = featureSpec;
		this.inputDataPath = new Path(inputDataPath);
		this.nodeUUID = UUID.randomUUID();
		this.workingDir = new Path(workingDir, nodeUUID.toString());
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.parentInfos = parentInfos;
		this.parentDistribution = parentDistribution;
	}
	
	@Override
	public void run() {
		try {
			if(parentDistribution==null){
				
				Job job0 = setupJob0();
				job0.waitForCompletion(false);
				parentDistribution = readParentDistribution();
			}
			if(parentDistribution.isPure()){
				leafMaker();
			}
			else{
				System.out.println(">>"+featureSpec.toString());
				System.out.println("<<<<<<<<<<"+parentDistribution.toString());
				Job job1 = setupJob1(parentDistribution);
				job1.submit();
				Job job2 = setupJob2(parentDistribution);
				job2.waitForCompletion(false);
				Job job3 = setupJob3();
				job3.waitForCompletion(false);
				qLeftDistribution = readBestQuestion();
				rightDistribution = parentDistribution.computeRightDistribution(qLeftDistribution.getScoreLeftDistribution().getDistribution());
				System.out.println("<<<<<<<<<<"+qLeftDistribution.toString());
				// Old API
				JobConf job4Conf = setupJob4(qLeftDistribution.getQuestion());
				JobClient.runJob(job4Conf);
				
				if(this.mustStop()){
					leafMaker();
				}
				else nodeMaker();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void nodeMaker(){
		log.log(Level.INFO, "Making a question node...");
		//construction du noeud
		Question question = qLeftDistribution.getQuestion();
		DecisionTreeQuestion dtq = new DecisionTreeQuestion(question);
		try {
			nodeSetter.set(dtq);
		} catch (CannotOverwriteTreeException e) {
			log.log(Level.ERROR, "NodeBuilder tries to overwrite a DecisionTree through NodeSetter with Question node");
			log.log(Level.ERROR, e.getMessage());
		}
		//on lance la construction du fils droit et gauche
		Path dataRes = new Path(this.workingDir,this.job4outDir);
		Path yesnames = new Path(dataRes, "left");
		Path nonames = new Path(dataRes, "right");
		ParentInfos pInfos = new ParentInfos(parentInfos.getDepth() + 1);
		
		NodeBuilder yesSon = new NodeBuilder(this.featureSpec, yesnames.toString(), 
				this.workingDir.getParent().toString(), this.criterion, dtq.yesSetter(), 
				this.stopping, pInfos, qLeftDistribution.getScoreLeftDistribution().getDistribution());
		NodeBuilder noSon = new NodeBuilder(this.featureSpec, nonames.toString(), 
				this.workingDir.getParent().toString(), this.criterion, dtq.noSetter(), 
				this.stopping, pInfos, rightDistribution);
		
		Scheduler scheduler = Scheduler.INSTANCE;
		scheduler.execute(yesSon);
		scheduler.execute(noSon);
		
		//on indique qu'on a fini
		scheduler.done(this);
	}
	
	private void leafMaker(){
		log.log(Level.INFO, "Making a distribution leaf...");
		//construction de la feuille
		int[] distr = parentDistribution.getDistributionVector();
		int sum = parentDistribution.getTotal();
		log.log(Level.DEBUG, "sum="+sum);
		Map<String, Double> map = new HashMap<String, Double>();
		for(int i=0 ; i<distr.length ; i++){
			log.log(Level.DEBUG, "distr[i]="+distr[i]);
			Double distri = new Double((double)distr[i]/(double)sum);
			if(distri.doubleValue()>0.0){
				String label = featureSpec.getLabels()[i];
				map.put(label, distri);
			}
		}
		DecisionTreeLeaf dtl = null;
		try {
			dtl = new DecisionTreeLeaf(new LeafLabels(map), sum);
		} catch (InvalidProbabilityComputationException e) {
			log.log(Level.ERROR, e.getMessage());
		}
		try {
			nodeSetter.set(dtl);
		} catch (CannotOverwriteTreeException e) {
			log.log(Level.ERROR, "NodeBuilder tries to overwrite a DecisionTree through NodeSetter with Distribution leaf");
		}
		
		Scheduler scheduler = Scheduler.INSTANCE;
		//on indique qu'on a fini
		scheduler.done(this);
		//on arrête le scheduler si on est la dernière feuille
		if(scheduler.everythingIsDone())
			scheduler.stopMe();
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

	private Job setupJob2(ScoredDistributionVector parentDistribution)
			throws IOException {
		Configuration conf = new Configuration();
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		parentDistribution.toConf(conf);
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
		System.out.println(inputDir.toString());
		Configuration conf = new Configuration();
		FileSystem fileSystem;
		fileSystem = FileSystem.get(conf);
		FileStatus[] files = fileSystem.listStatus(inputDir);
		//for(int i=0; i<files.length; i++)	
		//	System.out.println(files[i].getPath().toString());
		Path inputFile = null;
		for (int i = 0; i < files.length; i++) {
			Path tmp = files[i].getPath();
			if (tmp.getName().startsWith("part")) {
				inputFile = tmp;
				break;
			}
		}
		//System.out.println(inputFile.toString());
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
	
	private boolean mustStop(){
		for(StoppingCriterion s : stopping)
			if(s.mustStop(this))
				return true;
		return false;
	}

	public double getCurrentGain() {
		return parentDistribution.getScore() - ScoreLeftDistribution.computeCombinedEntropy(qLeftDistribution.getScoreLeftDistribution().getDistribution(), rightDistribution);
	}

	public int getDepth(){
		return parentInfos.getDepth() + 1;
	}

	public int getMinExamples() {
		int countL = qLeftDistribution.getScoreLeftDistribution().getDistribution().getTotal();
		int countR = rightDistribution.getTotal();
		return Math.min(countL, countR);
//		//Il faut compter le nombre d'exemples à droite et à gauche et retourner le minimum
//		Path dataRes = new Path(this.workingDir, this.job4outDir);
//		String pathRight = (new Path(dataRes, "right")).toString();
//		String pathLeft = (new Path(dataRes, "left")).toString();
//		log.log(Level.DEBUG, pathRight.toString());
//		log.log(Level.DEBUG, pathLeft.toString());
//		Reader readerR = null;
//		Reader readerL = null;
//		try {
//			readerR = new FileReader(pathRight);
//		} catch (FileNotFoundException e) {
//			log.log(Level.ERROR, "Cannot find file : "+pathRight);
//		}
//		try {
//			readerL = new FileReader(pathLeft);
//		} catch (FileNotFoundException e) {
//			log.log(Level.ERROR, "Cannot find file : "+pathLeft);
//		}
//		LineNumberReader lineR = new LineNumberReader(readerR);
//		LineNumberReader lineL = new LineNumberReader(readerL);
//		int countR = 0, countL = 0;
//		try {
//			while(lineR.readLine()!=null)
//				countR = lineR.getLineNumber();
//		} catch (IOException e) {
//			log.log(Level.ERROR, "Error occurs while reading "+pathRight);
//		}
//		try {
//			while(lineL.readLine()!=null)
//				countL = lineL.getLineNumber();
//		} catch (IOException e) {
//			log.log(Level.ERROR, "Error occurs while reading "+pathLeft);
//		}
//		return Math.min(countL, countR);
	}

}
