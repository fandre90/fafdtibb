package fr.insarennes.fafdti.bagging;
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
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.NodeBuilder;
import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.Scheduler;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.visitors.Checker;
import fr.insarennes.fafdti.visitors.StatChecker;
import fr.insarennes.fafdti.visitors.XmlExporter;


public class Launcher implements Observer {
		Logger log = Logger.getLogger(Launcher.class);
	
		int nbBagging;
		int baggingDone;
		List<DecisionTreeHolder> roots;
		String outXml;
		
		public Launcher(String inputNames, String inputData, 
				String outputDir, String xmlOutput,
				List<StoppingCriterion> stoppingList,
				Criterion criterion,
				int nbBagging) throws ParseException{
			//attributes initialization
			this.roots = new ArrayList<DecisionTreeHolder>(nbBagging);
			this.outXml = xmlOutput;
			this.nbBagging = nbBagging;
			this.baggingDone = 0;
			
			//launch process
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
			//get data split
			List<String> datasplit = splitData(inputData);
			//launch every tree
			for(int i=0 ; i<nbBagging ; i++){
				//statistiques object creation
				StatBuilder stats = new StatBuilder(1);
				stats.addObserver(this);
				//NodeBuilder creation
				roots.add(i, new DecisionTreeHolder());
				NodeBuilder nb = new NodeBuilder(featureSpec, 
						datasplit.get(i), outputDir,
						criterion, 
						roots.get(i).getNodeSetter(), 
						stoppingList,
						stats);
				//launch first node
				Scheduler.INSTANCE.execute(nb);
			}
		}
		
		private List<String> splitData(String inputData) {
			//TODO splitté le fichier en plusieurs fichiers 
			//en fonction de nbBagging
			List<String> res = new ArrayList<String>();
			for(int i=0 ; i<nbBagging ; i++)
				res.add(inputData);
			return res;
		}

		public void update(Observable arg0, Object arg1) {
			if(((StatBuilder)arg1).getNbPending()==0){
				incrBaggingDone();
				log.info("One tree fully constructed");
			}
				
			if(baggingDone==nbBagging){
				finish();
			}
		}
		
		private void finish(){
			//stop scheduler
			Scheduler.INSTANCE.shutdown();
			log.info("Construction process done");
			//get decision trees constructed
			List<DecisionTree> trees = new ArrayList<DecisionTree>();
			for(int i=0 ; i<nbBagging ; i++){
				try {
					trees.add(roots.get(i).getRoot());
				} catch (FAFException e) {
					log.error(e.getMessage());
				}
			}
			//bagging trees construction
			BaggingTrees result = new BaggingTrees(trees);
			//check it and stats
			BaggingStatChecker check = new BaggingStatChecker(result);
			check.launch();
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
		
		synchronized private void incrBaggingDone(){
			baggingDone++;
		}
}
