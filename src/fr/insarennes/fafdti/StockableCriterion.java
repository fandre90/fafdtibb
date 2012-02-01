package fr.insarennes.fafdti;

import org.apache.hadoop.conf.Configuration;

public class StockableCriterion extends HadoopConfStockable implements
		Criterion {
	
	private Criterion criterion;
	public static final String HADOOP_CONFIGURATION_KEY = "faf-criterion";
	
	public StockableCriterion(Criterion criterion) {
		this.criterion = criterion;
	}

	@Override
	public boolean better(double value1, double value2) {
		return this.criterion.better(value1, value2);
	}

	@Override
	public double compute(int[] distributionVector) {
		return this.criterion.compute(distributionVector);
	}

	@Override
	public void toConf(Configuration conf, String keySuffix) {
		CriterionFactory factory = CriterionFactory.INSTANCE;
		conf.set(HADOOP_CONFIGURATION_KEY + "-" + keySuffix, 
				factory.getCodeLetter(this.getClass()));
	}
	
	public static StockableCriterion fromConf(Configuration conf, String keySuffix) {
		CriterionFactory factory = CriterionFactory.INSTANCE;
		String codeLetter = conf.get(HADOOP_CONFIGURATION_KEY + "-" + keySuffix);
		return new StockableCriterion(factory.makeCriterion(codeLetter));
	}
	
	public static StockableCriterion fromConf(Configuration conf) {
		return fromConf(conf);
	}

}
