package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;

import fr.insarennes.fafdti.FAFException;

public class NewStep2Combiner
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
