package fr.insarennes.fafdti.builder;

import java.util.List;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;

public interface ITreeBuilderMaker {
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir,
			Criterion criterion,
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			StatBuilder stats,
			INodeBuilder nodeBuilder,
			String inputData,
			ParentInfos parentInfos, 
			ScoredDistributionVector parentDistribution, 
			ITreeBuilderMaker tbMaker,
			IScheduler scheduler);
	
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir,
			Criterion criterion,
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			StatBuilder stats,
			INodeBuilder nodeBuilder,
			String[][] inputData,
			ParentInfos parentInfos, 
			ScoredDistributionVector parentDistribution,
			ITreeBuilderMaker tbMaker,
			IScheduler scheduler);
	
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec, 
			String workingDir,
			Criterion criterion,
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			StatBuilder stats,
			INodeBuilder nodeBuilder,
			String baggingId,
			String inputDataPath, ITreeBuilderMaker tbMaker,
			IScheduler scheduler);
}
