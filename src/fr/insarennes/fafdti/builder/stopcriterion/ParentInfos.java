package fr.insarennes.fafdti.builder.stopcriterion;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

import fr.insarennes.fafdti.builder.HadoopConfSerializer;
import fr.insarennes.fafdti.builder.HadoopConfStockable;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;

/**
 * Classe encapsulant différentes informations utiles à la construction d'un DecisionTree
 * sur son parent (tel que la hauteur de son parent ou son identifiant)
 */
public class ParentInfos extends HadoopConfStockable {

	private static final String HADOOP_CONFIGURATION_KEY = 
			"parentinfos";

	private int depth;
	private String id;
	private String baggingId;
	
	public ParentInfos(int depth, String id, String baggingId) {
		this.depth = depth;
		this.id = id;
		this.baggingId = baggingId;
	}
	
	public int getDepth() {
		return depth;
	}

	public String getId() {
		return id;
	}

	public String getBaggingId() {
		return baggingId;
	}

	/**
	 * insere dans conf le contenu de l'instance courante
	 * 
	 * @param conf
	 *            objet conf dans lequel ajouter le featrureSpec
	 * @throws IOException
	 */
	@Override
	public void toConf(Configuration conf, String keySuffix) throws IOException {
		String key = HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		HadoopConfSerializer.serializeToConf(this, conf, key);
	}

	/**
	 * recupere une FeatureSpec depuis un objet conf
	 * 
	 * @param conf
	 *            objet conf dans lequel lire le FeatureSpec (il doit y avoir
	 *            été placé par toConf)
	 * @return une instance de FeatureSpec
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static ParentInfos fromConf(Configuration conf, String keySuffix)
			throws IOException, ClassNotFoundException {
		String key = HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		return (ParentInfos) HadoopConfSerializer
				.deserializeFromConf(conf, key);
	}

	public static ParentInfos fromConf(Configuration conf) throws IOException,
			ClassNotFoundException {
		return fromConf(conf, "");
	}
}
