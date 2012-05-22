package fr.insarennes.fafdti.test;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.junit.Test;

import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.hadoop.Step3Map;
import static fr.insarennes.fafdti.test.TestQuestionScoreLeftDistribution.*;
import static fr.insarennes.fafdti.test.UtilsTest.*;

public class TestStep3Map {

	@Test
	public void test() throws IOException, ParseException {
		MapDriver<Object, Text, Text, QuestionScoreLeftDistribution> mapDriver = 
				new MapDriver<Object, Text, Text, QuestionScoreLeftDistribution>();
        URL url2 = this.getClass().getResource("res/test2SGram.names");
        Configuration conf = generateConfiguration(url2.getPath());
        mapDriver.withConfiguration(conf);
        mapDriver.setMapper(new Step3Map());
		URL url = this.getClass().getResource("res/petitester-question_list.txt");
        FileReader fileReader = new FileReader(url.getPath());
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        int lineNum = 1;
        while ((line = bufferedReader.readLine()) != null) {
        	QuestionScoreLeftDistribution qSLDist = 
        			new QuestionScoreLeftDistribution(line);
        	System.out.println(line + " " + lineNum);
        	mapDriver.withInput(lineNum, new Text(line));
        	mapDriver.withOutput(new Text("best"), qSLDist);
            mapDriver.runTest();
            mapDriver.resetOutput();
        	lineNum++;
        }
        bufferedReader.close();
	}

}
