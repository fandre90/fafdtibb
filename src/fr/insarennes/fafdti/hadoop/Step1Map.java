package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import fr.insarennes.fafdti.builder.AttrSpec;
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.FeatureSpec;
import fr.insarennes.fafdti.builder.Question;

public class Step1Map extends MapperBase<Object, Text, Question, IntWritable>{



	protected void map(Object key, Text dataLine, Context context)
	throws IOException, InterruptedException {
		String strLine  = dataLine.toString();
		strLine = strLine.substring(0, strLine.length() - 1);
		String[] lineTokens = strLine.split(",");
		String label = lineTokens[lineTokens.length-1];
		IntWritable labelIndex = new IntWritable(fs.indexOfLabel(label));
		// Iterate over all attribute values
		for(int i = 0; i < lineTokens.length - 1; i++) {
			// Is it necessary to construct a new object every time ?
			AttrSpec attrSpec = fs.getAttrSpec(i);
			AttrType attrType = attrSpec.getType();
			if(attrType == AttrType.DISCRETE) {
				Question q = new Question(i, attrType, lineTokens[i]);
				System.out.println("Q: " + q);
				System.out.println("I: " + labelIndex);
				context.write(q, labelIndex);
			} else if (attrType == AttrType.TEXT) {
				// FIXME  On ne génère que des 1-Gram ici
				String[] words = lineTokens[i].split("\\s");
				for(String word : words) {
					Question q = new Question(i, attrType, word);
					System.out.println("Q: " + q);
					System.out.println("I: " + labelIndex);
					context.write(q, labelIndex);
				}
			}

		}
	}
}
