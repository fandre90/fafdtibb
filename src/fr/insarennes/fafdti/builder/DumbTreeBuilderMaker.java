package fr.insarennes.fafdti.builder;

import java.util.List;

import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;

public class DumbTreeBuilderMaker implements ITreeBuilderMaker {

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilder nodeBuilder, String inputData,
			ParentInfos parentInfos,
			ScoredDistributionVector parentDistribution,
			ITreeBuilderMaker tbMaker, IScheduler scheduler) {
		return new TreeBuilderRecursive(featureSpec, workingDir, criterion,
				nodeSetter, stopping, stats, nodeBuilder, inputData,
				parentInfos, parentDistribution, tbMaker, scheduler);
	}

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilder nodeBuilder, String[][] inputData,
			ParentInfos parentInfos,
			ScoredDistributionVector parentDistribution,
			ITreeBuilderMaker tbMaker, IScheduler scheduler) {
		return new TreeBuilderRecursive(featureSpec, workingDir, criterion,
				nodeSetter, stopping, stats, nodeBuilder, inputData,
				parentInfos, parentDistribution, tbMaker, scheduler);
	}

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilder nodeBuilder, String baggingId,
			String inputDataPath, ITreeBuilderMaker tbMaker,
			IScheduler scheduler) {
		// TODO Auto-generated method stub
		return new TreeBuilderRecursive(featureSpec, workingDir, criterion,
				nodeSetter, stopping, stats, nodeBuilder, baggingId,
				inputDataPath, tbMaker, scheduler);
	}

}
