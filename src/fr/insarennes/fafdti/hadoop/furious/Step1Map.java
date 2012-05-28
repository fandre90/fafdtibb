package fr.insarennes.fafdti.hadoop.furious;

import java.io.IOException;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.builder.namesinfo.AttrSpec;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.namesinfo.TextAttrSpec;
import fr.insarennes.fafdti.builder.nodebuilder.GramGenerator;
import fr.insarennes.fafdti.builder.nodebuilder.ThresholdComputer;
import fr.insarennes.fafdti.hadoop.MapperBase;
import fr.insarennes.fafdti.hadoop.Value;
import fr.insarennes.fafdti.hadoop.WritableValueSDVSortedMap;


public class Step1Map
		extends
		MapperBase<Object, Text, IntWritable, WritableValueSDVSortedMap> {

	ScoredDistributionVector entAndStats;

	WritableValueSDVSortedMap valueDistMap;
	ScoredDistributionVector distribution;
	IntWritable attrIndex;
	Value value;

	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);
		valueDistMap = new WritableValueSDVSortedMap();
		distribution = new ScoredDistributionVector(fs.numOfLabel());
		attrIndex = new IntWritable();
		value = new Value();
	}


	protected void map(Object key, Text dataLine, Context context)
			throws IOException, InterruptedException {
		String strLine = dataLine.toString();
		strLine = strLine.substring(0, strLine.length() - 1);
		String[] lineTokens = strLine.split(",");
		String label = lineTokens[lineTokens.length - 1].trim();
		int labelIndex;
		try {
			labelIndex = fs.indexOfLabel(label);
			// Iterate over all attribute values
			for (int i = 0; i < lineTokens.length - 1; i++) {
				lineTokens[i] = lineTokens[i].trim();
				AttrSpec attrSpec = fs.getAttrSpec(i);
				AttrType attrType = attrSpec.getType();
				distribution.reset();
				distribution.incrStat(labelIndex);
				valueDistMap.clear();
				attrIndex.set(i);
				if (attrType == AttrType.CONTINUOUS) {
					double normDouble = ThresholdComputer.normalizeValue(
							Double.parseDouble(lineTokens[i]),
							ThresholdComputer.EPSILON);
					value.set(normDouble);
					valueDistMap.put(value, distribution);

				} else if (attrType == AttrType.DISCRETE) {
					value.set(lineTokens[i]);
					valueDistMap.put(value, distribution);
				} else if (attrType == AttrType.TEXT) {
					TextAttrSpec textAttr = (TextAttrSpec) attrSpec;
					String[] words = lineTokens[i].split("\\s+");
					if(textAttr.getExpertType() == GramType.SGRAM) {
						Set<SGram> sGramSet = GramGenerator.generateSGram(textAttr, words);
						for(SGram sGram: sGramSet) {
							valueDistMap.put(new Value(sGram), distribution);
						}
					} else {
						Set<FGram> fGramSet = GramGenerator.generateAllNFGram(textAttr, words);
						for(FGram fGram: fGramSet) {
							valueDistMap.put(new Value(fGram), distribution);
						}
					}
				}
				context.write(attrIndex, valueDistMap);
			}
		} catch (FAFException e) {
			e.printStackTrace();
		}
	}
}
