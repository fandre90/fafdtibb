package fr.insarennes.fafdti.test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;
import org.junit.Test;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.nodebuilder.FastNodeBuilderFactory;
import fr.insarennes.fafdti.builder.nodebuilder.INodeBuilder;
import fr.insarennes.fafdti.builder.nodebuilder.NodeBuilderFast;
import fr.insarennes.fafdti.builder.EntropyCriterion;
import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;

public class TestNodeBuilderFast {

	public Path getResourcePath(String fileName) {
		URL url = this.getClass().getResource(fileName);
		return new Path(url.getPath());
	}

	public String[][] parseDatabase(Path dataPath) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fileSystem = FileSystem.get(conf);
		FSDataInputStream in = fileSystem.open(dataPath);
		LineReader lr = new LineReader(in);
		int readBytes;
		Text line = new Text();
		ArrayList<String[]> databaseList = new ArrayList<String[]>();
		while ((readBytes = lr.readLine(line)) != 0) {
			databaseList.add(parseExample(line.toString()));
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

	private Pair<DotNamesInfo, String[][]> getNamesInfoAndDatabase(
			String namesFile, String dataFile) throws IOException,
			ParseException {
		Path namesPath = getResourcePath(namesFile);
		Configuration conf = new Configuration();
		FileSystem fileSystem = FileSystem.get(conf);
		DotNamesInfo namesInfo = new DotNamesInfo(namesPath, fileSystem);
		Path dataPath = getResourcePath(dataFile);
		String database[][] = parseDatabase(dataPath);
		return new Pair<DotNamesInfo, String[][]>(namesInfo, database);
	}

	@Test
	public void testDiscreteContinuous() throws IOException, FAFException,
			InterruptedException, ClassNotFoundException {
		Pair<DotNamesInfo, String[][]> pInfoDb = getNamesInfoAndDatabase(
				"res/test1.names", "res/test1.data");
		DotNamesInfo namesInfo = pInfoDb.getFirst();
		String[][] database = pInfoDb.getSecond();
		FastNodeBuilderFactory nodeBuilderFactory = 
				new FastNodeBuilderFactory(new EntropyCriterion(), namesInfo);
		INodeBuilder nodeBuilder = nodeBuilderFactory.makeNodeBuilder(database, null, "0-0");
		QuestionScoreLeftDistribution qSLD = nodeBuilder.buildNode();
		Question expectedQuestion = new Question(0, AttrType.CONTINUOUS, 0.45);
		assertEquals(expectedQuestion, qSLD.getQuestion());
	}

	@Test
	public void testDiscreteText() throws IOException, FAFException,
			InterruptedException, ClassNotFoundException {
		/*
		Pair<DotNamesInfo, String[][]> pInfoDb = getNamesInfoAndDatabase(
				"petitester-ascii-sup6.names", "petitester-ascii-sup6.data");
		DotNamesInfo namesInfo = pInfoDb.getFirst();
		String[][] database = pInfoDb.getSecond();
		NodeBuilderFast nodeBuilder = new NodeBuilderFast(
				new EntropyCriterion(), namesInfo);
		QuestionScoreLeftDistribution qSLD = nodeBuilder.buildNode(database,
				null, null, null);
		Question expectedQuestion = new Question(0, AttrType.TEXT, new FGram(
				new String[] { "U-1:START", "U-1:START" }));
		System.out.println(qSLD.getQuestion());
		// assertEquals(expectedQuestion, p.getFirst());*/
	}

}
