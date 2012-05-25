package fr.insarennes.fafdti.builder.stopcriterion;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.builder.HadoopConfSerializer;
import fr.insarennes.fafdti.builder.HadoopConfStockable;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
/** Classe encapsulant le critère du nombre minimal d'exemples par feuille
 *  pour la construction de l'arbre
 */
public class ExampleMin extends HadoopConfStockable implements StoppingCriterion {

	private static final String HADOOP_CONFIGURATION_KEY = 
			"stop-criterion-examplemin";
	
	private static Logger log = Logger.getLogger(ExampleMin.class);
	private int exampleMin;
	
	public ExampleMin(int example){
		exampleMin = example;
	}
	
	public boolean mustStop(StopCriterionUtils node) {
		boolean res = node.getMinExamples() < exampleMin;
		if(res)
			log.info("stopping criterion : examples min by leaf");
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
	public static ExampleMin fromConf(Configuration conf, String keySuffix)
			throws IOException, ClassNotFoundException {
		String key = HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		return (ExampleMin) HadoopConfSerializer
				.deserializeFromConf(conf, key);
	}

	public static ExampleMin fromConf(Configuration conf) throws IOException,
			ClassNotFoundException {
		return fromConf(conf, "");
	}

}
