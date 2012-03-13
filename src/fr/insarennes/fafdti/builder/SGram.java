package fr.insarennes.fafdti.builder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

public class SGram implements WritableAskable {
	Text firstWord;
	Text lastWord;
	IntWritable size;

	public SGram() {
		this.firstWord = new Text();
		this.lastWord = new Text();
		this.size = new IntWritable();
	}

	public SGram(String firstWord, String lastWord, int size) {
		this();
		this.firstWord.set(firstWord);
		this.lastWord.set(lastWord);
		this.size.set(size);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.size.readFields(in);
		this.firstWord.readFields(in);
		this.lastWord.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.size.write(out);
		this.firstWord.write(out);
		this.lastWord.write(out);
	}

	@Override
	public boolean query(String textData) {
		String[] words = textData.split("\\s+");
		for(int i=0; i<words.length - size.get() - 1; ++i) {
			if(words[i].equals(this.firstWord.toString()) &&
			   words[i + size.get() + 1].equals(this.lastWord.toString())) {
				return true;
			}
		}
		return false;
	}
}
