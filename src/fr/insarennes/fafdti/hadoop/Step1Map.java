package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;

import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.FeatureSpec;
import fr.insarennes.fafdti.builder.Question;

public class Step1Map extends Mapper<Object, Text, Question, IntWritable>{

	private FeatureSpec fs;

	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		try {
			Configuration conf = context.getConfiguration();
			fs = FeatureSpec.fromConf(conf);
		} catch (ClassNotFoundException e) {
			// FIXME Auto-generated catch block
			// LOG ERROR MESSAGE HERE
			e.printStackTrace();
		}
	}

	protected void map(Object key, Text dataLine, Context context)
	throws IOException, InterruptedException {
		String strLine  = dataLine.toString();
		String[] lineTokens = strLine.split(",");
		String label = lineTokens[lineTokens.length-1];
		IntWritable labelIndex = new IntWritable(fs.indexOfLabel(label));
		// Iterate over all attribute values
		for(int i = 0; i < lineTokens.length - 1; i++) {
			// Is it necessary to construct a new object every time ?
			AttrType attrType = fs.getAttrSpec(i).getType();
			Question q;
			if(attrType != AttrType.CONTINUOUS) {
				double value = Double.parseDouble(lineTokens[i]);
				q = new Question(i, attrType, value);
				context.write(q, labelIndex);
			}
		}
	}
}
