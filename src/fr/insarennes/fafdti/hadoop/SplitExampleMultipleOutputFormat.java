package fr.insarennes.fafdti.hadoop;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.lib.MultipleTextOutputFormat;

import fr.insarennes.fafdti.builder.LabeledExample;

public class SplitExampleMultipleOutputFormat extends
		MultipleTextOutputFormat<Text, LabeledExample> {
	
	@Override
	protected String generateFileNameForKeyValue(Text key, LabeledExample value,
            String name) {
		return key.toString();
	}
	
	@Override
	protected Text generateActualKey(Text key, LabeledExample value) {
		return null;
	}
}
