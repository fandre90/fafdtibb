package fr.insarennes.fafdti.builder.gram;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class FGram implements WritableComparable<FGram>, Gram {

	Text[] words;

	public FGram() {}

	public FGram(String[] words) {
		this.words = new Text[words.length];
		for (int i = 0; i < words.length; ++i) {
			this.words[i] = new Text(words[i]);
		}
	}


	public FGram cloneGram() {
	    FGram fGram = new FGram();
	    fGram.words = new Text[this.words.length];
	    for(int i=0; i< this.words.length; ++i) {
	    	fGram.words[i] = new Text(this.words[i]);
	    }
	    return fGram;
	}
	
	public Text[] getWords(){
		return words;
	}

	public boolean query(String textData) {
		String[] textWords = textData.split("\\s+");
		int gramSize = this.words.length;
		for (int i = 0; i < textWords.length - gramSize + 1; ++i) {
			boolean allIndical = true;
			for (int j = 0; j < gramSize; j++) {
				if (!textWords[i + j].equals(words[j].toString())) {
					allIndical = false;
					break;
				}
			}
			if (allIndical) {
				return true;
			}
		}
		return false;
	}

	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		this.words = new Text[size];
		for (int i = 0; i < size; ++i) {
			this.words[i] = new Text(in.readUTF());
		}
	}

	public void write(DataOutput out) throws IOException {
		int size = this.words.length;
		out.writeInt(size);
		for (Text word : this.words) {
			out.writeUTF(word.toString());
		}
	}


	@Override
	public int compareTo(FGram other) {
		if(this.words.length > other.words.length) {
			return -1;
		}
		if(this.words.length < other.words.length) {
			return 1;
		}
		int comparison;
		for(int i=0; i<this.words.length; i++) {
			comparison = this.words[i].compareTo(other.words[i]);
			if(comparison != 0) {
				return comparison;
			}
		}
		return 0;
	}
	@Override
	public String toString() {
		String strRepr = "";
		for(Text word: this.words) {
			strRepr += word + GramContainer.DELIMITER;
		}
		return strRepr;
	}
	
	public void fromString(String strRepr) {
		String[] words = strRepr.split(GramContainer.DELIMITER);
		this.words = new Text[words.length];
		for (int i = 0; i < words.length; ++i) {
			this.words[i] = new Text(words[i]);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(words);
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
		FGram other = (FGram) obj;
		if (!Arrays.equals(words, other.words))
			return false;
		return true;
	}
}
