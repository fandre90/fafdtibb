package fr.insarennes.fafdti.test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.insarennes.fafdti.builder.NGram;

public class TestNGram {

	@Test
	public void testWrite() throws IOException {
		NGram nGram = new NGram(new String[] { "aaa", "bbb" });
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = new DataOutputStream(buffer);
		nGram.write(dataOut);
		DataInput dataIn = new DataInputStream(new ByteArrayInputStream(
				buffer.toByteArray()));
		nGram.readFields(dataIn);
		assertTrue(nGram.query("aaa bbb"));
		assertTrue(nGram.query("fff ddd ccc aaa bbb"));
		assertFalse(nGram.query("bbb aaa ccc aaa"));
	}

	@Test
	public void testQuery() {
		NGram nGram1 = new NGram(new String[] { "aaa", "bbb" });
		assertTrue(nGram1.query("aaa bbb"));
		assertTrue(nGram1.query("fff ddd ccc aaa bbb"));
		assertFalse(nGram1.query("bbb aaa ccc aaa"));
	}

}
