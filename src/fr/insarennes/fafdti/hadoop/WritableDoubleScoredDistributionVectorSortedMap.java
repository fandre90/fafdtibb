package fr.insarennes.fafdti.hadoop;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.hadoop.io.Writable;

import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class WritableDoubleScoredDistributionVectorSortedMap implements
		Cloneable, Writable, SortedMap<Double, ScoredDistributionVector> {

	private SortedMap<Double, ScoredDistributionVector> map;

	public WritableDoubleScoredDistributionVectorSortedMap() {
		this.map = new TreeMap<Double, ScoredDistributionVector>();
	}

	public WritableDoubleScoredDistributionVectorSortedMap(
			SortedMap<Double, ScoredDistributionVector> sortedMap) {
		this.map = new TreeMap<Double, ScoredDistributionVector>(sortedMap);
	}

	public WritableDoubleScoredDistributionVectorSortedMap(
			Map<Double, ScoredDistributionVector> map) {
		this.map = new TreeMap<Double, ScoredDistributionVector>(map);
	}

	private WritableDoubleScoredDistributionVectorSortedMap makeWritableWithReference(
			SortedMap<Double, ScoredDistributionVector> sortedMap) {
		WritableDoubleScoredDistributionVectorSortedMap wrMap = 
				new WritableDoubleScoredDistributionVectorSortedMap();
		wrMap.map = sortedMap;
		return wrMap;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		map.clear();
		while (size-- != 0) {
			double key = in.readDouble();
			ScoredDistributionVector value = new ScoredDistributionVector();
			value.readFields(in);
			map.put(key, value);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(map.size());
		for (Map.Entry<Double, ScoredDistributionVector> entry : map.entrySet()) {
			out.writeDouble(entry.getKey());
			entry.getValue().write(out);
		}
	}

	@Override
	public Object clone() {
	    WritableDoubleScoredDistributionVectorSortedMap wrMap = null;
	    try {
	    	wrMap = (WritableDoubleScoredDistributionVectorSortedMap) 
	    			super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	cnse.printStackTrace(System.err);
	    }
	    wrMap.map = new TreeMap<Double, ScoredDistributionVector>(this.map);
	    return wrMap;
	}

	@Override
	public void clear() {
		map.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public ScoredDistributionVector get(Object key) {
		return map.get(key);
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public ScoredDistributionVector put(Double key,
			ScoredDistributionVector value) {
		return map.put(key, value);
	}

	@Override
	public void putAll(
			Map<? extends Double, ? extends ScoredDistributionVector> map) {
		this.map.putAll(map);
	}

	@Override
	public ScoredDistributionVector remove(Object key) {
		return map.remove(key);
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public Comparator<? super Double> comparator() {
		return map.comparator();
	}

	@Override
	public Set<java.util.Map.Entry<Double, ScoredDistributionVector>> entrySet() {
		return map.entrySet();
	}

	@Override
	public Double firstKey() {
		return map.firstKey();
	}

	@Override
	public SortedMap<Double, ScoredDistributionVector> headMap(Double toKey) {
		return makeWritableWithReference(this.map.headMap(toKey));
	}

	@Override
	public Set<Double> keySet() {
		return map.keySet();
	}

	@Override
	public Double lastKey() {
		return map.lastKey();
	}

	@Override
	public SortedMap<Double, ScoredDistributionVector> subMap(Double fromKey,
			Double toKey) {
		return makeWritableWithReference(this.map.subMap(fromKey, toKey));
	}

	@Override
	public SortedMap<Double, ScoredDistributionVector> tailMap(Double fromKey) {
		return makeWritableWithReference(this.map.tailMap(fromKey));
	}

	@Override
	public Collection<ScoredDistributionVector> values() {
		return map.values();
	}

}
