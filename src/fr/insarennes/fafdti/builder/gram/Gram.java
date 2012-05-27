package fr.insarennes.fafdti.builder.gram;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public interface Gram {
	public boolean query(String textData);
	public Gram cloneGram();
}
