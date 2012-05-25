package fr.insarennes.fafdti.builder.treebuilder;

import java.util.List;

import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.nodebuilder.INodeBuilder;
import fr.insarennes.fafdti.builder.nodebuilder.INodeBuilderFactory;
import fr.insarennes.fafdti.builder.scheduler.IScheduler;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;

public class DumbTreeBuilderFactory implements ITreeBuilderFactory {

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilderFactory nodeBuilderFactory, String inputData,
			ParentInfos parentInfos,
			ScoredDistributionVector parentDistribution,
			ITreeBuilderFactory tbMaker, IScheduler scheduler) {
		return new TreeBuilderRecursive(featureSpec, workingDir, criterion,
				nodeSetter, stopping, stats, nodeBuilderFactory, inputData,
				parentInfos, parentDistribution, tbMaker, scheduler);
	}

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilderFactory nodeBuilderFactory, String[][] inputData,
			ParentInfos parentInfos,
			ScoredDistributionVector parentDistribution,
			ITreeBuilderFactory tbMaker, IScheduler scheduler) {
		return new TreeBuilderRecursive(featureSpec, workingDir, criterion,
				nodeSetter, stopping, stats, nodeBuilderFactory, inputData,
				parentInfos, parentDistribution, tbMaker, scheduler);
	}

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilderFactory nodeBuilderFactory, String baggingId,
			String inputDataPath, ITreeBuilderFactory tbMaker,
			IScheduler scheduler) {
		// TODO Auto-generated method stub
		return new TreeBuilderRecursive(featureSpec, workingDir, criterion,
				nodeSetter, stopping, stats, nodeBuilderFactory, baggingId,
				inputDataPath, tbMaker, scheduler);
	}

}
