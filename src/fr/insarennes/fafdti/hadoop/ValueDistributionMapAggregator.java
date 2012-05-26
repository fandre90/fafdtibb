package fr.insarennes.fafdti.hadoop;

import java.util.Map;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class ValueDistributionMapAggregator {

	private WritableDoubleScoredDistributionVectorSortedMap aggregatedMap;

	public ValueDistributionMapAggregator() {
		this.aggregatedMap = new WritableDoubleScoredDistributionVectorSortedMap();
	}

	public void aggregate(WritableDoubleScoredDistributionVectorSortedMap map)
			throws FAFException {
		for (Map.Entry<Double, ScoredDistributionVector> entry : map.entrySet()) {
			double continuousValue = entry.getKey();
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
			Iterable<WritableDoubleScoredDistributionVectorSortedMap> mapSet)
			throws FAFException {
		for (WritableDoubleScoredDistributionVectorSortedMap map : mapSet) {
			aggregate(map);
		}
	}

	public WritableDoubleScoredDistributionVectorSortedMap getAggregatedMap() {
		return aggregatedMap;
	}

}
