package fr.insarennes.fafdti.builder;

import org.apache.hadoop.io.Writable;

public interface WritableAskable extends Writable{
	public boolean query(String textData);
}
