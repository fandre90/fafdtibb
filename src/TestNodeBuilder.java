import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.Launcher;
import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.EntropyCriterion;
import fr.insarennes.fafdti.builder.NodeBuilder;
import fr.insarennes.fafdti.builder.Scheduler;
import fr.insarennes.fafdti.builder.StatBuilder;
import fr.insarennes.fafdti.builder.stopcriterion.DepthMax;
import fr.insarennes.fafdti.builder.stopcriterion.ExampleMin;
import fr.insarennes.fafdti.builder.stopcriterion.GainMin;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.visitors.Checker;
import fr.insarennes.fafdti.visitors.StatNumExamplesClassified;
import fr.insarennes.fafdti.visitors.XmlExporter;

public class TestNodeBuilder {
	
	
	public static void main(String[] args) throws FAFException {
		//String input = "/home/fabien/Bureau/Hadoop/data_test/in/test1";
		String input = "/home/momo/workspace/FaF/res/examples/iris";
		String inputNames = input + ".names";
		String inputData = input + ".data";
		Formatter format = new Formatter();
		String outputDir0 = "/home/momo/workspace/FaF/res/out/"
				+ format.format("%1$tY-%1$tm-%1$td %1$tHh%1$tM", new Date());
		String outxml = "/home/momo/workspace/FaF/res/xml/iris";
		//stopping criterion
		List<StoppingCriterion> stopping = new ArrayList<StoppingCriterion>();
		stopping.add(new DepthMax(5));
		stopping.add(new ExampleMin(1));
		stopping.add(new GainMin(0.1));
		new Launcher(inputNames, inputData, outputDir0, outxml, stopping, new EntropyCriterion(), 1, 0.6);
//		scheduler.start();
//		while(scheduler.isAlive());
//		{
//			//affichage des stats
//			StatNumExamplesClassified stat = new StatNumExamplesClassified();
//			try{
//				root.getRoot().accept(stat);
//			}catch(FAFException e){
//				;
//			}
//			List<DecisionTree> pending = stat.getPending();
//			int sum = stat.getResult();
//			while(!pending.isEmpty()){
//				//System.out.println(sum+" examples classified");
//				List<DecisionTree> tmp = new ArrayList<DecisionTree>();
//				for(DecisionTree dt : pending){
//					StatNumExamplesClassified st = new StatNumExamplesClassified();
//					dt.accept(st);
//					sum+=st.getResult();
//					tmp.addAll(st.getPending());
//				}
//				pending = tmp;
//			}
//		}
	}

}
