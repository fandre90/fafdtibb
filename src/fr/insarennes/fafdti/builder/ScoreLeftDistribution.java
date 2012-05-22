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

	public static double computeCombinedEntropy(ScoredDistributionVector leftDist,
			ScoredDistributionVector rightDist) {
		int nl = leftDist.getTotal();
		int nr = rightDist.getTotal();
		int N = nl + nr;
		//System.out.println(nl + "," + nr);
		return (nl * leftDist.getScore() + nr * rightDist.getScore())
				/ N;
	}

	public ScoreLeftDistribution(ScoredDistributionVector leftDist,
			ScoredDistributionVector rightDist) {
		double scoreValue = computeCombinedEntropy(leftDist, rightDist);
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((distribution == null) ? 0 : distribution.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
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
		ScoreLeftDistribution other = (ScoreLeftDistribution) obj;
		if (distribution == null) {
			if (other.distribution != null)
				return false;
		} else if (!distribution.equals(other.distribution))
			return false;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		return true;
	}
}
