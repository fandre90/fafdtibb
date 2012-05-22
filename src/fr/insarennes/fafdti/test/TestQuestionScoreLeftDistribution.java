package fr.insarennes.fafdti.test;

import static fr.insarennes.fafdti.test.UtilsTest.bufferToDataInput;
import static fr.insarennes.fafdti.test.UtilsTest.bufferToDataOutput;
import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.junit.Test;

import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;

public class TestQuestionScoreLeftDistribution {

	public static ArrayList<QuestionScoreLeftDistribution> getQSLDistList(
				String filename) throws IOException {
		ArrayList<QuestionScoreLeftDistribution> qSLDistList = 
				new ArrayList<QuestionScoreLeftDistribution>();
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
        	QuestionScoreLeftDistribution qSLDist = 
        			new QuestionScoreLeftDistribution(line);
        	qSLDistList.add(qSLDist);
        	assertEquals(qSLDist.toString(), line);
        }
        bufferedReader.close();
        return qSLDistList;
	}

	@Test
	public void testWrite() throws IOException {
		URL url = this.getClass().getResource("res/petitester-question_list.txt");
		ArrayList<QuestionScoreLeftDistribution> qSLDistList =
				getQSLDistList(url.getPath());
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		for(QuestionScoreLeftDistribution qSLDist: qSLDistList) {
        	qSLDist.write(dataOut);
		}
		DataInput dataIn = bufferToDataInput(buffer);
		QuestionScoreLeftDistribution qSLDist = 
				new QuestionScoreLeftDistribution();
		for(int i=0; i<qSLDistList.size(); ++i){
			qSLDist.readFields(dataIn);
			assertEquals(qSLDistList.indexOf(qSLDist), i);
		}
	}
	
	@Test
	public void testClone() throws IOException {
		URL url = this.getClass().getResource("res/petitester-question_list.txt");
		ArrayList<QuestionScoreLeftDistribution> qSLDistList =
				getQSLDistList(url.getPath());
		ArrayList<QuestionScoreLeftDistribution> qSLDistCloneList =
				new ArrayList<QuestionScoreLeftDistribution>();
		for(QuestionScoreLeftDistribution qSLDist: qSLDistList) {
			qSLDistCloneList.add((QuestionScoreLeftDistribution) qSLDist.clone());
		}
		for(int i=0; i<qSLDistList.size(); ++i){
			assertEquals(qSLDistList.get(i), qSLDistCloneList.get(i));
			assertNotSame(qSLDistList.get(i), qSLDistCloneList.get(i));
		}
	}
}
