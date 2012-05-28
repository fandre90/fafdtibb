package fr.insarennes.fafdti.hadoop.furious;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.hadoop.ReducerBase;

/**
 * reduce de l'étape 0 (calcul de l'entropie et du vecteur statistique)
 * @author Francois LEHERICEY
 */
public class Step0Red extends ReducerBase<Text, IntWritable, Text, ScoredDistributionVector>{
	/** feature spec récupéré dans la config du job */
	Logger log = Logger.getLogger(Step0Red.class);
	
	@Override
	protected void reduce(Text t, Iterable<IntWritable> etiquettes, Context context) 
	throws IOException ,InterruptedException {
		log.debug("dotNamesInfo="+fs.toString());
		ScoredDistributionVector out = 
				new ScoredDistributionVector(fs.numOfLabel());
		for (IntWritable e : etiquettes) {
			out.incrStat(e.get());
		}
		
		out.rate(criterion);
		context.write(new Text("scored disctribution vector"), out);
		log.info("finished");
	}
}
