package fr.insarennes.fafdti.builder.gram;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class SGram implements WritableComparable<SGram> {


	private Text firstWord;
	private Text lastWord;
	private IntWritable maxDistance;
	


	public SGram() {
		this.firstWord = new Text();
		this.lastWord = new Text();
		this.maxDistance = new IntWritable();
	}

	public SGram(String firstWord, String lastWord, int distance) {
		this();
		this.firstWord.set(firstWord);
		this.lastWord.set(lastWord);
		if(distance < 0) {
			throw new IllegalArgumentException("Distance between two words in " +
					"a SGram must be superior or equal to zero");
		}
		this.maxDistance.set(distance);
	}

	public Text getFirstWord() {
		return firstWord;
	}

	public Text getLastWord() {
		return lastWord;
	}

	public IntWritable getMaxDistance() {
		return maxDistance;
	}

	public SGram cloneGram() {
	    SGram sGram = new SGram();
	    sGram.firstWord = new Text(this.firstWord);
	    sGram.lastWord = new Text(this.lastWord);
	    sGram.maxDistance = new IntWritable(this.maxDistance.get());
	    return sGram;
	}
	
	public boolean query(String textData) {
		String[] words = textData.split("\\s+");
		int maxDistance = this.maxDistance.get();
		for(int i=0; i<words.length - 1; ++i) {
			int distanceLimit = Math.min(words.length - i - 2, maxDistance);
			for(int distance=0; distance<=distanceLimit; distance++) {
				if(words[i].equals(this.firstWord.toString()) &&
				   words[i + distance + 1].equals(this.lastWord.toString())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.maxDistance.readFields(in);
		this.firstWord.readFields(in);
		this.lastWord.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.maxDistance.write(out);
		this.firstWord.write(out);
		this.lastWord.write(out);
	}

	@Override
	public int compareTo(SGram other) {
		int comparison = this.maxDistance.compareTo(other.maxDistance);
		if(comparison != 0) {
			return comparison;
		}
		comparison = this.firstWord.compareTo(other.firstWord);
		if(comparison != 0) {
			return comparison;
		}
		comparison = this.lastWord.compareTo(other.lastWord);
		if(comparison != 0) {
			return comparison;
		}
		return 0;
		
	}
	
	@Override
	public String toString() {
		String strRepr = this.maxDistance.toString() + GramContainer.DELIMITER;
		strRepr += this.firstWord.toString() + GramContainer.DELIMITER;
		strRepr += this.lastWord.toString();
		return strRepr;
	}
	
	public void fromString(String strRepr) {
		String[] fields = strRepr.split(GramContainer.DELIMITER);
		int maxDistance = Integer.parseInt(fields[0]);
		this.maxDistance.set(maxDistance);
		this.firstWord.set(fields[1]);
		this.lastWord.set(fields[2]);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((firstWord == null) ? 0 : firstWord.hashCode());
		result = prime * result
				+ ((lastWord == null) ? 0 : lastWord.hashCode());
		result = prime * result
				+ ((maxDistance == null) ? 0 : maxDistance.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SGram other = (SGram) obj;
		if (firstWord == null) {
			if (other.firstWord != null)
				return false;
		} else if (!firstWord.equals(other.firstWord))
			return false;
		if (lastWord == null) {
			if (other.lastWord != null)
				return false;
		} else if (!lastWord.equals(other.lastWord))
			return false;
		if (maxDistance == null) {
			if (other.maxDistance != null)
				return false;
		} else if (!maxDistance.equals(other.maxDistance))
			return false;
		return true;
	}
}