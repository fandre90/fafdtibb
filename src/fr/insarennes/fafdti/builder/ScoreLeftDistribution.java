package fr.insarennes.fafdti.builder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;


public class ScoreLeftDistribution implements Writable, Cloneable {
	private DoubleWritable score;
	private ScoredDistributionVector distribution;
	public static final String DELIMITER = " ";

	public ScoreLeftDistribution() {
		super();
		this.score = new DoubleWritable();
		this.distribution = new ScoredDistributionVector();
	}
	// FIXME : Toujours la même manière d'agréger l'entropie
	
	public ScoreLeftDistribution(String strRepr) {
		this();
		fromString(strRepr);
	}

	public ScoreLeftDistribution(ScoredDistributionVector leftDist,
			ScoredDistributionVector rightDist) {
		int nl = leftDist.getTotal();
		int nr = rightDist.getTotal();
		int N = nl + nr;
		//System.out.println(nl + "," + nr);
		double scoreValue = (nl * leftDist.getScore() + nr * rightDist.getScore())
				/ N;
		this.score = new DoubleWritable(scoreValue);
		this.distribution = (ScoredDistributionVector) leftDist.clone();
	}

	public ScoreLeftDistribution(double score,
			ScoredDistributionVector distribution) {
		this();
		this.score.set(score);
		this.distribution = distribution;
	}

	public double getScore() {
		return score.get();
	}

	public ScoredDistributionVector getDistribution() {
		return distribution;
	}

	public Object clone() {
		ScoreLeftDistribution scoreLeftDist = null;
		try {
			scoreLeftDist = (ScoreLeftDistribution) super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace();
		}
		scoreLeftDist.score = new DoubleWritable(this.score.get());
		scoreLeftDist.distribution = (ScoredDistributionVector) 
				this.distribution.clone();
		return scoreLeftDist;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.score.readFields(in);
		this.distribution.readFields(in);
	}

	@Override
	public void write(DataOutput out) throws IOException {
		this.score.write(out);
		this.distribution.write(out);
	}
	
	@Override
	public String toString() {
		String strRepr = this.score.toString();
		strRepr += DELIMITER;
		strRepr += this.distribution.toString();
		return strRepr;
	}
	
	public void fromString(String strRepr) {
		String[] fields = strRepr.split(DELIMITER, 2);
		double scoreValue = Double.parseDouble(fields[0]);
		this.score.set(scoreValue);
		this.distribution.fromString(fields[1]);
	}
}
