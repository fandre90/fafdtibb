package fr.insarennes.fafdti.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

import fr.insarennes.fafdti.builder.TextAttrSpec.ExpertType;

/**
 * charge les etiquetes d'un fichier .names
 * 
 * @author Francois LEHERICEY
 */
public class FeatureSpec extends HadoopConfStockable {
	private static final long serialVersionUID = -6855907309314569268L;
	/** clé utilisé pour stocker la valeur serialisé dans la config d'hadoop */
	private static final String HADOOP_CONFIGURATION_KEY = "faf-etiq";
	/** liste des etiquettes */
	private String[] etiquettes;
	/** index inversé des etiquettes */
	private Map<String, Integer> rEtiquettes;
	private ArrayList<AttrSpec> attributeSpec;
	private String[] commentStartChars = { "|", "#" };

	/**
	 * construit un FeatureSpec en lisant un fichier .names
	 * 
	 * @param file
	 *            chemin du fichier .name
	 * @param fs
	 *            systeme de fichier dans lequel lire
	 * @throws IOException
	 */
	public FeatureSpec(Path file, FileSystem fs) throws IOException,
			ParseException {

		this.attributeSpec = new ArrayList<AttrSpec>();
		// FileSystem fs = FileSystem.get(conf);
		FSDataInputStream in = fs.open(file);
		LineReader lr = new LineReader(in);
		Text text = new Text();

		// Skip comments
		boolean skipComments = true;
		while (skipComments) {
			skipComments = false;
			lr.readLine(text);
			for (String startChar : commentStartChars) {
				if (text.toString().startsWith(startChar)) {
					// lr.readLine(text);
					skipComments = true;
				}
			}
		}

		// while (text.toString().startsWith("|")) {
		// lr.readLine(text);
		// }

		this.parseLabelLine(text.toString());
		// index inverse
		rEtiquettes = new HashMap<String, Integer>();
		for (int i = 0; i < etiquettes.length; i++) {
			rEtiquettes.put(etiquettes[i], i);
		}

		lr.readLine(text);
		while (!text.toString().equals("")) {
			this.parseAttributeLine(text.toString());
			lr.readLine(text);
		}

		lr.close();
	}

	private void parseLabelLine(String labelLine) throws ParseException {
		int len = labelLine.length();
		// Remove the dot a the end of the line
		if (labelLine.charAt(len - 1) != '.')
			throw new ParseException("Labels line must end with a dot");
		labelLine = labelLine.substring(0, len - 1);
		this.etiquettes = labelLine.split(",");
	}

	// FIXME FIXME FIXME Parsing du cutoff
	private void parseAttributeLine(String attrLine) throws ParseException {
		// Skip comments
		if (attrLine.startsWith("|"))
			return;
		int len = attrLine.length();
		if (attrLine.charAt(len - 1) != '.')
			throw new ParseException("Attribute lines must end with a dot");
		attrLine = attrLine.substring(0, len - 1);
		String[] tokens = attrLine.split(":");
		// FIXME : More precise error messages
		if (tokens.length < 2)
			throw new ParseException("Bad Line : " + attrLine);
		String name = tokens[0].trim();
		String typeStr = tokens[1].trim().toLowerCase();

		if (typeStr.equals("discrete"))
			this.attributeSpec.add(new DiscreteAttrSpec());
		else if (typeStr.equals("continuous"))
			this.attributeSpec.add(new ContinuousAttrSpec());
		else if (typeStr.equals("text")) {
			int expertLength = 0;
			int expertLevel = 0;
			ExpertType expertType = null;
			String[] paramTokens = tokens[2].trim().split("\\s+");
			for (String param : paramTokens) {
				String[] keyValTok = param.split("=");
				String key = keyValTok[0];
				String value = keyValTok[1];
				if (key.equals("expert_level")) {
					expertLevel = Integer.parseInt(value);
				} else if (key.equals("expert_type")) {
					// FIXME FIXME FIXME Deal with other expert types
					expertType = TextAttrSpec.ExpertType.NGRAM;
				} else if (key.equals("expert_length")) {
					expertLength = Integer.parseInt(value);
				}
			}
			this.attributeSpec.add(new TextAttrSpec(expertType, expertLength,
					expertLevel));
		} else
			throw new ParseException("Bad attribute type : -" + typeStr + "-");

	}

	/**
	 * rend l'index de l'etiquette e+ "-" + keySuffix
	 * 
	 * @param e
	 *            etiquette
	 * @return index
	 */
	public int indexOfLabel(String e) {
		return rEtiquettes.get(e);
	}

	public AttrSpec getAttrSpec(int col) {
		return (AttrSpec) this.attributeSpec.get(col).clone();
	}

	public String[] getLabels() {
		return this.etiquettes.clone();
	}

	/**
	 * rend le nombre d'etiquettes
	 * 
	 * @return le nombre d'etiquettes
	 */
	public int nbEtiquettes() {
		return etiquettes.length;
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
		String key = FeatureSpec.HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
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
	public static FeatureSpec fromConf(Configuration conf, String keySuffix)
			throws IOException, ClassNotFoundException {
		String key = FeatureSpec.HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		return (FeatureSpec) HadoopConfSerializer
				.deserializeFromConf(conf, key);
	}

	public static FeatureSpec fromConf(Configuration conf) throws IOException,
			ClassNotFoundException {
		return fromConf(conf, "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeSpec == null) ? 0 : attributeSpec.hashCode());
		result = prime * result + Arrays.hashCode(etiquettes);
		result = prime * result
				+ ((rEtiquettes == null) ? 0 : rEtiquettes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FeatureSpec other = (FeatureSpec) obj;
		if (attributeSpec == null) {
			if (other.attributeSpec != null)
				return false;
		} else if (!attributeSpec.equals(other.attributeSpec))
			return false;
		if (!Arrays.equals(etiquettes, other.etiquettes))
			return false;
		if (rEtiquettes == null) {
			if (other.rEtiquettes != null)
				return false;
		} else if (!rEtiquettes.equals(other.rEtiquettes))
			return false;
		return true;
	}

}
