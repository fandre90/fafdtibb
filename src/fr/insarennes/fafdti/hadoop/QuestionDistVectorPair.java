package fr.insarennes.fafdti.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.io.WritableComparable;

import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class QuestionDistVectorPair implements
		WritableComparable<QuestionDistVectorPair> {

	private Question question;
	private ScoredDistributionVector distributionVector;
	public static final String DELIMITER = "\t";

	public Question getQuestion() {
		return question;
	}

	public ScoredDistributionVector getDistributionVector() {
		return distributionVector;
	}

	public QuestionDistVectorPair() {
		this.question = new Question();
		this.distributionVector = new ScoredDistributionVector(0);
	}

	public QuestionDistVectorPair(Question question,
			ScoredDistributionVector distributionVector) {
		super();
		// FIXME Maybe we need to clone objects here
		this.question = question;
		this.distributionVector = distributionVector;
	}

	public QuestionDistVectorPair(String strRepr) {
		fromString(strRepr);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
//		byte[] inBuffer = new byte[1024];
//		try {
//			in.readFully(inBuffer);
//	} catch (Exception e) {
//
//		}
//		OutputStream out = new FileOutputStream("/tmp/rawBuff.hex", true);
//	out.write(inBuffer);
//		System.out.println();
		this.distributionVector.readFields(in);
		this.question.readFields(in);
		System.out.println("Got question : " + this.question);
		//System.out.println("Char: " + in.readChar());

	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.distributionVector.write(out);
		this.question.write(out);
		//out.writeChar('Z');
		//
	}

	@Override
	public int compareTo(QuestionDistVectorPair other) {
		if(!this.distributionVector.equals(other))
			return -1;
		if(!this.question.equals(other))
			return -1;
		return 0;
	}

	@Override
	public String toString() {
		String strRepr = this.question.toString();
		strRepr += DELIMITER;
		strRepr += this.distributionVector.toString();
		return strRepr;
	}
	
	public void fromString(String strRepr) {
		String[] fields = strRepr.split(DELIMITER);
		this.question = new Question(fields[0]);
		this.distributionVector = new ScoredDistributionVector(fields[1]);
	}
}
