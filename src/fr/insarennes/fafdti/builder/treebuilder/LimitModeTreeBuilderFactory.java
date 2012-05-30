package fr.insarennes.fafdti.builder.treebuilder;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FSUtils;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.nodebuilder.INodeBuilder;
import fr.insarennes.fafdti.builder.nodebuilder.INodeBuilderFactory;
import fr.insarennes.fafdti.builder.scheduler.IScheduler;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.cli.FAFBuildMode;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;

public class LimitModeTreeBuilderFactory implements ITreeBuilderFactory {

	private long sizeLimit;
	private static Logger log = Logger.getLogger(LimitModeTreeBuilderFactory.class);
	private FSUtils fsUtils;
	
	public LimitModeTreeBuilderFactory(long sizeLimit) throws IOException {
		this.sizeLimit = sizeLimit;
		this.fsUtils = new FSUtils();
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
			StatBuilder stats, INodeBuilderFactory nodeBuilderFactory, String inputData,
			ParentInfos parentInfos, ScoredDistributionVector parentDistribution, ITreeBuilderFactory tbMaker, IScheduler scheduler) throws IOException {
		long currentSize = fsUtils.getSize(new Path(inputData));
		Runnable treeBuilder = null;
		if (currentSize > sizeLimit) {
			treeBuilder = new TreeBuilderRecursive(featureSpec, workingDir, criterion,
					nodeSetter, stopping, stats, nodeBuilderFactory, inputData,
					parentInfos, parentDistribution, tbMaker, scheduler);
		} else {
			log.info("Switching to fast mode (Input file size: " + currentSize + " bytes )");
			treeBuilder = new TreeBuildRunMapper(featureSpec, workingDir, 
					criterion, nodeSetter, stopping, stats, inputData, parentInfos, parentDistribution);
		}
		return treeBuilder;
	}

	// Recursive Fast
	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilderFactory nodeBuilderFactory, String[][] inputData,
			ParentInfos parentInfos, ScoredDistributionVector parentDistribution, ITreeBuilderFactory tbMaker, IScheduler scheduler) {
		throw new UnsupportedOperationException(
				"This TreeBuilderMaker can only be used with input path");
	}

	@Override
	public Runnable makeTreeBuilder(DotNamesInfo featureSpec,
			String workingDir, Criterion criterion,
			DecisionNodeSetter nodeSetter, List<StoppingCriterion> stopping,
			StatBuilder stats, INodeBuilderFactory nodeBuilderFactory, String baggingId,
			String inputDataPath, ITreeBuilderFactory tbMaker, IScheduler scheduler) throws IOException {
		double currentSize = fsUtils.getSize(new Path(inputDataPath));
		Runnable treeBuilder = null;
		if (currentSize > sizeLimit) {
			treeBuilder = new TreeBuilderRecursive(featureSpec, workingDir, criterion,
					nodeSetter, stopping, stats, nodeBuilderFactory, baggingId, inputDataPath, tbMaker, scheduler);
		} else {
			log.info("Switching to fast mode for root");
			treeBuilder = new TreeBuildRunMapper(featureSpec, workingDir, 
					criterion, nodeSetter, stopping, stats, inputDataPath, baggingId);
		}
		return treeBuilder;
	}

}
