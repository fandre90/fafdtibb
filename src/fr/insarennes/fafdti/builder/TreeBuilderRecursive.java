package fr.insarennes.fafdti.builder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Pair;
import fr.insarennes.fafdti.Util;
import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StopCriterionUtils;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.cli.FAFExitCode;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.tree.CannotOverwriteTreeException;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;
import fr.insarennes.fafdti.tree.LinkedDecisionTreeQuestion;

public class TreeBuilderRecursive implements Runnable, StopCriterionUtils {
	protected static Logger log = Logger.getLogger(TreeBuilderRecursive.class);
	protected Path inputDataPath;
	protected String[][] inputData;
	protected Path workingDir;
	protected DotNamesInfo featureSpec;
	protected Criterion criterion;
	protected DecisionNodeSetter nodeSetter;
	protected List<StoppingCriterion> stopping;
	protected ParentInfos parentInfos;
	protected ScoredDistributionVector parentDistribution;
	protected QuestionScoreLeftDistribution qLeftDistribution;
	protected ScoredDistributionVector rightDistribution;
	protected StatBuilder stats;
	protected INodeBuilder nodeBuilder;
	protected BuildMode buildMode;
	protected ITreeBuilderMaker tbMaker;
	protected IScheduler scheduler;
	
	// factorizer constructor
	private TreeBuilderRecursive(DotNamesInfo featureSpec, String workingDir,
			Criterion criterion, DecisionNodeSetter nodeSetter,
			List<StoppingCriterion> stopping, StatBuilder stats,
			INodeBuilder nodeBuilder, ITreeBuilderMaker tbMaker, 
			IScheduler scheduler) {
		this.featureSpec = featureSpec;
		this.workingDir = new Path(workingDir);
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.stats = stats;
		this.nodeBuilder = nodeBuilder;
		this.tbMaker = tbMaker;
		this.scheduler = scheduler;
	}

	// root constructor
	public TreeBuilderRecursive(DotNamesInfo featureSpec, String workingDir,
			Criterion criterion, DecisionNodeSetter nodeSetter,
			List<StoppingCriterion> stopping, StatBuilder stats,
			INodeBuilder nodeBuilder, String baggingId, String inputDataPath,
			ITreeBuilderMaker tbMaker, IScheduler scheduler) {
		this(featureSpec, workingDir, criterion, nodeSetter, stopping, stats,
				nodeBuilder, tbMaker, scheduler);
		this.parentInfos = new ParentInfos(0, "launcher", baggingId);

	}

	// recursive constructor
	public TreeBuilderRecursive(DotNamesInfo featureSpec, String workingDir,
			Criterion criterion, DecisionNodeSetter nodeSetter,
			List<StoppingCriterion> stopping, StatBuilder stats,
			INodeBuilder nodeBuilder, String inputDataPath,
			ParentInfos parentInfos,
			ScoredDistributionVector parentDistribution,
			ITreeBuilderMaker tbMaker,
			IScheduler scheduler) {
		this(featureSpec, workingDir, criterion, nodeSetter, stopping, stats,
				nodeBuilder, tbMaker, scheduler);
		this.inputDataPath = new Path(inputDataPath);
		this.parentInfos = parentInfos;
		this.parentDistribution = parentDistribution;
		this.buildMode = BuildMode.MODEFAT;
	}

	public TreeBuilderRecursive(DotNamesInfo featureSpec, String workingDir,
			Criterion criterion, DecisionNodeSetter nodeSetter,
			List<StoppingCriterion> stopping, StatBuilder stats,
			INodeBuilder nodeBuilder, String[][] inputData,
			ParentInfos parentInfos,
			ScoredDistributionVector parentDistribution,
			ITreeBuilderMaker tbMaker, IScheduler scheduler) {
		this(featureSpec, workingDir, criterion, nodeSetter, stopping, stats,
				nodeBuilder, tbMaker, scheduler);
		this.inputData = inputData;
		this.parentInfos = parentInfos;
		this.parentDistribution = parentDistribution;
		this.buildMode = BuildMode.MODEFAST;
	}

