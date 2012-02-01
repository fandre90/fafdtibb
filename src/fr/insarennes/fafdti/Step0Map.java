package fr.insarennes.fafdti;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;

/**
 * map de l'étape 0 (calcul de l'entropie et du vecteur statistique)
 * @author Francois LEHERICEY
 */
public class Step0Map extends Mapper<Object, Text, Text, IntWritable>{
	/** feature spec récupéré dans la config du job */
	private FeatureSpec fs;
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		try {
			fs = FeatureSpec.fromConf(context.getConfiguration());
		} catch (ClassNotFoundException e) {
			// FIXME Auto-generated catch block
			// LOG ERROR MESSAGE HERE
			e.printStackTrace();
		}
	}

	@Override
	protected void map(Object key, Text value, Context context)
	throws IOException, InterruptedException {
		
		// on récupère l'étiquette
		String etiquette = "";
		StringTokenizer st = new StringTokenizer(value.toString(), ",");
		while(st.hasMoreTokens()){
			etiquette = st.nextToken();
		}
		
		// Enlever le point final.
		etiquette = etiquette.substring(0, etiquette.length() - 1);
		int index = fs.indexOfLabel(etiquette);
		
		context.write(new Text(""), new IntWritable(index));
	}
}
