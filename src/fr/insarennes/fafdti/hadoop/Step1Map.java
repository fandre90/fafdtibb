package fr.insarennes.fafdti.hadoop;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
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
import fr.insarennes.fafdti.builder.TextAttrSpec;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.gram.SGram;

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
				AttrSpec attrSpec = fs.getAttrSpec(i);
				AttrType attrType = attrSpec.getType();
				if (attrType == AttrType.DISCRETE) {
					Question q = new Question(i, attrType, lineTokens[i]);
					context.write(q, labelIndex);
				} else if (attrType == AttrType.TEXT) {
					String[] words = lineTokens[i].split("\\s+");
					TextAttrSpec textAttr = (TextAttrSpec) attrSpec;
					if(textAttr.getExpertType() == GramType.SGRAM) {
						generateSGram(context, labelIndex, i, textAttr, words);
					} else {
						generateNFGram(context, labelIndex, i, textAttr, words);
					}
				}
			}
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
		protected void generateNFGram(Context ctx, IntWritable labelIndex, 
				int qIdx, TextAttrSpec textAttr, String[] words) 
						throws IOException, InterruptedException {
			int minSize = 1;
			int maxSize = textAttr.getExpertLength();
			if(textAttr.getExpertType() == GramType.FGRAM) {
				minSize = maxSize;
			}
			for(int size = minSize; size <= maxSize; ++size) {
				Set<FGram> fGramSet = new HashSet<FGram>();
				for(int i=0; i<words.length; ++i) {
					String[] gramWords = new String[size];
					if(i + size - 1 < words.length) {
						for(int j=0; j < size; j++) {
							gramWords[j] = words[i + j];
						}
						fGramSet.add(new FGram(gramWords));
					}
				}
				for(FGram fGram : fGramSet) {
					Question q = new Question(qIdx, AttrType.TEXT, fGram);
					ctx.write(q, labelIndex);
				}
			}
		}

		protected void generateSGram(Context ctx, IntWritable labelIndex, 
				int qIdx, TextAttrSpec textAttr,  String[] words) 
						throws IOException, InterruptedException {
			int expLen = textAttr.getExpertLength();
			Set<SGram> sGramSet = new HashSet<SGram>();
			for(int dist = 0; dist <= expLen; ++dist) {
				for(int i=0; i< words.length - dist - 1; ++i) {
					String firstWord = words[i];
					String lastWord = words[i + dist + 1];
					sGramSet.add(new SGram(firstWord, lastWord, expLen));
				}
			}
			for(SGram sGram : sGramSet) {
				Question q = new Question(qIdx, AttrType.TEXT, sGram);
				ctx.write(q, labelIndex);
			}
		}
}
