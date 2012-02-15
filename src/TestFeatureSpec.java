import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.soap.Text;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.insarennes.fafdti.builder.AttrSpec;
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.FeatureSpec;
import fr.insarennes.fafdti.builder.ParseException;
import fr.insarennes.fafdti.builder.TextAttrSpec;



public class TestFeatureSpec {

	private FileSystem fs;
	private String resDir = "/home/fabien/Bureau/Hadoop/fafdtibb/res/examples/";

	@Before
	public void setUp() throws Exception {
		this.fs = FileSystem.get(new Configuration());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFeatureSpec() throws IOException, ParseException {
		// ester.names
		FeatureSpec featureSpec = new FeatureSpec(
				new Path(resDir + "ester.names"), fs);
		assertEquals(featureSpec.getLabels()[5], "loc-I");
		assertEquals(featureSpec.getLabels()[0], "amount-B");
		assertEquals(featureSpec.getLabels()[2], "fonc-B");
		TextAttrSpec textAttrSpec = (TextAttrSpec) featureSpec.getAttrSpec(0);
		assertEquals(textAttrSpec.getExpertLength(), 2);
		assertEquals(textAttrSpec.getExpertLevel(), 2);
		assertEquals(textAttrSpec.getType(), AttrType.TEXT);
		AttrSpec attrSpec = featureSpec.getAttrSpec(1);
		assertEquals(attrSpec.getType(), AttrType.CONTINUOUS);
		// horse-colic.names
		featureSpec = new FeatureSpec(
				new Path(resDir + "horse-colic.names"), fs);
		assertEquals(featureSpec.getLabels()[0], "1");
		assertEquals(featureSpec.getLabels()[1], "2");
		attrSpec = featureSpec.getAttrSpec(0);
		assertEquals(attrSpec.getType(), AttrType.DISCRETE);
		attrSpec = featureSpec.getAttrSpec(5);
		assertEquals(attrSpec.getType(), AttrType.CONTINUOUS);
		// petits-textes.names
		featureSpec = new FeatureSpec(
				new Path(resDir + "petits-textes.names"), fs);
		assertEquals(featureSpec.getLabels()[0], "politique");
		assertEquals(featureSpec.getLabels()[1], "sport");
		textAttrSpec = (TextAttrSpec) featureSpec.getAttrSpec(0);
		assertEquals(textAttrSpec.getExpertLength(), 5);
		System.out.println(textAttrSpec.getExpertLevel());
		assertEquals(textAttrSpec.getExpertLevel(), 2);
	}
}
