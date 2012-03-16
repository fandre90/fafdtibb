package fr.insarennes.fafdti.test;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.junit.Test;

import fr.insarennes.fafdti.builder.FGram;
import fr.insarennes.fafdti.builder.GramContainer;
import fr.insarennes.fafdti.builder.SGram;

import static fr.insarennes.fafdti.test.UtilsTest.*;

public class TestGramContainers {

	public static final SGram testSGram = new SGram("aaa", "bbb", 3);
	public static final FGram testFGram = new FGram(
			new String[] { "aaa", "bbb" });

	@Test
	public void testSetSGram() {
		GramContainer gramCont =  new GramContainer();
		gramCont.set(testSGram);
		testGramContWithSGram(gramCont);
	}
	
	@Test
	public void testSetFGram() {
		GramContainer gramCont =  new GramContainer();
		gramCont.set(testFGram);
		testGramContWithFGram(gramCont);
	}

	@Test
	public void testWriteSGram() throws IOException {
		GramContainer gramCont = new GramContainer(testSGram);
		gramCont.set(testSGram);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		gramCont.write(dataOut);
		GramContainer gramCont2 = new GramContainer();
		DataInput dataIn = bufferToDataInput(buffer);
		gramCont2.readFields(dataIn);
		testGramContWithSGram(gramCont2);
	}

	@Test
	public void testWriteFGram() throws IOException {
		GramContainer gramCont = new GramContainer(testFGram);
		gramCont.set(testSGram);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		gramCont.write(dataOut);
		GramContainer gramCont2 = new GramContainer();
		DataInput dataIn = bufferToDataInput(buffer);
		gramCont2.readFields(dataIn);
		testGramContWithFGram(gramCont2);
	}
	
	@Test
	public void testQueryFGram() {
		GramContainer gramCont = new GramContainer(testFGram);
		testGramContWithFGram(gramCont);

	}

	@Test
	public void testQuerySGram() {
		GramContainer gramCont = new GramContainer(testSGram);
		testGramContWithSGram(gramCont);
	}

	@Test
	public void testToStringSGram() {
		GramContainer gramCont = new GramContainer(testSGram);
		String strRepr = gramCont.toString();
		GramContainer gramCont2 = new GramContainer();
		gramCont2.fromString(strRepr);
		testGramContWithSGram(gramCont2);
	}
	
	@Test
	public void testToStringFGram() {
		GramContainer gramCont = new GramContainer(testFGram);
		String strRepr = gramCont.toString();
		GramContainer gramCont2 = new GramContainer();
		gramCont2.fromString(strRepr);
		testGramContWithFGram(gramCont2);
	}

	private void testGramContWithSGram(GramContainer gramCont) {
		assertTrue(gramCont.query("aaa ccc ddd eee bbb"));
		assertTrue(gramCont.query("aaa ccc ddd bbb"));
		assertTrue(gramCont.query("aaa ccc bbb"));
		assertTrue(gramCont.query("aaa bbb"));
		assertTrue(gramCont.query("ccc aaa aaa ccc ddd eee bbb"));
		assertTrue(gramCont
				.query("aaa ttt yyy iii ooo ccc aaa bbb aaa ccc ddd eee bbb uuu"));
		assertFalse(gramCont.query("ccc ddd"));
	}

	private void testGramContWithFGram(GramContainer gramCont) {
		assertTrue(gramCont.query("aaa bbb"));
		assertTrue(gramCont.query("fff ddd ccc aaa bbb"));
		assertFalse(gramCont.query("bbb aaa ccc aaa"));
	}
}
