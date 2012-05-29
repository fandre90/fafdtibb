import java.io.IOException;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.FSUtils;

public class TestFSUtils {
	public static void main(String[] args) throws IOException {
		FSUtils fsUtils = new FSUtils();
		System.out.println(fsUtils.getSize(
				new Path("/home/fabien/Bureau/Hadoop/data_test")));
	}
}
