package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.TooManyRelaunchException;

public interface INodeBuilderFactory {
	public INodeBuilder makeNodeBuilder(String[][] database, ScoredDistributionVector parentDistribution,
			String id) throws FAFException;
	public INodeBuilder makeNodeBuilder(Path dataPath,
			ScoredDistributionVector parentDistribution, String id, Path workdir) throws IOException, InterruptedException, ClassNotFoundException, TooManyRelaunchException;
}
