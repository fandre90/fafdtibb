package fr.insarennes.fafdti.builder;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

public abstract class Criterion extends HadoopConfStockable {
	public static final String HADOOP_CONFIGURATION_KEY = "faf-criterion";
	/**
	 * Returns true if value1 is a better value than value 2 for this criterion
	 */
	public abstract boolean better(double value1, double value2);

	/**
	 * Compute the value of the criterion for the given distribution
	 * vector.
	 * @param distributionVector The distribution vector for which
	 * we want to compute the criterion
	 * @return the value of the criterion for the given distribution
	 * vector
	 */
	public abstract double compute(int[] distributionVector);
	
	public static Criterion fromConf(Configuration conf, String keySuffix)
			throws IOException, ClassNotFoundException {
		String key = HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		return (Criterion) HadoopConfSerializer
				.deserializeFromConf(conf, key);
	}

	public static Criterion fromConf(Configuration conf) throws IOException,
			ClassNotFoundException {
		return fromConf(conf, "");
	}
	
	public void toConf(Configuration conf, String keySuffix) throws IOException {
		String key = HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		HadoopConfSerializer.serializeToConf(this, conf, key);
	}
}
