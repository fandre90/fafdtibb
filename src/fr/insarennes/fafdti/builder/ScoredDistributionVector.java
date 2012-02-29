package fr.insarennes.fafdti.builder;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;


/**
 * stocke le vecteur statistique et l'entropie associée. 
 * attention: l'entropie n'est pas calculée automatiquement. 
 * le seul separateur utilisé dans la conversion en string est SEPARATEUR
 * @author Francois LEHERICEY
 */

public class ScoredDistributionVector extends HadoopConfStockable 
		implements Writable {
	/** entropie */
	private double score;
	/** vecteur statistique */
	private int[] distributionVector;
	private int total;
	/** sétarateur utilisé dans toString et fromString */
	public static final String SEPARATEUR = ":";
	public static final String HADOOP_CONFIGURATION_KEY = "faf-dist";

	/**
	 * construit un vecteur vide avec une entropie nulle
	 * @param size taille du vecteur statistique
	 */
	public ScoredDistributionVector(int size) {
		score = 0;
		distributionVector = new int[size];
		for (int i = 0; i < distributionVector.length; i++)
			distributionVector[i] = 0;
	}
	
	/**
	 * construit un vecteur statistique et son entropie associée a partir d'un string
	 * (issu d'un toString), l'entropie n'est pas calculé mais récupérée.
	 * @param s
	 */
	public ScoredDistributionVector(String s) {
		String splited[] = s.split(SEPARATEUR);
		distributionVector = new int[splited.length-1];
		try {
			fromString(s);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * +1 dans le vecteur stats a l'indice i, l'entropie n'est pas mise a jour.
	 * @param e l'indice du vecteur a modifier
	 */
	public void incrStat(int e){distributionVector[e]++;}

	/**
	 * rend l'entropie, attention si updateEntropie n'a pas été appelé la valeur sera fausse.
	 * @return
	 */
	public double getScore() {
		return score;
	}
	
	public int getTotal() {
		return total;
	}
	
	public int[] getDistributionVector() {
		return this.distributionVector;
	}

	public void rate(Criterion criterion) {
		this.score = criterion.compute(distributionVector);
	}

	public ScoredDistributionVector computeRightDistribution(
			ScoredDistributionVector leftDistribution) {
		int vectLen = this.distributionVector.length;
		ScoredDistributionVector rightDistribution = 
			new ScoredDistributionVector(vectLen);
		for(int i=0; i< vectLen; i++) {
			rightDistribution.distributionVector[i] =
					this.distributionVector[i] - 
					leftDistribution.distributionVector[i];
		}
		return rightDistribution;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		score = in.readDouble();
		int size = in.readInt();
		if(size != distributionVector.length) throw new IOException("taille des vecteurs incompatibles");
		for (int i = 0; i < size; i++) {
			distributionVector[i] = in.readInt();
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeDouble(score);
		out.writeInt(distributionVector.length);
		for (int i = 0; i < distributionVector.length; i++) {
			out.writeInt(distributionVector[i]);
		}
	}

	@Override
	public String toString() {
		String out = score + "";
		for (int i = 0; i < distributionVector.length - 1; i++) {
			out +=  SEPARATEUR + distributionVector[i];
		}
		
		return out;
	}

	/**
	 * charge dans l'instance courante le contenu de s (issue d'un toString).
	 * @param s valeur a charger
	 * @throws Exception 
	 */
	public void fromString(String s) throws Exception {
		int OFFSET = 1;
		String splited[] = s.split(SEPARATEUR);
		score = Float.parseFloat(splited[0]);
		if(splited.length-OFFSET != distributionVector.length)
			throw new Exception("taille des vecteur incompatibles");
		for (int i = OFFSET; i < splited.length; i++) {
			distributionVector[i-OFFSET] = Integer.parseInt(splited[i]);
		}
	}

	public static ScoredDistributionVector fromConf(Configuration conf,
			String keySuffix) {
		int OFFSET = 1;
		String strRepr = conf.get(HADOOP_CONFIGURATION_KEY + "-" + keySuffix);
		String splited[] = strRepr.split(SEPARATEUR);
		int vectorLen = splited.length - OFFSET;
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
}
