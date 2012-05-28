package fr.insarennes.fafdti.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Test;

import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.types.Pair;

import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.hadoop.veryfurious.Step11Map;
import static fr.insarennes.fafdti.test.UtilsTest.*;

public class TestStep1Map {



	@Test
	public void testSGramGeneration() throws ParseException, IOException {
		URL url = this.getClass().getResource("res/test2SGram.names");
		Configuration conf = generateConfiguration(url.getPath());
		MapDriver<Object, Text, Question, IntWritable> mapDriver = 
				new MapDriver<Object, Text, Question, IntWritable>();
		mapDriver.withConfiguration(conf);
		mapDriver.setMapper(new Step11Map());
		mapDriver.withInput(0, new Text("aaa bbb ccc ddd, classA."));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, new SGram("aaa",
				"bbb", 2)), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, new SGram("aaa",
				"ccc", 2)), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, new SGram("aaa",
				"ddd", 2)), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, new SGram("bbb",
				"ccc", 2)), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, new SGram("bbb",
				"ddd", 2)), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, new SGram("ccc",
				"ddd", 2)), new IntWritable(0));
		mapDriver.runTest(false);
	}

	@Test
	public void testFGramGeneration() throws ParseException, IOException {
		URL url = this.getClass().getResource("res/test2FGram.names");
		Configuration conf = generateConfiguration(url.getPath());
		MapDriver<Object, Text, Question, IntWritable> mapDriver = 
				new MapDriver<Object, Text, Question, IntWritable>();
		mapDriver.withConfiguration(conf);
		mapDriver.setMapper(new Step11Map());
		mapDriver.withInput(0, new Text("aaa bbb ccc ddd, classA."));
		Question q1 = new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"aaa", "bbb"}));
		Pair<Question, IntWritable> p1 = new Pair<Question, IntWritable>(q1, new IntWritable(0));
		mapDriver.withOutput(q1, new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"bbb", "ccc"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ccc", "ddd"})), new IntWritable(0));
		List<Pair<Question, IntWritable>> lst = mapDriver.run();
		mapDriver.runTest(false);
	}

	@Test
	public void testNGramGeneration() throws ParseException, IOException {
		URL url = this.getClass().getResource("res/test2NGram.names");
		Configuration conf = generateConfiguration(url.getPath());
		MapDriver<Object, Text, Question, IntWritable> mapDriver = 
				new MapDriver<Object, Text, Question, IntWritable>();
		mapDriver.withConfiguration(conf);
		mapDriver.setMapper(new Step11Map());
		mapDriver.withInput(0, new Text("aaa bbb ccc ddd, classA."));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"aaa"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"bbb"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ccc"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ddd"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"aaa", "bbb"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"bbb", "ccc"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ccc", "ddd"})), new IntWritable(0));
		mapDriver.runTest(false);
	}

	@Test
	public void testQuestion() {
		Question q1 = new Question(0, AttrType.TEXT, new SGram("aaa",
				"bbb", 2));
		Question q2 = new Question(0, AttrType.TEXT, new SGram("aaa",
				"bbb", 2));
		assertEquals(q1, q2);
	}
}
