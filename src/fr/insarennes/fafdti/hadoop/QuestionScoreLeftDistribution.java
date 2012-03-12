package fr.insarennes.fafdti.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoreLeftDistribution;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class QuestionScoreLeftDistribution implements
		Writable, Cloneable {

	private Question question;
	private ScoreLeftDistribution scoreLeftDistribution;
	public static final String DELIMITER = " ";

	public Question getQuestion() {
		return question;
	}

	public ScoreLeftDistribution getScoreLeftDistribution() {
		return scoreLeftDistribution;
	}

	public QuestionScoreLeftDistribution() {
		this.question = new Question();
		this.scoreLeftDistribution = new ScoreLeftDistribution();
	}

	public QuestionScoreLeftDistribution(Question question,
			ScoreLeftDistribution scoreLeftDistribution) {
		super();
		// FIXME Maybe we need to clone objects here
		this.question = question;
		this.scoreLeftDistribution = scoreLeftDistribution;
	}

	public QuestionScoreLeftDistribution(String strRepr) {
		this();
		fromString(strRepr);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		// byte[] inBuffer = new byte[1024];
		// try {
		// in.readFully(inBuffer);
		// } catch (Exception e) {
		//
		// }
		// OutputStream out = new FileOutputStream("/tmp/rawBuff.hex", true);
		// out.write(inBuffer);
		// System.out.println();
		this.scoreLeftDistribution.readFields(in);
		this.question.readFields(in);
		// System.out.println("Got question : " + this.question);
		// System.out.println("Char: " + in.readChar());

	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.scoreLeftDistribution.write(out);
		this.question.write(out);
		// out.writeChar('Z');
		//
	}

	@Override
	public Object clone() {
		QuestionScoreLeftDistribution qDVPair = null;
		try {
			qDVPair = (QuestionScoreLeftDistribution) super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace();
		}
		qDVPair.question = (Question) this.question.clone();
		qDVPair.scoreLeftDistribution = (ScoreLeftDistribution) 
				this.scoreLeftDistribution.clone();
		return qDVPair;
	}

	@Override
	public String toString() {
		String strRepr = this.question.toString();
		strRepr += DELIMITER;
		strRepr += this.scoreLeftDistribution.toString();
		return strRepr;
	}

	public void fromString(String strRepr) {
		String[] fields = strRepr.split(DELIMITER, 2);
		this.question = new Question(fields[0]);
		this.scoreLeftDistribution.fromString(fields[1]);
	}
}
