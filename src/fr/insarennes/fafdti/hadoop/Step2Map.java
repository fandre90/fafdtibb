package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import fr.insarennes.fafdti.builder.AttrSpec;
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.ScoredDistributionVector;

public class Step2Map extends MapperBase<Object, Text, IntWritable, ContinuousAttrLabelPair> {
	Question attrValue = new Question();
	ScoredDistributionVector entAndStats;

	protected void map(Object key, Text dataLine, Context context)
			throws IOException, InterruptedException {
		String strLine  = dataLine.toString();
		strLine = strLine.substring(0, strLine.length() - 1);
		String[] lineTokens = strLine.split(",");
		String label = lineTokens[lineTokens.length-1];
		int labelIndex = fs.indexOfLabel(label);
		// Iterate over all attribute values
		for(int i = 0; i < lineTokens.length - 1; i++) {
			AttrSpec attrSpec = fs.getAttrSpec(i);
			AttrType attrType = attrSpec.getType();
			if(attrType == AttrType.CONTINUOUS) {
				double value = Double.parseDouble(lineTokens[i]);
				ContinuousAttrLabelPair pair = 
						new ContinuousAttrLabelPair(labelIndex, value);
				IntWritable attrIndex = new IntWritable(i);
				context.write(attrIndex, pair);
				System.out.println(attrIndex + " " + pair);
			}
		}
	}

}
