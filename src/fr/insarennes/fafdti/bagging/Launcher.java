package fr.insarennes.fafdti.bagging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Util;
import fr.insarennes.fafdti.builder.Criterion;
import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.NodeBuilder;
import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.Scheduler;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.visitors.XmlExporter;


public class Launcher implements Observer {
		private Logger log = Logger.getLogger(Launcher.class);
	
		private int nbBagging;
		private int baggingDone;
		private List<DecisionTreeHolder> roots;
		private String outXml;
		private double baggingPercent;	//between 0 and 1
		private final String baggingDir = "bagging-data";  
		
		public Launcher(String inputNames, String inputData, 
				String outputDir, String xmlOutput,
				List<StoppingCriterion> stoppingList,
				Criterion criterion,
				int nbBagging,
				double baggingPercent) throws ParseException{
			//attributes initialization
			this.roots = new ArrayList<DecisionTreeHolder>(nbBagging);
			this.outXml = xmlOutput;
			this.nbBagging = nbBagging;
			this.baggingDone = 0;
			this.baggingPercent = baggingPercent;
			
			//launch process
			//DotNamesInfos creation
			Configuration conf = new Configuration();
			FileSystem fileSystem = null;
			DotNamesInfo featureSpec = null;
			try {
				fileSystem = FileSystem.get(conf);
				featureSpec = new DotNamesInfo(new Path(inputNames), fileSystem);
			} catch (IOException e1) {
				log.error(e1.getMessage());
			}
			//get data split
			List<String> datasplit = splitData(inputData, outputDir, fileSystem);
			log.info("Data file splitting done");
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
		
		private List<String> splitData(String inputData, String outputDir, FileSystem filesystem) {
			List<String> res = new ArrayList<String>();
			if(nbBagging==1){
				res.add(inputData);
			}
			else{
				//count number of examples (=lines)
				FSDataInputStream in = null;
				try {
					in = filesystem.open(new Path(inputData));
				} catch (IOException e) {
					log.error("Cannot open "+inputData);
				}
				LineReader lr = new LineReader(in);
				Text text = new Text();
				int total = 0;
				try {
					while(lr.readLine(text) != 0)
						total++;
				} catch (IOException e) {
					log.error("Error occured while counting number of lines of "+inputData);
				}
				//number of lines by tree
				int nbLines = (int)(baggingPercent * total);
				//random lines for each bagging data files and associated writers construction
				int[][] lines = new int[nbBagging][nbLines];
				//writers list creation
				List<FSDataOutputStream> writers = new ArrayList<FSDataOutputStream>();
				Path path = new Path(outputDir, baggingDir);
				//inputData without ".data"
				String data = inputData.substring(inputData.lastIndexOf(File.separatorChar)+1, inputData.lastIndexOf('.'));
				for(int i=0 ; i<nbBagging ; i++){
					lines[i] = Util.getSortedRandomIntList(nbLines, total);
					FSDataOutputStream fw = null;
					try {
						Path p = new Path(path, data+i+".data");
						fw = filesystem.create(p);
						res.add(p.toString());
					} catch (IOException e) {
						log.error("Error occured while files creation : "+e.getMessage());
					}
					writers.add(fw);
				}
				//construction of map<Line, List<Writer>> to optimize file reading
				//warning : remove duplicate lines in a same file
				Map<Integer, List<FSDataOutputStream>> map = new HashMap<Integer, List<FSDataOutputStream>>(total);
				for(int i=0 ; i<nbBagging ; i++){
					for(int j=0 ; j<nbLines ; j++){
						int key = lines[i][j];
						if(map.get(key)==null)
							map.put(key, new ArrayList<FSDataOutputStream>());
						map.get(key).add(writers.get(i));
					}
				}
				//raz inputData
				try {
					in = filesystem.open(new Path(inputData));
					lr = new LineReader(in);
				} catch (IOException e) {
					log.error("Error occured while raz of data file : "+e.getMessage());
				}
				//data files writing
				Text txt = new Text();
				int lind = 0;
				try {
					while(lr.readLine(txt) != 0){
						List<FSDataOutputStream> list = map.get(lind++);
						if(list!=null){
							Iterator<FSDataOutputStream> it2 = list.iterator();
							while(it2.hasNext())
								it2.next().writeBytes(txt.toString()+"\n");
						}
					}
				} catch (IOException e) {
					log.error("Error occured while reading "+inputData);
				}
				//close everything
				try {
					in.close();
					for(FSDataOutputStream w : writers){
						w.flush();
						w.close();
					}
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
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
			String[] str = check.toString().split("\n");
			for(String s : str)
				log.info(s);
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
