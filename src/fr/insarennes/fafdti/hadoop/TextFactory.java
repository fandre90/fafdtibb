package fr.insarennes.fafdti.hadoop;

import org.apache.hadoop.io.Text;

public class TextFactory implements IFactory<Text> {

	@Override
	public Text newInstance() {
		return new Text();
	}

}
