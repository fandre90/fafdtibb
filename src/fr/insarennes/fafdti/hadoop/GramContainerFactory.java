package fr.insarennes.fafdti.hadoop;

import fr.insarennes.fafdti.builder.gram.GramContainer;

public class GramContainerFactory implements IFactory<GramContainer>{

	@Override
	public GramContainer newInstance() {
		return new GramContainer();
	}

}
