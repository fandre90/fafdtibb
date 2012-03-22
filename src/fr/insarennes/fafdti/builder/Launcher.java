package fr.insarennes.fafdti.builder;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.visitors.Checker;
import fr.insarennes.fafdti.visitors.StatChecker;
import fr.insarennes.fafdti.visitors.XmlExporter;


public class Launcher implements Observer {
		Logger log = Logger.getLogger(Launcher.class);
	
		DecisionTreeHolder root;
		DecisionTree result;
		String outXml;
		
		public Launcher(String inputNames, String inputData, 
				String outputDir, String xmlOutput,
				List<StoppingCriterion> stoppingList,
				Criterion criterion) throws ParseException{
			//attributes initialization
			root = new DecisionTreeHolder();
			result = null;
			outXml = xmlOutput;
			
			//statistiques object creation
			StatBuilder stats = new StatBuilder(1);
			stats.addObserver(this);
			//DotNamesInfos creation
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
			//NodeBuilder creation
			NodeBuilder nb = new NodeBuilder(featureSpec, 
					inputData, outputDir,
					criterion, 
					root.getNodeSetter(), 
					stoppingList,
					stats);
			//launch first node
			Scheduler.INSTANCE.execute(nb);
		}
		
		public void update(Observable arg0, Object arg1) {
			if(((StatBuilder)arg1).getNbPending()==0){
				//stop scheduler
				Scheduler.INSTANCE.shutdown();
				log.info("Construction process done");
				//get decision tree constructed
				try {
					result = root.getRoot();
				} catch (FAFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//check it and stats
				StatChecker check = new StatChecker();
				result.accept(check);
				if(!check.checkOK()){
					log.error("Construction failed : pending found");
					return;
				}
				log.info("Validation tree resul : check OK");
				log.info("-------Stats-------");
				log.info(check.toString());
				log.info("-------------------");
				//export xml
				XmlExporter xml = new XmlExporter(result, outXml);
				xml.launch();
				log.info("Tree resulting exports in "+outXml+".xml");
			}		
		}
}
