package fr.insarennes.fafdti.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.junit.Test;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.FGram;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.SGram;

import static fr.insarennes.fafdti.test.UtilsTest.*;

public class TestQuestion {

	private final Question continuousQuestion = new Question(1,
			AttrType.CONTINUOUS, 42);
	private final Question discreteQuestion = new Question(1,
			AttrType.DISCRETE, "Val1");
	private final Question fGramQuestion = new Question(1, AttrType.TEXT,
			new FGram(new String[] { "aa", "bb" }));
	private final Question sGramQuestion = new Question(1, AttrType.TEXT,
			new SGram("aa", "bb", 2));

	private void testFGramQuestion(Question fGramQuestion) throws FAFException {
		assertTrue(fGramQuestion.ask("aa aa bb cc bb"));
		assertFalse(fGramQuestion.ask("tt bb bb aa"));
	}

	private void testSGramQuestion(Question fGramQuestion) throws FAFException {
		assertTrue(fGramQuestion.ask("aa aa bb cc bb"));
		assertTrue(fGramQuestion.ask("aa aa xx bb cc bb"));
		assertTrue(fGramQuestion.ask("aa aa xx xx bb cc bb"));
		assertFalse(fGramQuestion.ask("aa aa xx xx xx bb cc bb"));
		assertFalse(fGramQuestion.ask("tt bb bb aa"));
	}

	private void testDiscreteQuestion(Question discreteQuestion)
			throws FAFException {
		assertTrue(discreteQuestion.ask("Val1"));
		assertFalse(discreteQuestion.ask("Val2"));
	}

	private void testContinuousQuestion(Question continuousQuestion)
			throws FAFException {
		assertTrue(continuousQuestion.ask("41"));
		assertTrue(continuousQuestion.ask("42"));
		assertFalse(continuousQuestion.ask("53"));
	}

	@Test
	public void testWriteDiscrete() throws IOException, FAFException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		discreteQuestion.write(dataOut);
		DataInput dataIn = bufferToDataInput(buffer);
		Question question = new Question();
		question.readFields(dataIn);
		testDiscreteQuestion(question);
	}

	@Test
	public void testWriteContinuous() throws IOException, FAFException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		continuousQuestion.write(dataOut);
		DataInput dataIn = bufferToDataInput(buffer);
		Question question = new Question();
		question.readFields(dataIn);
		testContinuousQuestion(question);
	}

	@Test
	public void testWriteFGram() throws IOException, FAFException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		fGramQuestion.write(dataOut);
		DataInput dataIn = bufferToDataInput(buffer);
		Question question = new Question();
		question.readFields(dataIn);
		testFGramQuestion(question);
	}

	@Test
	public void testWriteSGram() throws IOException, FAFException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		sGramQuestion.write(dataOut);
		DataInput dataIn = bufferToDataInput(buffer);
		Question question = new Question();
		question.readFields(dataIn);
		testSGramQuestion(question);
	}

	@Test
	public void testCompareTo() {
		assertTrue(continuousQuestion.compareTo(discreteQuestion) == -(discreteQuestion
				.compareTo(continuousQuestion)));
		assertTrue(fGramQuestion.compareTo(discreteQuestion) == -(discreteQuestion
				.compareTo(fGramQuestion)));
		assertTrue(fGramQuestion.compareTo(sGramQuestion) == -(sGramQuestion
				.compareTo(fGramQuestion)));
		Question fGramQuestion2 = new Question(1, AttrType.TEXT, new FGram(
				new String[] { "cc", "dd" }));
		assertTrue(fGramQuestion.compareTo(fGramQuestion2) == -(fGramQuestion2
				.compareTo(fGramQuestion)));
		Question sGramQuestion2 = new Question(1, AttrType.TEXT, new SGram(
				"cc", "dd", 2));
		assertTrue(sGramQuestion.compareTo(sGramQuestion2) == -(sGramQuestion2
				.compareTo(sGramQuestion)));
		Question sGramQuestion3 = new Question(1, AttrType.TEXT, new SGram(
				"aa", "cc", 2));
		assertTrue(sGramQuestion.compareTo(sGramQuestion3) == -(sGramQuestion3
				.compareTo(sGramQuestion)));
		Question sGramQuestion4 = new Question(1, AttrType.TEXT,
				new SGram("aa", "bb", 2));
		assertEquals(sGramQuestion4.compareTo(sGramQuestion), 0);
		Question continuousQuestion2 = new Question(1,
				AttrType.CONTINUOUS, 42);
		assertEquals(continuousQuestion.compareTo(continuousQuestion2), 0);
	}

	@Test
	public void testToStringDiscrete() throws FAFException {
		String strRepr = discreteQuestion.toString();
		Question question = new Question();
		question.fromString(strRepr);
		testDiscreteQuestion(question);
	}
	
	@Test
	public void testToStringContinuous() throws FAFException {
		String strRepr = continuousQuestion.toString();
		Question question = new Question();
		question.fromString(strRepr);
		testContinuousQuestion(question);
	}
	
	@Test
	public void testToStringFGram() throws FAFException {
		String strRepr = fGramQuestion.toString();
		Question question = new Question();
		question.fromString(strRepr);
		testFGramQuestion(question);
	}

	@Test
	public void testToStringSGram() throws FAFException {
		String strRepr = sGramQuestion.toString();
		Question question = new Question();
		question.fromString(strRepr);
		testSGramQuestion(question);
	}

}
