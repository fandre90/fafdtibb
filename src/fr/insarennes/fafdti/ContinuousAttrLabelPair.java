package fr.insarennes.fafdti;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

public class ContinuousAttrLabelPair implements WritableComparable<ContinuousAttrLabelPair> {
	private IntWritable labelIndex;
	private DoubleWritable continuousValue;

	public ContinuousAttrLabelPair() {
		this.labelIndex = new IntWritable();
		this.continuousValue = new DoubleWritable();
	}

	public ContinuousAttrLabelPair(int labelIndex, double continuousValue) {
		super();
		this.labelIndex.set(labelIndex);
		this.continuousValue.set(continuousValue);
	}

	public int getLabelIndex() {
		return labelIndex.get();
	}

	public double getContinuousValue() {
		return continuousValue.get();
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.continuousValue.readFields(in);
		this.labelIndex.readFields(in);
		
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.continuousValue.write(out);
		this.labelIndex.write(out);
	}

	@Override
	public int compareTo(ContinuousAttrLabelPair other) {
		if(this.getLabelIndex() != other.getLabelIndex())
			return -1;
		if(this.getContinuousValue() != other.getContinuousValue())
			return -1;
		return 0;
	}
	
	
}