	public void run() {
		try {
			String id = parentInfos.getBaggingId() + "-"
					+ Integer.toString(stats.getNextId());
			Path wd = new Path(this.workingDir, id);
			QuestionScoreLeftDistribution qLeftDistribution = null;
			if (buildMode == BuildMode.MODEFAT)
				qLeftDistribution = this.nodeBuilder.buildNode(
						this.inputDataPath, parentDistribution, wd, id);
			else
				qLeftDistribution = this.nodeBuilder.buildNode(this.inputData);
			// compute right distribution from left one
			rightDistribution = parentDistribution
					.computeRightDistribution(qLeftDistribution
							.getScoreLeftDistribution().getDistribution());
			rightDistribution.rate(criterion);
			if (this.mustStop()) {
				leafMaker();
			} else
				nodeMaker();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// *******************Construction methods************//

	private void nodeMaker() {
		log.log(Level.INFO, "Making a question node...");
		// +2(2 sons)-1(current node) = 1
		// on le fait avant d'appeler set(dtq) sur le nodeSetter
		stats.incrementPending();
		// construction du noeud
		Question question = qLeftDistribution.getQuestion();
		LinkedDecisionTreeQuestion dtq = new LinkedDecisionTreeQuestion(
				question, nodeBuilder);
		try {
			nodeSetter.set(dtq);
		} catch (CannotOverwriteTreeException e) {
			log.log(Level.ERROR,
					"NodeBuilder tries to overwrite a DecisionTree through NodeSetter with Question node");
			log.log(Level.ERROR, e.getMessage());
		}

		try {
			Pair<Path, Path> datapaths = nodeBuilder.getSplitPath();
			Runnable treeBuilderLeft;
			Runnable treeBuilderRight;
			ParentInfos pInfos = new ParentInfos(parentInfos.getDepth() + 1, nodeBuilder.getId(),
					 parentInfos.getBaggingId());
			if (datapaths == null) {
				Pair<String[][], String[][]> datas = nodeBuilder.getSplitData();
				treeBuilderLeft = tbMaker
						.makeTreeBuilder(featureSpec, workingDir.toString(), criterion,
								dtq.yesSetter(), stopping, stats, nodeBuilder,
								datas.getFirst(), pInfos, parentDistribution, null, null);
				treeBuilderRight = tbMaker
						.makeTreeBuilder(featureSpec, workingDir.toString(), criterion,
								dtq.noSetter(), stopping, stats, nodeBuilder,
								datas.getSecond(), pInfos, parentDistribution, null, null);
			} else {
				treeBuilderLeft = tbMaker
						.makeTreeBuilder(featureSpec, workingDir.toString(), criterion,
								dtq.yesSetter(), stopping, stats, nodeBuilder,
								datapaths.getFirst().toString(), pInfos, parentDistribution, null, null);
				treeBuilderRight = tbMaker
						.makeTreeBuilder(featureSpec, workingDir.toString(), criterion,
								dtq.noSetter(), stopping, stats, nodeBuilder,
								datapaths.getSecond().toString(), pInfos, parentDistribution, null, null);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void leafMaker() {
		log.log(Level.INFO, "Making a distribution leaf...");
		// construction de la feuille
		int[] distr = parentDistribution.getDistributionVector();
		int sum = parentDistribution.getTotal();
		log.log(Level.DEBUG, "sum=" + sum);
		Map<String, Double> map = new HashMap<String, Double>();
		for (int i = 0; i < distr.length; i++) {
			log.log(Level.DEBUG, "distr[i]=" + distr[i]);
			Double distri = new Double((double) distr[i] / (double) sum);
			if (distri.doubleValue() > 0.0) {
				String label = featureSpec.getLabels()[i];
				map.put(label, distri);
			}
		}
		DecisionTreeLeaf dtl = null;
		try {
			dtl = new DecisionTreeLeaf(new LeafLabels(map), sum);
		} catch (InvalidProbabilityComputationException e) {
			log.log(Level.ERROR, e.getMessage());
			System.out.println(parentDistribution.toString());
			System.exit(FAFExitCode.EXIT_ERROR);
		}
		try {
			nodeSetter.set(dtl);
		} catch (CannotOverwriteTreeException e) {
			log.log(Level.ERROR,
					"NodeBuilder tries to overwrite a DecisionTree through NodeSetter with Distribution leaf");
		}
		try {
			stats.addExClassified(sum);
		} catch (FAFException e) {
			log.error("Cannot add stats of examples classified because total has not been set");
		}
		// un pending en moins
		stats.decrementPending();
	}

	// *****************stopping utils methods************//

	protected boolean mustStop() {
		for (StoppingCriterion s : stopping)
			if (s.mustStop(this))
				return true;
		return false;
	}

	public double getCurrentGain() {
		return parentDistribution.getScore()
				- qLeftDistribution.getScoreLeftDistribution().getScore();
	}

	public int getDepth() {
		return parentInfos.getDepth() + 1;
	}

	public int getMinExamples() {
		int countL = qLeftDistribution.getScoreLeftDistribution()
				.getDistribution().getTotal();
		int countR = rightDistribution.getTotal();
		return Math.min(countL, countR);
	}

	private enum BuildMode {
		MODEFAST, MODEFAT;
	}
}
