package fr.insarennes.fafdti;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;

public abstract class ReducerBase<K1, V1, K2, V2> 
	extends Reducer<K1, V1, K2, V2> {

	protected FeatureSpec fs;
	protected Criterion criterion;
	
	@Override
	protected void setup(Context context) throws IOException ,InterruptedException {
		Configuration conf = context.getConfiguration();
		try {
			fs = FeatureSpec.fromConf(conf);
			criterion = StockableCriterion.fromConf(conf);
		} catch (ClassNotFoundException e) {
			// FIXME Auto-generated catch block
			e.printStackTrace();
		}
	}
}
