package fr.insarennes.fafdti.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.Gram;
import fr.insarennes.fafdti.builder.gram.GramContainer;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;

public class Value implements Writable, Comparable<Value> {

	AttrType type = null;
	DoubleWritable doubleValue = null;
	Text textValue = null;
	GramContainer gramValue = null;

	public Value(double d) {
		this.type = AttrType.CONTINUOUS;
		this.doubleValue = new DoubleWritable(d);
	}
	
	public Value(String s) {
		this.type = AttrType.DISCRETE;
		this.textValue = new Text(s);
	}
	
	public Value(FGram fGram) {
		this.type = AttrType.TEXT;
		this.gramValue = new GramContainer(fGram);
	}

	public Value(SGram sGram) {
		this.type = AttrType.TEXT;
		this.gramValue = new GramContainer(sGram);
	}

	public Value() {
		this.type = null;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		char typeChar = in.readChar();
		switch (typeChar) {
		case 'C':
			this.type = AttrType.CONTINUOUS;
			if(this.doubleValue == null) {
				this.doubleValue = new DoubleWritable();
			}
			this.gramValue = null;
			this.textValue = null;
			this.doubleValue.readFields(in);
			break;
		case 'T':
			this.type = AttrType.TEXT;
			if(this.gramValue == null) {
				this.gramValue = new GramContainer();
			}
			this.doubleValue = null;
			this.textValue = null;
			this.gramValue.readFields(in);
			break;
		case 'D':
			this.type = AttrType.DISCRETE;
			if(this.textValue == null) {
				this.textValue = new Text();
			}
			this.gramValue = null;
			this.doubleValue = null;
			this.textValue.readFields(in);
			break;
		default:
			throw new IllegalStateException("Bad type description character "
					+ typeChar);
		}	
	}
	
	public static char typeToChar(AttrType type) {
		char typeChar = 'C';
		switch (type) {
		case CONTINUOUS:
			typeChar = 'C';
			break;
		case DISCRETE:
			typeChar = 'D';
			break;
		case TEXT:
			typeChar = 'T';
			break;
		}
		return typeChar;
	}

	public static AttrType charToType(char typeChar) {
		switch (typeChar) {
		case 'C':
			return AttrType.CONTINUOUS;
		case 'T':
			return AttrType.TEXT;
		case 'D':
			return AttrType.DISCRETE;
		default:
			throw new IllegalArgumentException(
					"Bad type description character " + typeChar);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		switch (this.type) {
		case CONTINUOUS:
			out.writeChar('C');
			this.doubleValue.write(out);
			break;
		case DISCRETE:
			out.writeChar('D');
			this.textValue.write(out);
			break;
		case TEXT:
			out.writeChar('T');
			this.gramValue.write(out);
			break;
		}
	}

	@Override
	public int compareTo(Value other) {
		int comparison = this.type.compareTo(other.type);
		if (comparison != 0)
			return comparison;
		if (this.type == AttrType.DISCRETE)
			return this.textValue.compareTo(other.textValue);
		if (this.type == AttrType.CONTINUOUS) {
			return this.doubleValue.compareTo(other.doubleValue);
		}
		if(this.type == AttrType.TEXT) {
			return this.gramValue.compareTo(other.gramValue);
		}
		return 0;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Value other = (Value) obj;
		if (type != other.type)
			return false;
		if (this.type == AttrType.DISCRETE) {
			if (textValue == null) {
				if (other.textValue != null)
					return false;
			} else if (!textValue.equals(other.textValue))
				return false;
		} else if(this.type == AttrType.CONTINUOUS){
			if (doubleValue == null) {
				if (other.doubleValue != null)
					return false;
			} else if (!doubleValue.equals(other.doubleValue))
				return false;
		} else if (this.type == AttrType.TEXT) {
			if (gramValue == null) {
				if (other.gramValue != null)
					return false;
			} else if (!gramValue.equals(other.gramValue))
				return false;
		}
		return true;
	}

	public double getDoubleValue() {
		return this.doubleValue.get();
	}

	public String getTextValue() {
		return this.textValue.toString();
	}

	public void set(String s) {
		this.type = AttrType.DISCRETE;
		this.gramValue = null;
		this.doubleValue = null;
		if(this.textValue == null) {
			this.textValue = new Text(s);
		} else {
			this.textValue.set(s);
		}
	}
	
	public void set(FGram fGram) {
		this.type = AttrType.TEXT;
		this.doubleValue = null;
		this.textValue = null;
		if(this.gramValue == null) {
			this.gramValue = new GramContainer(fGram);
		} else {
			this.gramValue.set(fGram);
		}
	}
	
	public void set(SGram sGram) {
		this.type = AttrType.TEXT;
		this.doubleValue = null;
		this.textValue = null;
		if(this.gramValue == null) {
			this.gramValue = new GramContainer(sGram);
		} else {
			this.gramValue.set(sGram);
		}
	}

	public void set(double d) {
		this.type = AttrType.CONTINUOUS;
		this.gramValue = null;
		this.textValue = null;
		if(this.doubleValue == null) {
			this.doubleValue = new DoubleWritable(d);
		} else {
			this.doubleValue.set(d);
		}
	}
	
	public AttrType getType() {
		return type;
	}
	
	public Gram getGram(){
		return gramValue.getGram();
	}
}
