package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.TooManyRelaunchException;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;

public interface INodeBuilder {

	public QuestionScoreLeftDistribution buildNode() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException;

	public ScoredDistributionVector getDistribution() throws IOException,
			InterruptedException, ClassNotFoundException, FAFException;

	public Pair<Path, Path> getSplitPath() throws IOException, TooManyRelaunchException;

	public Pair<String[][], String[][]> getSplitData() throws FAFException;

	public void cleanUp();

	public String getId();
}
