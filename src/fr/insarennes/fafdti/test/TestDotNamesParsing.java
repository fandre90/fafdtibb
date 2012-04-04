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
import fr.insarennes.fafdti.builder.TextAttrSpec;
import static fr.insarennes.fafdti.builder.AttrType.*;
import static fr.insarennes.fafdti.builder.gram.GramType.*;

@RunWith(Parameterized.class)
public class TestDotNamesParsing {

	private Configuration conf;
	private FileSystem fileSystem;
	private String dotNamesFileName;
	private AttrType[] attrTypesArray;
	private TextAttrSpec[] textAttrArray;
	private DotNamesInfo dotNamesInfo;

	public TestDotNamesParsing(String dotNamesFileName,
			AttrType[] attrTypesArray, TextAttrSpec[] textAttrArray) {
		super();
		System.out.println("Testing file: " + dotNamesFileName);
		this.dotNamesFileName = dotNamesFileName;
		this.attrTypesArray = attrTypesArray;
		this.textAttrArray = textAttrArray;
	}

	@Before
	public void setUp() throws IOException, ParseException {
		conf = new Configuration();
		fileSystem = FileSystem.get(conf);
		dotNamesInfo = new DotNamesInfo(
				getResourcePath(dotNamesFileName), fileSystem);
	}
	
	public Path getResourcePath(String fileName) {
		URL url = this.getClass().getResource("res/" + fileName);
		System.out.println(url);
		return new Path(url.getPath());
	}

	@Parameters
	public static Collection getData() {
		return Arrays.asList(
			new Object[][] {
					{"adult.names", 
					  new AttrType[] {CONTINUOUS, DISCRETE, CONTINUOUS, DISCRETE,
						CONTINUOUS, DISCRETE, DISCRETE, DISCRETE, DISCRETE,
						DISCRETE, CONTINUOUS, CONTINUOUS, CONTINUOUS, DISCRETE},
						new TextAttrSpec[]{}
						
					},
					{"reuters.names",
				      new AttrType[] {TEXT},
				      new TextAttrSpec[] {new TextAttrSpec(NGRAM, 1, 1)},
					},
			});
	}

	@Test
	public void testAttrTypes() {
		int iTxt = 0;
		AttrType curType = null;
		for(int i=0; i<attrTypesArray.length; ++i) {
			curType = dotNamesInfo.getAttrSpec(i).getType();
			assertEquals(curType, attrTypesArray[i]);
			if(curType == TEXT) {
				TextAttrSpec curTextAttr = (TextAttrSpec) dotNamesInfo.getAttrSpec(i);
				TextAttrSpec testTextAttr = textAttrArray[iTxt];
				assertEquals(curTextAttr.getExpertLength(), 
						testTextAttr.getExpertLength());
				assertEquals(curTextAttr.getExpertLevel(), 
						testTextAttr.getExpertLevel());
				assertEquals(curTextAttr.getExpertType(), 
						testTextAttr.getExpertType());
				iTxt++;
			}
		}
	}

}
