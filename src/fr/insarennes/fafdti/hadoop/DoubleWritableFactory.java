package fr.insarennes.fafdti.hadoop;

import org.apache.hadoop.io.DoubleWritable;

public class DoubleWritableFactory implements IFactory<DoubleWritable> {

	@Override
	public DoubleWritable newInstance() {
		return new DoubleWritable();
	}

}
