package fr.insarennes.fafdti.hadoop.furious;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.hadoop.ReducerBase;
import fr.insarennes.fafdti.hadoop.ValueDistributionMapAggregator;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;

public class Step1Combiner
		extends
		ReducerBase<IntWritable, WritableValueSDVSortedMap, IntWritable, WritableValueSDVSortedMap> {
	private Logger log = Logger.getLogger("COMB: ");
	private Runtime run = Runtime.getRuntime();

	protected void reduce(
			IntWritable col,
			Iterable<WritableValueSDVSortedMap> valueDistMaps,
			Context context) throws IOException, InterruptedException {
		try {
			ValueDistributionMapAggregator valueDistMapAgg = 
					new ValueDistributionMapAggregator();
			valueDistMapAgg.aggregateAll(valueDistMaps);
			/*
			log.info("Attr: " + col.get() + " - Count / Size : " + 
					valueDistMapAgg.getNumberOfAggregatedMaps() + " / " +
					valueDistMapAgg.getSize());
			log.info("Used / Total: " + ((run.totalMemory() - run.freeMemory()) / (1024 * 1024)) + " / " +
					(run.totalMemory() / (1024 * 1024)));
			*/
			//System.gc();
			context.write(col, valueDistMapAgg.getAggregatedMap());
		} catch (FAFException e) {
			e.printStackTrace();
		}
	}
}
