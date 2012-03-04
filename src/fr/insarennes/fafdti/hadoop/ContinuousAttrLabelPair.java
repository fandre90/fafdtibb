package fr.insarennes.fafdti.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;

public class ContinuousAttrLabelPair implements Cloneable, WritableComparable<ContinuousAttrLabelPair> {
	private IntWritable labelIndex;
	private DoubleWritable continuousValue;

	public ContinuousAttrLabelPair() {
		this.labelIndex = new IntWritable();
		this.continuousValue = new DoubleWritable();
	}

	public ContinuousAttrLabelPair(int labelIndex, double continuousValue) {
		this();
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

	@Override
	public String toString() {
		return "ContinuousAttrLabelPair [labelIndex=" + labelIndex
				+ ", continuousValue=" + continuousValue + "]";
	}
	
	public Object clone() {
		ContinuousAttrLabelPair vlPair = new ContinuousAttrLabelPair(getLabelIndex(),getContinuousValue());
	    /*try {
	      	vlPair = (ContinuousAttrLabelPair) super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	// Ne devrait jamais arriver car nous implémentons 
	      	// l'interface Cloneable
	      	cnse.printStackTrace(System.err);
	    }*/
	    
	    /*vlPair.labelIndex = this.labelIndex;
	    vlPair.continuousValue = this.continuousValue;*/
	    return vlPair;
	}
}
