package fr.insarennes.fafdti.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.junit.Test;

import org.apache.hadoop.mrunit.mapreduce.MapDriver;

import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.hadoop.Step1Map;

public class TestStep1Map {

	public Configuration generateConfiguration(String resourcePath) 
			throws ParseException, IOException {
		URL url = this.getClass().getResource(resourcePath);
		Path path = new Path(url.getPath());
		Configuration conf = new Configuration();
		DotNamesInfo dotNames = new DotNamesInfo(path, FileSystem.get(conf));
		dotNames.toConf(conf);
		return conf;
	}

	@Test
	public void testSGramGeneration() throws ParseException, IOException {
		Configuration conf = generateConfiguration("res/test2SGram.names");
		MapDriver<Object, Text, Question, IntWritable> mapDriver = 
				new MapDriver<Object, Text, Question, IntWritable>();
		mapDriver.withConfiguration(conf);
		mapDriver.setMapper(new Step1Map());
		mapDriver.withInput(null, new Text("aaa bbb ccc ddd, classA."));
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
		mapDriver.runTest();
	}

	@Test
	public void testFGramGeneration() throws ParseException, IOException {
		Configuration conf = generateConfiguration("res/test2FGram.names");
		MapDriver<Object, Text, Question, IntWritable> mapDriver = 
				new MapDriver<Object, Text, Question, IntWritable>();
		mapDriver.withConfiguration(conf);
		mapDriver.setMapper(new Step1Map());
		mapDriver.withInput(null, new Text("aaa bbb ccc ddd, classA."));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"aaa", "bbb"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"bbb", "ccc"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ccc", "ddd"})), new IntWritable(0));
		mapDriver.runTest();
	}

	@Test
	public void testNGramGeneration() throws ParseException, IOException {
		Configuration conf = generateConfiguration("res/test2NGram.names");
		MapDriver<Object, Text, Question, IntWritable> mapDriver = 
				new MapDriver<Object, Text, Question, IntWritable>();
		mapDriver.withConfiguration(conf);
		mapDriver.setMapper(new Step1Map());
		mapDriver.withInput(null, new Text("aaa bbb ccc ddd, classA."));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"aaa"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"aaa", "bbb"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"bbb"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"bbb", "ccc"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ccc"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ccc", "ddd"})), new IntWritable(0));
		mapDriver.withOutput(new Question(0, AttrType.TEXT, 
				new FGram(new String[]{"ddd"})), new IntWritable(0));
		mapDriver.runTest();
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
