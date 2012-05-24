package fr.insarennes.fafdti.builder.treebuilder;

import java.util.List;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.Util;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.nodebuilder.INodeBuilder;
import fr.insarennes.fafdti.builder.scheduler.IScheduler;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;

public class LimitModeTreeBuilderMaker implements ITreeBuilderMaker {

	private double sizeLimit;

	public LimitModeTreeBuilderMaker(double sizeLimit) {
		this.sizeLimit = sizeLimit;
	}
/*
	public TreeBuilderRecursive(DotNamesInfo featureSpec, 
						String workingDir, 
						Criterion criterion, 
						DecisionNodeSetter nodeSetter, 
						List<StoppingCriterion> stopping,
						StatBuilder stats,
						INodeBuilder nodeBuilder,
						String inputDataPath,
						ParentInfos parentInfos, 
						ScoredDistributionVector parentDistribution)
 */
	// Recursive Fat or Fast
	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilder nodeBuilder, String inputData,
			ParentInfos parentInfos, ScoredDistributionVector parentDistribution, ITreeBuilderMaker tbMaker, IScheduler scheduler) {
		double currentSize = Util.getSize(inputData);
		Runnable treeBuilder = null;
		if (currentSize > sizeLimit) {
			treeBuilder = new TreeBuilderRecursive(featureSpec, workingDir, criterion,
					nodeSetter, stopping, stats, nodeBuilder, inputData,
					parentInfos, parentDistribution, tbMaker, scheduler);
		} else {
			treeBuilder = new TreeBuildRunMapper();
		}
		return treeBuilder;
	}

	// Recursive Fast
	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilder nodeBuilder, String[][] inputData,
			ParentInfos parentInfos, ScoredDistributionVector parentDistribution, ITreeBuilderMaker tbMaker, IScheduler scheduler) {
		throw new UnsupportedOperationException(
				"This TreeBuilderMaker can only be used with input path");
	}

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilder nodeBuilder, String baggingId,
			String inputDataPath, ITreeBuilderMaker tbMaker, IScheduler scheduler) {
		double currentSize = Util.getSize(inputDataPath);
		Runnable treeBuilder = null;
		if (currentSize > sizeLimit) {
			treeBuilder = new TreeBuilderRecursive(featureSpec, workingDir, criterion,
					nodeSetter, stopping, stats, nodeBuilder, baggingId, inputDataPath, tbMaker, scheduler);
		} else {
			treeBuilder = new TreeBuildRunMapper();
		}
		return treeBuilder;
	}

}