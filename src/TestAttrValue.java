import static org.junit.Assert.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.Question;




public class TestAttrValue {
	
	Question attrValCont;
	Question attrValDis;
	Question attrValText;
	@Before
	public void setUp() throws Exception {
		this.attrValCont = new Question(0, AttrType.CONTINUOUS, 50);
		this.attrValDis = new Question(0, AttrType.DISCRETE, "toto");
		this.attrValText= new Question(0, AttrType.TEXT, "toto");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAttrValueIntAttrTypeDouble() {
		assertEquals(attrValCont.getType(), AttrType.CONTINUOUS);
		assertEquals(attrValCont.getDoubleValue(), 50, 10e-6);
	}

	@Test
	public void testAttrValueIntAttrTypeString() {
		assertEquals(this.attrValDis.getType(), AttrType.DISCRETE);
		assertEquals(this.attrValDis.getTextValue(), "toto");
		assertEquals(this.attrValText.getType(), AttrType.TEXT);
		assertEquals(this.attrValText.getTextValue(), "toto");
	}

	@Test
	public void testReadWriteFields() throws IOException {
		this.testReadWriteFields(this.attrValCont);
		this.testReadWriteFields(this.attrValDis);
		this.testReadWriteFields(this.attrValText);
	}
	
	public void testReadWriteFields(Question attrValueToSerialize) throws IOException {
		Question attrValue = new Question();
		DataOutputBuffer out = new DataOutputBuffer();
		attrValueToSerialize.write(out);
		DataInputBuffer in = new DataInputBuffer();
		in.reset(out.getData(), out.getLength());
		//assertEquals(attrValue, attrValueToSerialize);
	}

	@Test
	// TODO : Document special behavious here
	public void testCompareTo() {
		Question attrValueCont2 = new Question(0, AttrType.CONTINUOUS, 60.0);
		assertEquals(attrValueCont2.compareTo(this.attrValCont), 0);
		assertEquals(this.attrValCont.compareTo(attrValueCont2), 0);
		Question attrValueCont3 = new Question(1, AttrType.CONTINUOUS, 50.0);
		assertFalse(attrValueCont3.compareTo(this.attrValCont) == 0);
		Question attrValueText2 = new Question(0, AttrType.TEXT, "toto");
		assertEquals(this.attrValText.compareTo(attrValueText2), 0);
		Question attrValueText3 = new Question(0, AttrType.TEXT, "titi");
		assertFalse(this.attrValText.compareTo(attrValueText3) == 0);
		Question attrValueDis2 = new Question(0, AttrType.DISCRETE, "toto");
		assertEquals(this.attrValDis.compareTo(attrValueDis2),0);
		Question attrValueDis3 = new Question(0, AttrType.DISCRETE, "tata");
		assertFalse(this.attrValDis.compareTo(attrValueDis3) == 0);
	}

	@Test
	public void testStringSerialization() {
		this.testStringSerialization(this.attrValCont);
		this.testStringSerialization(this.attrValDis);
		this.testStringSerialization(this.attrValText);
	}

	public void testStringSerialization(Question attrValueToSerialize) {
		String strRepr = attrValueToSerialize.toString();
		Question attrValue = new Question();
		attrValue.fromString(strRepr);
		assertEquals(attrValueToSerialize, attrValue);
	}
}
