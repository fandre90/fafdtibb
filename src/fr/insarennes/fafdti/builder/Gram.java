package fr.insarennes.fafdti.builder;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

public abstract class Gram {
	public abstract boolean query(String textData);
	public abstract Gram cloneGram();
}
