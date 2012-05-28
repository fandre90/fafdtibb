import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;

import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
import fr.insarennes.fafdti.builder.LabeledExample;
import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.hadoop.SplitExampleMultipleOutputFormat;
import fr.insarennes.fafdti.hadoop.fast.MapperTreeBuilder;
import fr.insarennes.fafdti.hadoop.fast.WholeTextInputFormat;
import fr.insarennes.fafdti.hadoop.furious.Step3Map;
import fr.insarennes.fafdti.hadoop.furious.Step3Red;

public class TestTreeBuilderMapper {
	public static void main(String[] args) throws ParseException, IOException {
		JobConf jobConf = new JobConf(MapperTreeBuilder.class);
		DotNamesInfo namesInfo = new DotNamesInfo(
				new Path("/home/fabien/Bureau/Hadoop/data_test/in/adult.names"),
				FileSystem.get(jobConf));
		namesInfo.toConf(jobConf);
		jobConf.setOutputKeyClass(NullWritable.class);
		jobConf.setOutputValueClass(Text.class);
		jobConf.setMapperClass(MapperTreeBuilder.class);
		jobConf.setInputFormat(WholeTextInputFormat.class);
		org.apache.hadoop.mapred.FileInputFormat.setInputPaths(jobConf,
				"/home/fabien/Bureau/Hadoop/data_test/in/adult.data");
		jobConf.setOutputFormat(SplitExampleMultipleOutputFormat.class);
		org.apache.hadoop.mapred.FileOutputFormat.setOutputPath(jobConf,
				new Path("/home/fabien/Bureau/Hadoop/data_test/out3"));
		JobClient.runJob(jobConf);
	}
}
