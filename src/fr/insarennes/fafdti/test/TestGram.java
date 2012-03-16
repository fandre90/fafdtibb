package fr.insarennes.fafdti.test;

import static org.junit.Assert.*;

import org.apache.commons.net.nntp.NewGroupsOrNewsQuery;
import org.junit.Test;

import fr.insarennes.fafdti.builder.FGram;
import fr.insarennes.fafdti.builder.Gram;
import fr.insarennes.fafdti.builder.SGram;

public class TestGram {

	@Test
	public void testCloneGram() {
		FGram gram1 = new FGram(new String[]{"aa", "bb"});
		testGram1(gram1);
		FGram gram1Clone = gram1.cloneGram();
		assertNotSame(gram1, gram1Clone);
		testGram1(gram1Clone);
		SGram gram2 =  new SGram("aa", "bb", 1);
		testGram2(gram2);
		SGram gram2Clone = gram2.cloneGram();
		assertNotSame(gram2, gram2Clone);
		testGram2(gram2);
	}
	
	private void testGram1(FGram gram1) {
		assertTrue(gram1.query("cc aa bb cc dd ff"));
		assertFalse(gram1.query("cc aa cc cc dd ff"));
	}
	
	private void testGram2(SGram gram2) {
		assertTrue(gram2.query("cc aa bb dd"));
		assertTrue(gram2.query("cc aa dd bb"));
		assertFalse(gram2.query("cc bb aa dd"));
		assertFalse(gram2.query("aa cc dd dd bb"));
	}

}
