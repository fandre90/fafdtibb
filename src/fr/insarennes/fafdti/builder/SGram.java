package fr.insarennes.fafdti.builder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class SGram implements WritableAskable {
	Text firstWord;
	Text lastWord;
	IntWritable maxDistance;

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
}