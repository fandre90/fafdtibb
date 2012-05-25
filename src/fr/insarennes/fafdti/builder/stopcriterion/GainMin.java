package fr.insarennes.fafdti.builder.stopcriterion;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.builder.HadoopConfSerializer;
import fr.insarennes.fafdti.builder.HadoopConfStockable;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
/** Classe encapsulant le critère du gain minimum pour la construction de l'arbre
 */
public class GainMin extends HadoopConfStockable implements StoppingCriterion {

	private static final String HADOOP_CONFIGURATION_KEY = 
			"stop-criterion-gainmin";

	private static Logger log = Logger.getLogger(GainMin.class);
	private double gainMin;
	
	public GainMin(double gain){
		gainMin = gain;
	}

	@Override
	public boolean mustStop(StopCriterionUtils node) {
		boolean res = node.getCurrentGain() < gainMin;
		if(res)
			log.info("stopping criterion : gain min");
		return res;
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
	public static GainMin fromConf(Configuration conf, String keySuffix)
			throws IOException, ClassNotFoundException {
		String key = HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		return (GainMin) HadoopConfSerializer
				.deserializeFromConf(conf, key);
	}

	public static GainMin fromConf(Configuration conf) throws IOException,
			ClassNotFoundException {
		return fromConf(conf, "");
	}

}
