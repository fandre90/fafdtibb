package fr.insarennes.fafdti.hadoop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.util.Hash;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class ValueDistributionMapAggregator {

	private WritableValueSDVSortedMap aggregatedMap;
	private int i;
	
	public ValueDistributionMapAggregator() {
		this.aggregatedMap = new WritableValueSDVSortedMap();
	}

	public void aggregate(WritableValueSDVSortedMap map)
			throws FAFException {
		for (Map.Entry<Value, ScoredDistributionVector> entry : map.entrySet()) {
			Value continuousValue = entry.getKey();
			ScoredDistributionVector partDistribution = entry.getValue();
			if (!aggregatedMap.containsKey(continuousValue)) {
				aggregatedMap.put(continuousValue,
						(ScoredDistributionVector) partDistribution.clone());
			} else {
				ScoredDistributionVector globDistribution = aggregatedMap
						.get(continuousValue);
				globDistribution.add(partDistribution);
			}
		}
	}

	public void aggregateAll(
			Iterable<WritableValueSDVSortedMap> mapSet)
			throws FAFException {
		i = 0;
		for (WritableValueSDVSortedMap map : mapSet) {
			aggregate(map);
			i++;
		}
	}

	public int getSize() {
		return this.aggregatedMap.size();
	}
	
	public int getNumberOfAggregatedMaps() {
		return i;
	}

	public WritableValueSDVSortedMap getAggregatedMap() {
		return aggregatedMap;
	}

}
