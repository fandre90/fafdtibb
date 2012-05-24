package fr.insarennes.fafdti.builder;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.builder.stopcriterion.ParentInfos;
import fr.insarennes.fafdti.builder.stopcriterion.StopCriterionUtils;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.hadoop.QuestionScoreLeftDistribution;
import fr.insarennes.fafdti.tree.DecisionNodeSetter;
/**
 * Classe qui construit un noeud (ou une feuille) et fait l'appel récursif.
 * On note qu'une fois la construciton finie, dans le cas où c'est un noeud, le thread
 * lance 2 autres threads puis fini ; ses fils seront assigné grâce au mécanisme
 * de DecisionNodeSetter.
 */
public abstract class NodeBuilder implements Runnable, StopCriterionUtils {
	
	protected static Logger log = Logger.getLogger(NodeBuilder.class);

	protected Path inputDataPath;
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
	protected String id;
	
	protected int relaunchCounter;
	protected final int MAX_RELAUNCH_COUNTER = 10;
	
	//First node constructor
	public NodeBuilder(DotNamesInfo featureSpec, 
			String inputDataPath,
			String workingDir,
			Criterion criterion,
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			StatBuilder stats,
			String baggingId) {
		this.parentInfos = new ParentInfos(0, "launcher", baggingId);
		this.featureSpec = featureSpec;
		this.inputDataPath = new Path(inputDataPath);
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.stats = stats;
		this.id = parentInfos.getBaggingId()+"-"+Integer.toString(stats.getNextId());
		this.workingDir = new Path(workingDir, id);
		this.relaunchCounter = 0;
	}
	
	//Recursive constructor
	public NodeBuilder(DotNamesInfo featureSpec, 
			String inputDataPath,
			String workingDir, 
			Criterion criterion, 
			DecisionNodeSetter nodeSetter, 
			List<StoppingCriterion> stopping,
			ParentInfos parentInfos, 
			ScoredDistributionVector parentDistribution,
			StatBuilder stats) {
		this.featureSpec = featureSpec;
		this.inputDataPath = new Path(inputDataPath);
		this.criterion = criterion;
		this.nodeSetter = nodeSetter;
		this.stopping = stopping;
		this.parentInfos = parentInfos;
		this.parentDistribution = parentDistribution;
		this.stats = stats;
		this.id = parentInfos.getBaggingId()+"-"+Integer.toString(stats.getNextId());
		this.workingDir = new Path(workingDir, id);
		this.relaunchCounter = 0;
	}
	
	@Override
	public abstract void run();
	
	//*******************relaunching utils methods*******************//
	
	protected void deleteDir(String dir){
		FileSystem fs = null;
		try {
			fs = FileSystem.get(new Configuration());
			fs.delete(new Path(workingDir,dir), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	//*****************stopping utils methods************//
	
	protected boolean mustStop(){
		for(StoppingCriterion s : stopping)		
			if(s.mustStop(this))
				return true;
		return false;
	}

	public double getCurrentGain() {
		return parentDistribution.getScore() - qLeftDistribution.getScoreLeftDistribution().getScore();
	}

	public int getDepth(){
		return parentInfos.getDepth() + 1;
	}

	public int getMinExamples() {
		int countL = qLeftDistribution.getScoreLeftDistribution().getDistribution().getTotal();
		int countR = rightDistribution.getTotal();
		return Math.min(countL, countR);
	}

}
