package fr.insarennes.fafdti.hadoop;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.AttrSpec;
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.Question;

public class Step1Map extends MapperBase<Object, Text, Question, IntWritable> {

	public final String VALIDATION_REGEX = "(.+,)+(.+)\\.";
	protected Pattern validationPattern;
	Logger log;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		validationPattern = Pattern.compile(VALIDATION_REGEX);
		log = Logger.getLogger(Step1Map.class);
	}

	protected void map(Object key, Text dataLine, Context context)
			throws IOException, InterruptedException {
		String strLine = dataLine.toString();
		Matcher lineMatcher = validationPattern.matcher(strLine);
		if(!lineMatcher.matches()) {
			log.error("Invalid line: " + strLine + "\n."
						+ "Lines must validate the regular expression : "
						+ VALIDATION_REGEX);
			return;
		}
		strLine = strLine.substring(0, strLine.length() - 1);
		String[] lineTokens = strLine.split(",");
		String label = lineTokens[lineTokens.length - 1].trim();
		IntWritable labelIndex = null;
		try {
			labelIndex = new IntWritable(fs.indexOfLabel(label));
			// Iterate over all attribute values
			for (int i = 0; i < lineTokens.length - 1; i++) {
				lineTokens[i] = lineTokens[i].trim();
				// Is it necessary to construct a new object every time ?
				AttrSpec attrSpec = fs.getAttrSpec(i);
				AttrType attrType = attrSpec.getType();
				if (attrType == AttrType.DISCRETE) {
					Question q = new Question(i, attrType, lineTokens[i]);
					// System.out.println("Q: " + q);
					// System.out.println("I: " + labelIndex);
					context.write(q, labelIndex);
				} else if (attrType == AttrType.TEXT) {
					// FIXME On ne génère que des 1-Gram ici
					String[] words = lineTokens[i].split("\\s");
					for (String word : words) {
						Question q = new Question(i, attrType, word);
						// System.out.println("Q: " + q);
						// System.out.println("I: " + labelIndex);
						context.write(q, labelIndex);
					}
				}
			}
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
