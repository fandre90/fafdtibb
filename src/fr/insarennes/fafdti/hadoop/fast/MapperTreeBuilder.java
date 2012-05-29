package fr.insarennes.fafdti.hadoop.fast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingInterrogator;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.nodebuilder.FastNodeBuilderFactory;
import fr.insarennes.fafdti.builder.scheduler.DumbScheduler;
import fr.insarennes.fafdti.builder.scheduler.IScheduler;
import fr.insarennes.fafdti.builder.stopcriterion.DepthMax;
import fr.insarennes.fafdti.builder.stopcriterion.ExampleMin;
import fr.insarennes.fafdti.builder.stopcriterion.GainMin;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.builder.treebuilder.DumbTreeBuilderFactory;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.EntropyCriterion;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.visitors.XmlExporter;

public class MapperTreeBuilder extends MapReduceBase implements
		Mapper<Object, Text, NullWritable, Text> {

	private DotNamesInfo namesInfo;
	private List<StoppingCriterion> stopCriteria;
	private ParentInfos parentInfos;
	private Criterion criterion;
	private static Logger log = Logger.getLogger(MapperTreeBuilder.class);
	
	@Override
	public void configure(JobConf jobConf) {
		try {
			namesInfo = DotNamesInfo.fromConf(jobConf);
			parentInfos = ParentInfos.fromConf(jobConf);
			// FIXME
			DepthMax dmax = DepthMax.fromConf(jobConf);
			ExampleMin exmin = ExampleMin.fromConf(jobConf);
			GainMin gmin = GainMin.fromConf(jobConf);
			stopCriteria = new ArrayList<StoppingCriterion>();
			stopCriteria.add(dmax);
			stopCriteria.add(exmin);
			stopCriteria.add(gmin);
			criterion = Criterion.fromConf(jobConf);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void map(Object key, Text value,
			OutputCollector<NullWritable, Text> output, Reporter reporter)
			throws IOException {
		log.info("Full tree builder mapper started");
		StatBuilder stats = new StatBuilder(1);
		DecisionTreeHolder treeHolder = new DecisionTreeHolder();
		Criterion criterion = new EntropyCriterion();
		FastNodeBuilderFactory nodeBuilderFactory = new FastNodeBuilderFactory(
				new EntropyCriterion(), namesInfo);
		String[][] database = parseDatabase(value.toString());
		DumbTreeBuilderFactory treeBuilderMaker = new DumbTreeBuilderFactory();
		IScheduler scheduler = new DumbScheduler();
		DecisionNodeSetter nodeSetter = treeHolder.getNodeSetter();
		Runnable treeBuilder = treeBuilderMaker.makeTreeBuilder(namesInfo, "/",
				criterion, nodeSetter, stopCriteria, stats, nodeBuilderFactory,
				database, parentInfos, null, treeBuilderMaker, scheduler);
		treeBuilder.run();
		BaggingTrees treeBag = new BaggingTrees(1);
		try {
			treeBag.setTree(0, treeHolder.getRoot());
			XmlExporter xmlExporter = new XmlExporter(treeBag,
					new HashMap<String, String>(), namesInfo);
			String xmlTree = xmlExporter.exportToString();
			output.collect(NullWritable.get(), new Text(xmlTree));
			log.info("Full tree builder mapper : XML export done");
		} catch (FAFException e) {
			e.printStackTrace();
		}
	}

	public String[][] parseDatabase(String databaseContents) throws IOException {
		ArrayList<String[]> databaseList = new ArrayList<String[]>();
		for (String line : databaseContents.split("\n")) {
			databaseList.add(parseExample(line));
		}
		String[][] database = new String[databaseList.size()][];
		for (int i = 0; i < databaseList.size(); ++i) {
			database[i] = databaseList.get(i);
		}
		return database;
	}

	private String[] parseExample(String example) {
		example = example.substring(0, example.lastIndexOf('.'));
		String[] tokens = example.split(",");
		for (int i = 0; i < tokens.length; ++i) {
			tokens[i] = tokens[i].trim();
		}
		return tokens;
	}

}
