package fr.insarennes.fafdti.hadoop;

public interface IFactory<T> {
	public T newInstance();
}
