package fr.insarennes.fafdti.hadoop;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.DotNamesInfo;

/**
 * map de l'étape 0 (calcul de l'entropie et du vecteur statistique)
 * @author Francois LEHERICEY
 */
public class Step0Map extends Mapper<Object, Text, Text, IntWritable>{
	/** feature spec récupéré dans la config du job */
	private DotNamesInfo fs;
	Logger log = Logger.getLogger(Step0Map.class);
	
	@Override
	protected void setup(Context context) throws IOException, InterruptedException {
		try {
			fs = DotNamesInfo.fromConf(context.getConfiguration());
		} catch (ClassNotFoundException e) {
			// FIXME Auto-generated catch block
			// LOG ERROR MESSAGE HERE
			e.printStackTrace();
		}
		log.info("setup Step0Map");
	}

	@Override
	protected void map(Object key, Text value, Context context)
	throws IOException, InterruptedException {
		
		// on récupère l'étiquette
		String label = "";
		StringTokenizer st = new StringTokenizer(value.toString(), ",");
		while(st.hasMoreTokens()){
			label = st.nextToken();
		}
		
		// Enlever le point final.
		label = label.substring(0, label.length() - 1);
		int index;
		try {
			index = fs.indexOfLabel(label);
			context.write(new Text("labels"), new IntWritable(index));
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
