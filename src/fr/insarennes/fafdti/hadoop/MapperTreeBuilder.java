package fr.insarennes.fafdti.hadoop;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.LineReader;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.nodebuilder.NodeBuilderFast;
import fr.insarennes.fafdti.builder.scheduler.DumbScheduler;
import fr.insarennes.fafdti.builder.scheduler.IScheduler;
import fr.insarennes.fafdti.builder.stopcriterion.DepthMax;
import fr.insarennes.fafdti.builder.stopcriterion.ExampleMin;
import fr.insarennes.fafdti.builder.stopcriterion.GainMin;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.builder.treebuilder.DumbTreeBuilderMaker;
import fr.insarennes.fafdti.builder.treebuilder.ITreeBuilderMaker;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.EntropyCriterion;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.visitors.XmlExporter;

public class MapperTreeBuilder extends MapReduceBase implements
		Mapper<Object, Text, NullWritable, Text> {

	DotNamesInfo namesInfo;

	@Override
	public void configure(JobConf jobConf) {
		try {
			namesInfo = DotNamesInfo.fromConf(jobConf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void map(Object key, Text value,
			OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		System.out.println("Mapper started");
		DecisionTreeHolder treeHolder = new DecisionTreeHolder();
		Criterion criterion = new EntropyCriterion();
		NodeBuilderFast nodeBuilder = new NodeBuilderFast(
				new EntropyCriterion(), namesInfo);
		String[][] database = parseDatabase(value.toString());
		DumbTreeBuilderMaker treeBuilderMaker = new DumbTreeBuilderMaker();
		IScheduler scheduler = new DumbScheduler();
		DecisionNodeSetter nodeSetter = treeHolder.getNodeSetter();
		List<StoppingCriterion> stopCriteria = new ArrayList<StoppingCriterion>();
		stopCriteria.add(new DepthMax(10));
		stopCriteria.add(new ExampleMin(1));
		stopCriteria.add(new GainMin(0.00001));
		StatBuilder stats = new StatBuilder(1);
		ParentInfos pInfos = new ParentInfos(0, "0", "0");
		Runnable treeBuilder = treeBuilderMaker.makeTreeBuilder(namesInfo, "/",
				criterion, nodeSetter, stopCriteria, stats, nodeBuilder, database, pInfos, null, treeBuilderMaker,
				scheduler);
		treeBuilder.run();
		BaggingTrees treeBag = new BaggingTrees(1);
		try {
			treeBag.setTree(0, treeHolder.getRoot());
			XmlExporter xmlExporter = new XmlExporter(treeBag, "/home/fabien/Bureau/Hadoop/data_test/toto.xml", 
					new HashMap<String, String>(), namesInfo);
			xmlExporter.launch();
			System.out.println("XML Export done.");
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Surprisingly mapper has not crashed");
	}

	public String[][] parseDatabase(String databaseContents) throws IOException {
		ArrayList<String[]> databaseList = new ArrayList<String[]>();
		for(String line: databaseContents.split("\n")) {
			databaseList.add(parseExample(line));
		}
		String[][] database = new String[databaseList.size()][];
		for (int i = 0; i < databaseList.size(); ++i) {
			database[i] = databaseList.get(i);
		}
		return database;
	}
	
	private String[] parseExample(String example) {
		example = example.substring(0, example.length() - 1);
		String[] tokens = example.split(",");
		for (int i = 0; i < tokens.length; ++i) {
			tokens[i] = tokens[i].trim();
		}
		return tokens;
	}

}
