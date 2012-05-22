package fr.insarennes.fafdti.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.builder.DotNamesInfo;
import fr.insarennes.fafdti.builder.ParseException;

public class UtilsTest {
	
	public static DataOutput bufferToDataOutput(ByteArrayOutputStream buffer) {
		return new DataOutputStream(buffer);
	}
	
	public static DataInput bufferToDataInput(ByteArrayOutputStream buffer) {
		return new DataInputStream(new ByteArrayInputStream(
				buffer.toByteArray()));
	}
	
	public static Configuration generateConfiguration(String filename) 
			throws ParseException, IOException {
		Path path = new Path(filename);
		Configuration conf = new Configuration();
		DotNamesInfo dotNames = new DotNamesInfo(path, FileSystem.get(conf));
		dotNames.toConf(conf);
		return conf;
	}
}
