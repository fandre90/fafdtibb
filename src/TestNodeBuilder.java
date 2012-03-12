import java.util.Date;
import java.util.Formatter;

import fr.insarennes.fafdti.builder.EntropyCriterion;
import fr.insarennes.fafdti.builder.NodeBuilder;

public class TestNodeBuilder {
	public static void main(String[] args) {
		String input = "/home/fabien/Bureau/Hadoop/data_test/in/test1";
		String inputNames = input + ".names";
		String inputData = input + ".data";
		Formatter format = new Formatter();
		String outputDir0 = "/home/fabien/Bureau/Hadoop/data_test/"
				+ format.format("%1$tY-%1$tm-%1$td %1$tHh%1$tM", new Date());
		NodeBuilder nb = new NodeBuilder(inputNames, inputData, outputDir0,
				new EntropyCriterion());
		nb.run();
	}
}
