package fr.insarennes.fafdti.hadoop.furious;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.hadoop.ReducerBase;
import fr.insarennes.fafdti.hadoop.ValueDistributionMapAggregator;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;

public class Step1Combiner
		extends
		ReducerBase<IntWritable, WritableValueSDVSortedMap, IntWritable, WritableValueSDVSortedMap> {

	protected void reduce(
			IntWritable col,
			Iterable<WritableValueSDVSortedMap> valueDistMaps,
			Context context) throws IOException, InterruptedException {
		try {
			ValueDistributionMapAggregator valueDistMapAgg = 
					new ValueDistributionMapAggregator();
			valueDistMapAgg.aggregateAll(valueDistMaps);
			context.write(col, valueDistMapAgg.getAggregatedMap());
		} catch (FAFException e) {
			e.printStackTrace();
		}
	}
}
