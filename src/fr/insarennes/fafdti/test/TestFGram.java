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

import fr.insarennes.fafdti.builder.gram.FGram;
import static fr.insarennes.fafdti.test.UtilsTest.*;

public class TestFGram {

	@Test
	public void testWrite() throws IOException {
		FGram nGram = new FGram(new String[] { "aaa", "bbb" });
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		DataOutput dataOut = bufferToDataOutput(buffer);
		nGram.write(dataOut);
		DataInput dataIn = bufferToDataInput(buffer);
		nGram.readFields(dataIn);
		assertTrue(nGram.query("aaa bbb"));
		assertTrue(nGram.query("fff ddd ccc aaa bbb"));
		assertFalse(nGram.query("bbb aaa ccc aaa"));
	}

	@Test
	public void testToString() {
		FGram fGram = new FGram(new String[] { "aaa", "bbb" });
		String strRepr = fGram.toString();
		FGram fGram2 = new FGram();
		fGram2.fromString(strRepr);
		assertTrue(fGram2.query("aaa bbb"));
		assertTrue(fGram2.query("fff ddd ccc aaa bbb"));
		assertFalse(fGram2.query("bbb aaa ccc aaa"));
	}

	@Test
	public void testQuery() {
		FGram nGram1 = new FGram(new String[] { "aaa", "bbb" });
		assertTrue(nGram1.query("aaa bbb"));
		assertTrue(nGram1.query("fff ddd ccc aaa bbb"));
		assertFalse(nGram1.query("bbb aaa ccc aaa"));
	}

}
