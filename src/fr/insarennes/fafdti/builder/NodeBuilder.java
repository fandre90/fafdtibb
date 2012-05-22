/**
 * Classe qui construit un noeud (ou une feuille) et fait l'appel récursif.
 * On note qu'une fois la construciton finie, dans le cas où c'est un noeud, le thread
 * lance 2 autres threads puis fini ; ses fils seront assigné grâce au mécanisme
 * de DecisionNodeSetter.
 */

package fr.insarennes.fafdti.builder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StopCriterionUtils;
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
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class NodeBuilder implements Runnable, StopCriterionUtils {
	
	protected static Logger log = Logger.getLogger(NodeBuilder.class);

	protected Path inputDataPath;
	protected Path workingDir;
	protected DotNamesInfo featureSpec;
	protected Criterion criterion;
	protected DecisionNodeSetter nodeSetter;
	protected List<StoppingCriterion> stopping;
	protected ParentInfos parentInfos;
	protected ScoredDistributionVector parentDistribution;
	protected QuestionScoreLeftDistribution qLeftDistribution;
	protected ScoredDistributionVector rightDistribution;
	protected StatBuilder stats;
	protected String id;
	
	protected int relaunchCounter;
	protected final int MAX_RELAUNCH_COUNTER = 10;
	private final String job0outDir = "initial-entropy";
	private final String job1outDir = "discrete-text-questions";
	private final String job2outDir = "continuous-questions";
	private final String job3outDir = "best-question";
	private final String job4outDir = "split";
	
	//First node constructor
	public NodeBuilder(DotNamesInfo featureSpec, 
			String inputDataPath,
			String workingDir,
			Criterion criterion,
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			StatBuilder stats,
			String baggingId) {
		this.parentInfos = new ParentInfos(0, "launcher", baggingId);
		this.featureSpec = featureSpec;
		this.inputDataPath = new Path(inputDataPath);
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.stats = stats;
		this.id = parentInfos.getBaggingId()+"-"+Integer.toString(stats.getNextId());
		this.workingDir = new Path(workingDir, id);
		this.relaunchCounter = 0;
	}
	
	//Recursive constructor
	public NodeBuilder(DotNamesInfo featureSpec, 
			String inputDataPath,
			String workingDir, 
			Criterion criterion, 
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			ParentInfos parentInfos, 
			ScoredDistributionVector parentDistribution,
			StatBuilder stats) {
		this.featureSpec = featureSpec;
		this.inputDataPath = new Path(inputDataPath);
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.parentInfos = parentInfos;
		this.parentDistribution = parentDistribution;
		this.stats = stats;
		this.id = parentInfos.getBaggingId()+"-"+Integer.toString(stats.getNextId());
		this.workingDir = new Path(workingDir, id);
	}
	
	@Override
	public void run() {
		log.info("Thread "+id+" starting (launched by "+parentInfos.getId()+")");
		try {
			//JOB0
			if(parentDistribution==null){
				log.info("Thread "+id+ " : launching step0...");
				Job job0 = setupJob0();
				job0.waitForCompletion(false);
				//****//
				while(!job0.isSuccessful()){
					log.info("Thread "+id+ " : RE-launching step0...");
					deleteDir(job0outDir);
					job0 = setupJob0();
					job0.waitForCompletion(false);
				}
				//****//
				//get initial distribution
				parentDistribution = readParentDistribution();
				stats.setTotalEx(parentDistribution.getTotal());
			}
			log.debug("parentDist = "+parentDistribution.toString());
			//JOB1
			Job job1 = setupJob1(parentDistribution);
			log.info("Thread "+id+ " : launching step1...");
			job1.waitForCompletion(false);
			//****//
			while(!job1.isSuccessful()){
				log.info("Thread "+id+ " : RE-launching step1...");
				deleteDir(job1outDir);
				job1 = setupJob1(parentDistribution);
				job1.waitForCompletion(false);
			}
			//****//
			//JOB2
			Job job2 = setupJob2(parentDistribution);
			log.info("Thread "+id+ " : launching step2...");
			job2.waitForCompletion(false);
			//****//
			while(!job2.isSuccessful()){
				log.info("Thread "+id+ " : RE-launching step2...");
				deleteDir(job2outDir);
				job2 = setupJob2(parentDistribution);
				job2.waitForCompletion(false);
			}
			//****//
			//JOB3
			Job job3 = setupJob3();
			log.info("Thread "+id+ " : launching step3...");
			job3.waitForCompletion(false);
			//****//
			while(!job3.isSuccessful()){
				log.info("Thread "+id+ " : RE-launching step3...");
				deleteDir(job3outDir);
				job3 = setupJob3();
				job3.waitForCompletion(false);
			}
			//****//
			//get best question	
			qLeftDistribution = readBestQuestion();
			//if no best question : make a leaf
			if(qLeftDistribution==null){
				leafMaker();
			}
			//else
			else{
				//compute right distribution from left one
				rightDistribution = parentDistribution.computeRightDistribution(qLeftDistribution.getScoreLeftDistribution().getDistribution());
				rightDistribution.rate(criterion);
				//JOB4
				// Old API
				JobConf job4Conf = setupJob4(qLeftDistribution.getQuestion());
				log.info("Thread "+id+ " : launching step4...");
				RunningJob rj4 = JobClient.runJob(job4Conf);
				//****//
				while(!rj4.isSuccessful()){
					log.info("Thread "+id+ " : RE-launching step4...");
					deleteDir(job4outDir);
					job4Conf = setupJob4(qLeftDistribution.getQuestion());
					rj4 = JobClient.runJob(job4Conf);
				}
				//****//
				
				if(this.mustStop()){
					leafMaker();
				}
				else nodeMaker();
			}
		
			log.info("Thread "+id+" finished (launched by "+parentInfos.getId()+")");
			
		} catch (Exception e){
			//if catch an exception : relaunch full thread
			log.error(e.getMessage());
			log.error("Thread "+id+ " catched an exception : re-launch it");
			relaunchCounter++;
			if(relaunchCounter <= MAX_RELAUNCH_COUNTER) {
				relaunch();
			} else {
				log.error("Thread " + id + " was relaunched more than " +
						MAX_RELAUNCH_COUNTER + " times. Aborting.");
				System.exit(FAFExitCode.EXIT_ERROR);
			}
		}
	}
	
	//*******************relaunching utils methods*******************//
	
	private void deleteDir(String dir){
		FileSystem fs = null;
		try {
			fs = FileSystem.get(new Configuration());
			fs.delete(new Path(workingDir,dir), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void relaunch(){
		deleteAllDirs();
		Scheduler.INSTANCE.execute(this);
	}
	
	private void deleteAllDirs(){
		deleteDir(job0outDir);
		deleteDir(job1outDir);
		deleteDir(job2outDir);
		deleteDir(job3outDir);
		deleteDir(job4outDir);
	}
	
	//*******************Construction methods************//
	
	private void nodeMaker(){
		log.log(Level.INFO, "Making a question node...");
		//+2(2 sons)-1(current node) = 1
		//on le fait avant d'appeler set(dtq) sur le nodeSetter
		stats.incrementPending();
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
		Path yesdata = new Path(dataRes, "left");
		Path nodata = new Path(dataRes, "right");
		ParentInfos pInfos = new ParentInfos(parentInfos.getDepth() + 1, id, parentInfos.getBaggingId());

		NodeBuilder yesSon = new NodeBuilder(this.featureSpec, 
				yesdata.toString(), 
				this.workingDir.getParent().toString(), 
				this.criterion, dtq.yesSetter(), 
				this.stopping, pInfos, 
				qLeftDistribution.getScoreLeftDistribution().getDistribution(),
				stats);
		NodeBuilder noSon = new NodeBuilder(this.featureSpec, 
				nodata.toString(), 
				this.workingDir.getParent().toString(), 
				this.criterion, dtq.noSetter(), 
				this.stopping, pInfos, 
				rightDistribution,
				stats);
		
		Scheduler.INSTANCE.execute(yesSon);
		Scheduler.INSTANCE.execute(noSon);
	}
	
	private void leafMaker(){
		log.log(Level.INFO, "Making a distribution leaf...");
		//construction de la feuille
		int[] distr = parentDistribution.getDistributionVector();
		int sum = parentDistribution.getTotal();
		log.log(Level.DEBUG, "sum="+sum);
		Map<String, Double> map = new HashMap<String, Double>();
		for(int i=0 ; i<distr.length ; i++) {
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
			System.out.println(parentDistribution.toString());
			System.exit(FAFExitCode.EXIT_ERROR);
		}
		try {
			nodeSetter.set(dtl);
		} catch (CannotOverwriteTreeException e) {
			log.log(Level.ERROR, "NodeBuilder tries to overwrite a DecisionTree through NodeSetter with Distribution leaf");
		}
		try {
			stats.addExClassified(sum);
		} catch (FAFException e) {
			log.error("Cannot add stats of examples classified because total has not been set");
		}
		//un pending en moins
		stats.decrementPending();
	}
	
	//***************setup JOB methods********************//
	
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
		featureSpec.toConf(conf);
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
		featureSpec.toConf(conf);
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
		featureSpec.toConf(conf);
		criterion.toConf(conf);
		Job job = new Job(conf, "Best question selection");

		job.setJarByClass(this.getClass());

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(QuestionScoreLeftDistribution.class);

		job.setMapperClass(Step3Map.class);
		//job.setCombinerClass(Step3Red.class);
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

	//****************Reading results utils methods********//
	
	private FSDataInputStream getPartNonEmpty(Path inputDir) throws IOException{
		Configuration conf = new Configuration();
		FileSystem fileSystem;
		fileSystem = FileSystem.get(conf);
		FileStatus[] files = fileSystem.listStatus(inputDir);
		FSDataInputStream in = null;
		for (int i = 0; i < files.length; i++) {
			Path tmp = files[i].getPath();
			if (tmp.getName().startsWith("part") && fileSystem.getFileStatus(tmp).getLen() > 0)
					in = fileSystem.open(tmp);
		}
		if(in == null){
			log.warn("No non-empty part file found");
			return null;
		}
		return in;
	}
	
	private String readFileFirstLine(Path inputDir) throws IOException {
		FSDataInputStream in = getPartNonEmpty(inputDir);
		
		if(in==null)
			return "";
		
		LineReader lr = new LineReader(in);
		Text line = new Text();
		lr.readLine(line);
		return line.toString();
	}

	private QuestionScoreLeftDistribution readBestQuestion() throws IOException {
		Path path = new Path(workingDir, job3outDir);
		String line = readFileFirstLine(path);
		if(line.trim().equals("")){
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
	
	
	//*****************stopping utils methods************//
	
	private boolean mustStop(){
		for(StoppingCriterion s : stopping)		
			if(s.mustStop(this))
				return true;
		return false;
	}

	public double getCurrentGain() {
		return parentDistribution.getScore() - qLeftDistribution.getScoreLeftDistribution().getScore();
	}

	public int getDepth(){
		return parentInfos.getDepth() + 1;
	}

	public int getMinExamples() {
		int countL = qLeftDistribution.getScoreLeftDistribution().getDistribution().getTotal();
		int countR = rightDistribution.getTotal();
		return Math.min(countL, countR);
	}

}
