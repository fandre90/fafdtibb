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

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.TextAttrSpec.ExpertType;

/**
 * Memory representation of a .names file.
 * This file specifies :
 *  - The various labels by which an individual can be labeled
 *  - All the attributes of the individuals along with their types (discrete, continuous
 *    or text) and their properties (e.g. for text attributes, different mode of
 *    analysis are proposed : NGram, FGram and SGram
 */
public class DotNamesInfo extends HadoopConfStockable {
	private static final long serialVersionUID = -6855907309314569268L;
	/** clé utilisé pour stocker la valeur serialisé dans la config d'hadoop */
	private static final String HADOOP_CONFIGURATION_KEY = "faf-etiq";
	/** liste des etiquettes */
	private String[] labelsArray;
	/** index inversé des etiquettes */
	private Map<String, Integer> reverseLabelMap;
	private ArrayList<AttrSpec> attributeSpec;
	private String[] commentStartChars = { "|", "#" };

	/**
	 * Read a .names file and stores the parsed information
	 * @param file The path to the file to be read
	 * @param fs The FileSystem in which the file is located
	 * @throws IOException
	 */
	public DotNamesInfo(Path file, FileSystem fs) throws IOException,
			ParseException {
		this.attributeSpec = new ArrayList<AttrSpec>();
		FSDataInputStream in = fs.open(file);
		LineReader lr = new LineReader(in);
		String labelLine = skipComments(lr);
		this.parseLabelLine(labelLine);
		String attrLine = skipComments(lr);
		while (!attrLine.equals("")) {
			this.parseAttributeLine(attrLine);
			attrLine = skipComments(lr);
		}
		lr.close();
	}
	
	/**
	 * Skip comments and empty lines and return the next line
	 * to be parsed.
	 * @return the next line to parse
	 * @throws IOException 
	 */
	private String skipComments(LineReader lr) throws IOException {
		Text text = new Text();
		boolean skipComments = true;
		int readBytes = 0;
		while (skipComments) {
			skipComments = false;
			readBytes = lr.readLine(text);
			if(readBytes == 0) {
				return "";
			}
			for (String startChar : commentStartChars) {
				if (text.toString().startsWith(startChar) 
						|| text.toString().trim().equals("")) {
					skipComments = true;
				}
			}
		}
		return text.toString();
	}

	/**
	 * Parse the line specifying all possible labels
	 * @param labelLine the line to parse
	 * @throws ParseException if the line could not be parsed
	 */
	private void parseLabelLine(String labelLine) throws ParseException {
		labelLine = labelLine.trim();
		int len = labelLine.length();
		// Remove the dot a the end of the line
		if (labelLine.charAt(len - 1) != '.')
			throw new ParseException("Labels line must end with a dot");
		labelLine = labelLine.substring(0, len - 1);
		this.labelsArray = labelLine.split(",");
		// Create labels array
		for(int i=0; i<labelsArray.length; ++i) {
			this.labelsArray[i] = this.labelsArray[i].trim();
		}
		// Create reverse array mapping
		reverseLabelMap = new HashMap<String, Integer>();
		for (int i = 0; i < labelsArray.length; i++) {
			reverseLabelMap.put(labelsArray[i], i);
		}
	}

	/**
	 * Parse an attribute specification line. This line specifies the attribute
	 * name, its type and various properties
	 * @param attrLine the line to parse
	 * @throws ParseException if the line could not be parsed
	 */
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
	 * Returns the index corresponding to a label.
	 * This method allows the program to use number to represent
	 * labels instead of strings.
	 * @param label the label to be indexed
	 * @return the index of the label
	 */
	public int indexOfLabel(String label) throws FAFException {
		Integer labelIndex = reverseLabelMap.get(label);
		if(labelIndex == null) {
			throw new FAFException("Non existent label: " + label);
		}
		return labelIndex;
	}

	/**
	 * Returns an array containing all possible labels
	 * @return the array containing all labels
	 */
	public String[] getLabels() {
		return this.labelsArray.clone();
	}

	/**
	 * Returns the number of labels
	 * 
	 * @return the number of labels
	 */
	public int numOfLabel() {
		return labelsArray.length;
	}

	public AttrSpec getAttrSpec(int col) {
		return (AttrSpec) this.attributeSpec.get(col).clone();
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
		String key = DotNamesInfo.HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
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
	public static DotNamesInfo fromConf(Configuration conf, String keySuffix)
			throws IOException, ClassNotFoundException {
		String key = DotNamesInfo.HADOOP_CONFIGURATION_KEY + "-" + keySuffix;
		return (DotNamesInfo) HadoopConfSerializer
				.deserializeFromConf(conf, key);
	}

	public static DotNamesInfo fromConf(Configuration conf) throws IOException,
			ClassNotFoundException {
		return fromConf(conf, "");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributeSpec == null) ? 0 : attributeSpec.hashCode());
		result = prime * result + Arrays.hashCode(labelsArray);
		result = prime * result
				+ ((reverseLabelMap == null) ? 0 : reverseLabelMap.hashCode());
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
		DotNamesInfo other = (DotNamesInfo) obj;
		if (attributeSpec == null) {
			if (other.attributeSpec != null)
				return false;
		} else if (!attributeSpec.equals(other.attributeSpec))
			return false;
		if (!Arrays.equals(labelsArray, other.labelsArray))
			return false;
		if (reverseLabelMap == null) {
			if (other.reverseLabelMap != null)
				return false;
		} else if (!reverseLabelMap.equals(other.reverseLabelMap))
			return false;
		return true;
	}

}
