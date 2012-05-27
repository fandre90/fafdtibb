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

public class WritableValueSDVSortedMap implements
		Cloneable, Writable, SortedMap<Value, ScoredDistributionVector> {

	private SortedMap<Value, ScoredDistributionVector> map;
	

	public WritableValueSDVSortedMap() {
		this.map = new TreeMap<Value, ScoredDistributionVector>();
	}

	public WritableValueSDVSortedMap(
			SortedMap<Value, ScoredDistributionVector> sortedMap) {
		this.map = new TreeMap<Value, ScoredDistributionVector>(sortedMap);
	}

	public WritableValueSDVSortedMap(
			Map<Value, ScoredDistributionVector> map) {
		this.map = new TreeMap<Value, ScoredDistributionVector>(map);
	}

	private WritableValueSDVSortedMap makeWritableWithReference(
			SortedMap<Value, ScoredDistributionVector> sortedMap) {
		WritableValueSDVSortedMap wrMap = 
				new WritableValueSDVSortedMap();
		wrMap.map = sortedMap;
		return wrMap;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		map.clear();
		while (size-- != 0) {
			Value key = new Value(); // Yes the key is a value :)
									 // and the value is its distribution
			key.readFields(in);
			ScoredDistributionVector value = new ScoredDistributionVector();
			value.readFields(in);
			map.put(key, value);
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(map.size());
		for (Map.Entry<Value, ScoredDistributionVector> entry : map.entrySet()) {
			entry.getKey().write(out);
			entry.getValue().write(out);
		}
	}

	@Override
	public Object clone() {
	    WritableValueSDVSortedMap wrMap = null;
	    try {
	    	wrMap = (WritableValueSDVSortedMap) 
	    			super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	cnse.printStackTrace(System.err);
	    }
	    wrMap.map = new TreeMap<Value, ScoredDistributionVector>(this.map);
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
	public ScoredDistributionVector put(Value key,
			ScoredDistributionVector value) {
		return map.put(key, value);
	}

	@Override
	public void putAll(
			Map<? extends Value, ? extends ScoredDistributionVector> map) {
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
	public Comparator<? super Value> comparator() {
		return map.comparator();
	}

	@Override
	public Set<java.util.Map.Entry<Value, ScoredDistributionVector>> entrySet() {
		return map.entrySet();
	}

	@Override
	public Value firstKey() {
		return map.firstKey();
	}

	@Override
	public SortedMap<Value, ScoredDistributionVector> headMap(Value toKey) {
		return makeWritableWithReference(this.map.headMap(toKey));
	}

	@Override
	public Set<Value> keySet() {
		return map.keySet();
	}

	@Override
	public Value lastKey() {
		return map.lastKey();
	}

	@Override
	public SortedMap<Value, ScoredDistributionVector> subMap(Value fromKey,
			Value toKey) {
		return makeWritableWithReference(this.map.subMap(fromKey, toKey));
	}

	@Override
	public SortedMap<Value, ScoredDistributionVector> tailMap(Value fromKey) {
		return makeWritableWithReference(this.map.tailMap(fromKey));
	}

	@Override
	public Collection<ScoredDistributionVector> values() {
		return map.values();
	}

}
