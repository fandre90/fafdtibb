package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import fr.insarennes.fafdti.builder.ScoredDistributionVector;

/**
 * reduce de l'étape 0 (calcul de l'entropie et du vecteur statistique)
 * @author Francois LEHERICEY
 */
public class Step0Red extends ReducerBase<Text, IntWritable, Text, ScoredDistributionVector>{
	/** feature spec récupéré dans la config du job */

	@Override
	protected void reduce(Text t, Iterable<IntWritable> etiquettes, Context context) 
	throws IOException ,InterruptedException {
		ScoredDistributionVector out = 
				new ScoredDistributionVector(fs.nbEtiquettes());
		for (IntWritable e : etiquettes) {
			out.incrStat(e.get());
		}
		
		out.rate(criterion);
		context.write(new Text("scored disctribution vector"), out);
	}
}
