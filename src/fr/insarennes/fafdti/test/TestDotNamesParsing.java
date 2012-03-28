package fr.insarennes.fafdti.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.ParseException;
import static fr.insarennes.fafdti.builder.AttrType.*;

@RunWith(Parameterized.class)
public class TestDotNamesParsing {
	
	private Configuration conf;
	private FileSystem fileSystem;
	private String dotNamesFileName;
	private AttrType[] attrTypesArray;
	private DotNamesInfo dotNamesInfo;
	
	public TestDotNamesParsing(String dotNamesFileName,
			AttrType[] attrTypesArray) {
		super();
		System.out.println("Testing file: " + dotNamesFileName);
		this.dotNamesFileName = dotNamesFileName;
		this.attrTypesArray = attrTypesArray;
	}

	@Before
	public void setUp() throws IOException, ParseException {
		conf = new Configuration();
		fileSystem = FileSystem.get(conf);
		dotNamesInfo = new DotNamesInfo(
				getResourcePath(dotNamesFileName), fileSystem);
	}
	
	public Path getResourcePath(String fileName) {;
		URL url = this.getClass().getResource(fileName);
		System.out.println(fileName);
		System.out.println(this.getClass().getResource(fileName));
		//Enumeration<E> urls = cl.getResources(name);
		System.out.println("URL: " + url);
		return null;
	}

	@Parameters
	public static Collection getData() {
		String bp = "/home/fabien/Bureau/Hadoop/fafdtibb/res/examples/";
		return Arrays.asList(
			new Object[][] {
					{"adult.names", 
					  new AttrType[] {CONTINUOUS, DISCRETE, CONTINUOUS, DISCRETE,
						CONTINUOUS, DISCRETE, DISCRETE, DISCRETE, DISCRETE,
						DISCRETE, CONTINUOUS, CONTINUOUS, CONTINUOUS, DISCRETE}
					},
			});
	}

	@Test
	public void testAttrTypes() {
		for(int i=0; i<attrTypesArray.length; ++i) {
			assertEquals(dotNamesInfo.getAttrSpec(i).getType(), 
					attrTypesArray[i]);
		}
	}

}
