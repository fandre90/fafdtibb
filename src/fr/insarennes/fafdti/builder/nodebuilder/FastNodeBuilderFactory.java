package fr.insarennes.fafdti.builder.nodebuilder;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;

public class FastNodeBuilderFactory implements INodeBuilderFactory {

	private Criterion criterion;
	private DotNamesInfo namesInfo;

	public FastNodeBuilderFactory(Criterion criterion, DotNamesInfo namesInfo) {
		this.criterion = criterion;
		this.namesInfo = namesInfo;
	}

	@Override
	public INodeBuilder makeNodeBuilder(String[][] database,
			ScoredDistributionVector parentDistribution, String id) throws FAFException {
		return new NodeBuilderFast(criterion, namesInfo, database, parentDistribution, id);
	}

	@Override
	public INodeBuilder makeNodeBuilder(Path dataPath,
			ScoredDistributionVector parentDistribution, String id, Path workdir) {
		throw new UnsupportedOperationException(this.getClass().getName() + 
				" cannot make NodeBuilder from Path");
	}
}
