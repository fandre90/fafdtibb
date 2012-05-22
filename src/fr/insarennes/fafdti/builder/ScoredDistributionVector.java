package fr.insarennes.fafdti.builder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;

import fr.insarennes.fafdti.FAFException;

/**
 * stocke le vecteur statistique et l'entropie associée. attention: l'entropie
 * n'est pas calculée automatiquement. le seul separateur utilisé dans la
 * conversion en string est SEPARATEUR
 * 
 * @author Francois LEHERICEY
 */

public class ScoredDistributionVector extends HadoopConfStockable implements
		Writable, Cloneable {
	/** entropie */
	private double score;
	/** vecteur statistique */
	private int[] distributionVector;
	private int total;
	private boolean hasMoreThanOneNonEmptyLabel = false;
	/** sétarateur utilisé dans toString et fromString */
	public static final String SEPARATEUR = ":";
	public static final String HADOOP_CONFIGURATION_KEY = "faf-dist";

	public ScoredDistributionVector() {
		this(1);
	}

	/**
	 * construit un vecteur vide avec une entropie nulle
	 * 
	 * @param size
	 *            taille du vecteur statistique
	 */
	public ScoredDistributionVector(int size) {
		score = 0;
		distributionVector = new int[size];
		hasMoreThanOneNonEmptyLabel = false;
	}

	/**
	 * construit un vecteur statistique et son entropie associée a partir d'un
	 * string (issu d'un toString), l'entropie n'est pas calculé mais récupérée.
	 * 
	 * @param s
	 */
	public ScoredDistributionVector(String s) {
		String splited[] = s.split(SEPARATEUR);
		distributionVector = new int[splited.length - 1];
		try {
			fromString(s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * +1 dans le vecteur stats a l'indice i, l'entropie n'est pas mise a jour.
	 * 
	 * @param e
	 *            l'indice du vecteur a modifier
	 */
	public void incrStat(int i) {
		distributionVector[i]++;
		total++;
		hasMoreThanOneNonEmptyLabel = (total != distributionVector[i]);
	}

	/**
	 * rend l'entropie, attention si updateEntropie n'a pas été appelé la valeur
	 * sera fausse.
	 * 
	 * @return
	 */
	public double getScore() {
		return score;
	}

	public int getTotal() {
		return total;
	}
	
	public boolean isPure() {
		return !hasMoreThanOneNonEmptyLabel;
	}

	public boolean hasMoreThanOneNonEmptyLabel() {
		return hasMoreThanOneNonEmptyLabel;
	}

	public int[] getDistributionVector() {
		return this.distributionVector;
	}

	public void rate(Criterion criterion) {
		this.score = criterion.compute(distributionVector);
	}

	public void add(ScoredDistributionVector otherSDV) throws FAFException {
		if(otherSDV.distributionVector.length != this.distributionVector.length) {
			throw new FAFException("Distribution vectors must have the same length");
		}
		for(int i=0; i<this.distributionVector.length; i++) {
			this.distributionVector[i] += otherSDV.distributionVector[i];
		}
		this.total += otherSDV.total;
	}

	public ScoredDistributionVector computeRightDistribution(
			ScoredDistributionVector leftDistribution) {
		int vectLen = this.distributionVector.length;
		ScoredDistributionVector rightDist = new ScoredDistributionVector(
				vectLen);
		for (int i = 0; i < vectLen; i++) {
			int n = this.distributionVector[i]
					- leftDistribution.distributionVector[i];
			rightDist.distributionVector[i] = n;
			rightDist.total += n;
			rightDist.hasMoreThanOneNonEmptyLabel = (rightDist.total != n)
					&& (n != 0);
		}
		return rightDist;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		score = in.readDouble();
		int size = in.readInt();
		//System.out.println("Read: " + score + "," + size);
		if (size != distributionVector.length) {
			distributionVector = new int[size];
		}
		total = 0;
		hasMoreThanOneNonEmptyLabel = false;
		for (int i = 0; i < size; i++) {
			distributionVector[i] = in.readInt();
			total += distributionVector[i];
			hasMoreThanOneNonEmptyLabel = (total != distributionVector[i])
					&& (distributionVector[i] != 0);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(score);
		out.writeInt(distributionVector.length);
		//System.out.println("Write: " + score + "," + distributionVector.length);
		for (int i = 0; i < distributionVector.length; i++) {
			out.writeInt(distributionVector[i]);
		}
	}

	@Override
	public Object clone() {
		ScoredDistributionVector scoredDistVect = null;
		try {
			scoredDistVect = (ScoredDistributionVector) super.clone();
		} catch (CloneNotSupportedException cnse) {
			cnse.printStackTrace();
		}
		scoredDistVect.distributionVector = this.distributionVector.clone();
		return scoredDistVect;
	}

	@Override
	public String toString() {
		String out = score + "";
		for (int i = 0; i < distributionVector.length; i++) {
			out += SEPARATEUR + distributionVector[i];
		}

		return out;
	}

	/**
	 * charge dans l'instance courante le contenu de s (issue d'un toString).
	 * 
	 * @param s
	 *            valeur a charger
	 * @throws Exception
	 */
	public void fromString(String s) {
		int OFFSET = 1;
		String splited[] = s.split(SEPARATEUR);
		score = Float.parseFloat(splited[0]);
		int size = splited.length - OFFSET;
		if (size != distributionVector.length) {
			distributionVector = new int[size];
		}
		total = 0;
		hasMoreThanOneNonEmptyLabel = false;
		for (int i = OFFSET; i < splited.length; i++) {
			int n = Integer.parseInt(splited[i]);
			distributionVector[i - OFFSET] = n;
			total += n;
			hasMoreThanOneNonEmptyLabel = (total != n) && (n != 0);
		}
	}

	public static ScoredDistributionVector fromConf(Configuration conf,
			String keySuffix) {
		String strRepr = conf.get(HADOOP_CONFIGURATION_KEY + "-" + keySuffix);
		ScoredDistributionVector dist = new ScoredDistributionVector(strRepr);
		return dist;
	}

	public static ScoredDistributionVector fromConf(Configuration conf) {
		return fromConf(conf, "");
	}

	@Override
	public void toConf(Configuration conf, String keySuffix) {
		String strRepr = this.toString();
		conf.set(HADOOP_CONFIGURATION_KEY + "-" + keySuffix, strRepr);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(distributionVector);
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ScoredDistributionVector other = (ScoredDistributionVector) obj;
		if (!Arrays.equals(distributionVector, other.distributionVector))
			return false;
		if (Double.doubleToLongBits(score) != Double
				.doubleToLongBits(other.score))
			return false;
		return true;
	}

}
