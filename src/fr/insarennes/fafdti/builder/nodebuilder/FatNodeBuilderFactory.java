package fr.insarennes.fafdti.builder.nodebuilder;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;

public class FatNodeBuilderFactory implements INodeBuilderFactory {

	private Criterion criterion;
	private DotNamesInfo namesInfo;
	private StatBuilder stats;
	
	public FatNodeBuilderFactory(Criterion criterion, DotNamesInfo namesInfo,
			StatBuilder stats) {
		this.criterion = criterion;
		this.namesInfo = namesInfo;
		this.stats = stats;
	}

	@Override
	public INodeBuilder makeNodeBuilder(String[][] database,
			ScoredDistributionVector parentDistribution, String id) {
		throw new UnsupportedOperationException(this.getClass().getName() + 
				" cannot make NodeBuilder from database");
	}

	@Override
	public INodeBuilder makeNodeBuilder(Path dataPath,
			ScoredDistributionVector parentDistribution, String id, Path workdir) {
		return new NodeBuilderFat(namesInfo, criterion, stats);
	}

}
