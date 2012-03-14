package fr.insarennes.fafdti.test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import fr.insarennes.fafdti.builder.FGram;
import fr.insarennes.fafdti.builder.SGram;


public class TestSGram {

	@Test
	public void testWrite() throws IOException {
		SGram sGram = new SGram("aaa", "bbb", 1);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = new DataOutputStream(buffer);
		sGram.write(dataOut);
		DataInput dataIn = new DataInputStream(new ByteArrayInputStream(
				buffer.toByteArray()));
		sGram.readFields(dataIn);
		assertTrue(sGram.query("ddd aaa ccc bbb yyy"));
		assertTrue(sGram.query("aaa bbb"));
		assertFalse(sGram.query("bbb aaa ccc aaa"));
	}

	@Test
	public void testQuery() {
		SGram sGram1 = new SGram("aaa", "bbb", 3);
		assertTrue(sGram1.query("aaa ccc ddd eee bbb"));
		assertTrue(sGram1.query("aaa ccc ddd bbb"));
		assertTrue(sGram1.query("aaa ccc bbb"));
		assertTrue(sGram1.query("aaa bbb"));
		assertTrue(sGram1.query("ccc aaa aaa ccc ddd eee bbb"));
		assertTrue(sGram1.query(
				"aaa ttt yyy iii ooo ccc aaa bbb aaa ccc ddd eee bbb uuu"));
		assertFalse(sGram1.query("ccc ddd"));
	}

}
