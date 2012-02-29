package fr.insarennes.fafdti.builder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;

public class HadoopConfSerializer {

	public static void serializeToConf(Object obj, Configuration conf,
			String key) throws IOException {
		ByteArrayOutputStream baot = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baot);
		oos.writeObject(obj);
		oos.flush();
		String sFeatureSpec = Base64.encodeBase64String(baot.toByteArray());
		oos.close();

		conf.set(key, sFeatureSpec);
	}

	public static Object deserializeFromConf(Configuration conf,
			String key) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(
				Base64.decodeBase64(conf.get(key)));
		ObjectInputStream ois = new ObjectInputStream(bais);
		return ois.readObject();
	}
}
