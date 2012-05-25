package fr.insarennes.fafdti.builder.treebuilder;

import java.awt.Stroke;
import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.NullArgumentException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextOutputFormat;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Util;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.hadoop.MapperTreeBuilder;
import fr.insarennes.fafdti.hadoop.SplitExampleMultipleOutputFormat;
import fr.insarennes.fafdti.hadoop.WholeTextInputFormat;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.ImportXML;

public class TreeBuildRunMapper implements Runnable {

	private DotNamesInfo namesInfo;
	private String workingDir;
	private Criterion criterion;
	private DecisionNodeSetter nodeSetter;
	private List<StoppingCriterion> stopping;
	private StatBuilder stats;
	private String inputData;
	private ParentInfos parentInfos;
	private Path outputPath;

	private TreeBuildRunMapper(DotNamesInfo namesInfo, String workingDir,
			Criterion criterion, DecisionNodeSetter nodeSetter,
			List<StoppingCriterion> stopping, StatBuilder stats,
			String inputData) {
		this.namesInfo = namesInfo;
		this.workingDir = workingDir;
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.stats = stats;
		this.inputData = inputData;

	}

	public TreeBuildRunMapper(DotNamesInfo namesInfo, String workingDir,
			Criterion criterion, DecisionNodeSetter nodeSetter,
			List<StoppingCriterion> stopping, StatBuilder stats,
			String inputData, String baggingId) {
		this(namesInfo, workingDir, criterion, nodeSetter, stopping, stats,
				inputData);
		ParentInfos parentInfos = new ParentInfos(0, "launcher", baggingId
				+ "-fast");
		this.parentInfos = parentInfos;
		String id = parentInfos.getBaggingId() + "-"
				+ Integer.toString(stats.getNextId());
		this.outputPath = new Path(this.workingDir, id);
	}

	public TreeBuildRunMapper(DotNamesInfo namesInfo, String workingDir,
			Criterion criterion, DecisionNodeSetter nodeSetter,
			List<StoppingCriterion> stopping, StatBuilder stats,
			String inputData, ParentInfos parentInfos) {
		this(namesInfo, workingDir, criterion, nodeSetter, stopping, stats,
				inputData);
		if(parentInfos == null) {
			throw new NullArgumentException("parentInfos muts not be null");
		}
		this.parentInfos = parentInfos;
		String id = parentInfos.getBaggingId() + "-"
				+ Integer.toString(stats.getNextId());
		this.outputPath = new Path(this.workingDir, id);
	}

	@Override
	public void run() {
		JobConf jobConf;
		try {
			jobConf = setupMapperJob();
			JobClient.runJob(jobConf);
			DecisionTree dt = readTree();
			nodeSetter.set(dt);
			stats.decrementPending();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private JobConf setupMapperJob() throws IOException {
		JobConf jobConf = new JobConf(MapperTreeBuilder.class);
		namesInfo.toConf(jobConf);
		parentInfos.toConf(jobConf);
		criterion.toConf(jobConf);
		for (StoppingCriterion stop : stopping) {
			stop.toConf(jobConf);
		}
		jobConf.setOutputKeyClass(NullWritable.class);
		jobConf.setOutputValueClass(Text.class);
		jobConf.setMapperClass(MapperTreeBuilder.class);
		jobConf.setInputFormat(WholeTextInputFormat.class);
		org.apache.hadoop.mapred.FileInputFormat.setInputPaths(jobConf,
				inputData);
		jobConf.setOutputFormat(TextOutputFormat.class);

		org.apache.hadoop.mapred.FileOutputFormat.setOutputPath(jobConf,
				outputPath);
		return jobConf;
	}

	private DecisionTree readTree() throws IOException, FAFException {
		ImportXML importXML = new ImportXML(Util
				.getPartNonEmptyPath(outputPath).toString());
		importXML.launch();
		BaggingTrees treeBag = importXML.getResult();
		return treeBag.getTree(0);
	}

}
