import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.DepthMax;
import fr.insarennes.fafdti.builder.EntropyCriterion;
import fr.insarennes.fafdti.builder.ExampleMin;
import fr.insarennes.fafdti.builder.GainMin;
import fr.insarennes.fafdti.builder.NodeBuilder;
import fr.insarennes.fafdti.builder.Scheduler;
import fr.insarennes.fafdti.builder.StoppingCriterion;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeHolder;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.visitors.Checker;
import fr.insarennes.fafdti.visitors.StatNumExamplesClassified;
import fr.insarennes.fafdti.visitors.XmlExporter;

public class TestNodeBuilder {
	public static void main(String[] args) throws FAFException {
		DecisionTreeHolder root = new DecisionTreeHolder();
		//String input = "/home/fabien/Bureau/Hadoop/data_test/in/test1";
		String input = "/home/momo/workspace/FaF/res/examples/exp";
		String inputNames = input + ".names";
		String inputData = input + ".data";
		Formatter format = new Formatter();
		String outputDir0 = "/home/momo/workspace/FaF/res/out/"
				+ format.format("%1$tY-%1$tm-%1$td %1$tHh%1$tM", new Date());
		
		List<StoppingCriterion> stopping = new ArrayList<StoppingCriterion>();
		stopping.add(new DepthMax(5));
		stopping.add(new ExampleMin(2));
		stopping.add(new GainMin(0.1));
		NodeBuilder nb = new NodeBuilder(inputNames, inputData, outputDir0,
				new EntropyCriterion(), root.getNodeSetter(), stopping);
		
		Scheduler scheduler = Scheduler.INSTANCE;
		scheduler.execute(nb);
		scheduler.start();
		while(scheduler.isAlive()){
			//affichage des stats
			StatNumExamplesClassified stat = new StatNumExamplesClassified();
			root.getRoot().accept(stat);
			List<DecisionTree> pending = stat.getPending();
			int sum = stat.getResult();
			while(!pending.isEmpty()){
				System.out.println(sum+" examples classified");
				List<DecisionTree> tmp = new ArrayList<DecisionTree>();
				for(DecisionTree dt : pending){
					StatNumExamplesClassified st = new StatNumExamplesClassified();
					dt.accept(st);
					sum+=st.getResult();
					tmp.addAll(st.getPending());
				}
				pending = tmp;
			}
		}
		//check
		Checker check = new Checker();
		root.getRoot().accept(check);
		if(!check.checkOK()){
			System.out.println("construction failed : pending found");
			System.exit(1);
		}
		//export xml
		String outxml = "/home/momo/workspace/FaF/res/xml/exp";
		XmlExporter xml = new XmlExporter(root.getRoot(), outxml);
		xml.launch();
	}
}
