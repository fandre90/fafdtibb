package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;

import fr.insarennes.fafdti.FAFException;

public class NewStep2Combiner
		extends
		ReducerBase<IntWritable, WritableDoubleScoredDistributionVectorSortedMap, IntWritable, WritableDoubleScoredDistributionVectorSortedMap> {

	protected void reduce(
			IntWritable col,
			Iterable<WritableDoubleScoredDistributionVectorSortedMap> valueDistMaps,
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
