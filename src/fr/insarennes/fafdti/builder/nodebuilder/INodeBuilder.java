package fr.insarennes.fafdti.builder.nodebuilder;

import java.io.IOException;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;

public interface INodeBuilder {
	//Constructor : dotNames, criterion, workingdir
	public QuestionScoreLeftDistribution buildNode(String[][] data);
	public QuestionScoreLeftDistribution buildNode(Path dataPath, ScoredDistributionVector parentDistribution, Path workDir, String id) throws IOException, InterruptedException, ClassNotFoundException;
	public Pair<Path,Path> getSplitPath() throws IOException;
	public Pair<String[][],String[][]> getSplitData();
	public void cleanUp();
	public String getId();
}
