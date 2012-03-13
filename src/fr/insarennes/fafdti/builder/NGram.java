package fr.insarennes.fafdti.builder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Text;

public class NGram implements WritableAskable {

	Text[] words;

	public NGram() {
	}

	public NGram(String[] words) {
		this.words = new Text[words.length];
		for (int i = 0; i < words.length; ++i) {
			this.words[i] = new Text(words[i]);
		}
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		this.words = new Text[size];
		for (int i = 0; i < size; ++i) {
			this.words[i] = new Text(in.readUTF());
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		int size = this.words.length;
		out.writeInt(size);
		for (Text word : this.words) {
			out.writeUTF(word.toString());
		}
	}

	@Override
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
}
