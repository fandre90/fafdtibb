package fr.insarennes.fafdti.hadoop;

import java.util.Map;

import org.apache.hadoop.io.DoubleWritable;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class ValueDistributionMapAggregator {

	private WritableValueSDVSortedMap aggregatedMap;

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
		for (WritableValueSDVSortedMap map : mapSet) {
			aggregate(map);
		}
	}

	public WritableValueSDVSortedMap getAggregatedMap() {
		return aggregatedMap;
	}

}
