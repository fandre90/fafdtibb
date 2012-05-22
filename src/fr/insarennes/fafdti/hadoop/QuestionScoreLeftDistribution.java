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
	public static final String DELIMITER = "\t";

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
		this.scoreLeftDistribution.readFields(in);
		this.question.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.scoreLeftDistribution.write(out);
		this.question.write(out);
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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((question == null) ? 0 : question.hashCode());
		result = prime
				* result
				+ ((scoreLeftDistribution == null) ? 0 : scoreLeftDistribution
						.hashCode());
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
		QuestionScoreLeftDistribution other = (QuestionScoreLeftDistribution) obj;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		if (scoreLeftDistribution == null) {
			if (other.scoreLeftDistribution != null)
				return false;
		} else if (!scoreLeftDistribution.equals(other.scoreLeftDistribution))
			return false;
		return true;
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
