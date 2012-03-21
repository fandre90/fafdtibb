package fr.insarennes.fafdti.builder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.visitors.Checker;
import fr.insarennes.fafdti.visitors.XmlExporter;


public class Launcher implements Observer {
		Logger log = Logger.getLogger(Launcher.class);
	
		DecisionTreeHolder root;
		DecisionTree result;
		String outXml;
		
		public Launcher(String inputNames, String inputData, 
				String outputDir, String xmlOutput) throws ParseException{
			 root = new DecisionTreeHolder();
			 result = null;
			 outXml = xmlOutput;
			
			//stopping criterion
			List<StoppingCriterion> stopping = new ArrayList<StoppingCriterion>();
			stopping.add(new DepthMax(5));
			stopping.add(new ExampleMin(1));
			stopping.add(new GainMin(0.1));
			//stats
			StatBuilder stats = new StatBuilder(1);
			stats.addObserver(this);
			//DotNamesInfos
			Configuration conf = new Configuration();
			FileSystem fileSystem;

			DotNamesInfo featureSpec = null;
			try {
				fileSystem = FileSystem.get(conf);
				featureSpec = new DotNamesInfo(new Path(inputNames), fileSystem);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//NodeBuilder
			NodeBuilder nb = new NodeBuilder(featureSpec, 
					inputData, outputDir,
					new EntropyCriterion(), 
					root.getNodeSetter(), 
					stopping,
					stats);
			
			Scheduler.INSTANCE.execute(nb);
		}
		
		public void update(Observable arg0, Object arg1) {
			if(((StatBuilder)arg1).getNbPending()==0){
				//on arrête le scheduler
				Scheduler.INSTANCE.shutdown();
				log.info("Construction process done");
				try {
					result = root.getRoot();
				} catch (FAFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//check
				Checker check = new Checker();
				result.accept(check);
				if(!check.checkOK()){
					log.info("construction failed : pending found");
					System.exit(1);
				}
				log.info("Validation tree resulting : check OK");
				//export xml
				XmlExporter xml = new XmlExporter(result, outXml);
				xml.launch();
				log.info("Tree resulting exports in "+outXml+".xml");
			}		
		}
}
