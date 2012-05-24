import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.builder.ScoredDistributionVector;
import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.hadoop.Step0Map;
import fr.insarennes.fafdti.hadoop.Step0Red;



public class Test {
	public static void main(String[] args) throws Exception {
		String file = "letter-recognition";
		String outputDir0 = "output-step0";
		
		FileSystem fs = FileSystem.get(new Configuration());//utilisé pour lire les fichiers
		DotNamesInfo featureSpec = new DotNamesInfo(new Path(file+".names"), fs);
		
		Logger log = Logger.getLogger(Test.class);

		{//etape 0
			log.info("debut de l'étape 0");
			
			Configuration conf = new Configuration();
			featureSpec.toConf(conf);
			
			Job job = new Job(conf, "Calcul de l'entropie associée l'ensemble d'exemples courant");
	
			job.setOutputKeyClass(Text.class);
			job.setOutputValueClass(IntWritable.class);
	
			job.setMapperClass(Step0Map.class);
			job.setReducerClass(Step0Red.class);
	
			job.setInputFormatClass(TextInputFormat.class);
			job.setOutputFormatClass(TextOutputFormat.class);
	
			FileInputFormat.addInputPath(job, new Path(file+".data"));
			FileOutputFormat.setOutputPath(job, new Path(outputDir0));
	
			job.waitForCompletion(false);
		}
		
		ScoredDistributionVector stats;
		{//on recupère le vecteur stats
			FSDataInputStream in = fs.open(new Path(outputDir0+"/part-r-00000"));
			LineReader lr = new LineReader(in);
			
			Text text = new Text();
			lr.readLine(text);
			//on enlève la tabulation qui débute le fichier
			String stext = text.toString().substring(1, text.toString().length());
			stats = new ScoredDistributionVector(stext);
		}
		
		System.out.println(stats+"");
		
		{//etape 1
			
		}
	}
}
