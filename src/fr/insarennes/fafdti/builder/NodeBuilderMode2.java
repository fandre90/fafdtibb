package fr.insarennes.fafdti.builder;

import java.util.List;

import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;

public class NodeBuilderMode2 extends NodeBuilder{
	//root constructor
	public NodeBuilderMode2(DotNamesInfo featureSpec, 
			String inputDataPath,
			String workingDir,
			Criterion criterion,
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			StatBuilder stats,
			String baggingId) {
		super(featureSpec, inputDataPath, workingDir, criterion, nodeSetter, stopping, stats, baggingId);
	}
	//recursive constructor
	public NodeBuilderMode2(DotNamesInfo featureSpec, 
			String inputDataPath,
			String workingDir, 
			Criterion criterion, 
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			ParentInfos parentInfos, 
			ScoredDistributionVector parentDistribution,
			StatBuilder stats) {
		super(featureSpec, inputDataPath, workingDir, criterion, nodeSetter, stopping, parentInfos, parentDistribution, stats);
	}
	public void run(){
		
	}
}
