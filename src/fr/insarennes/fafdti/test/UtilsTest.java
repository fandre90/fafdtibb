package fr.insarennes.fafdti.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;

public class UtilsTest {
	
	public static DataOutput bufferToDataOutput(ByteArrayOutputStream buffer) {
		return new DataOutputStream(buffer);
	}
	
	public static DataInput bufferToDataInput(ByteArrayOutputStream buffer) {
		return new DataInputStream(new ByteArrayInputStream(
				buffer.toByteArray()));
	}
	
}
