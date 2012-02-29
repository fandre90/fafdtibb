package fr.insarennes.fafdti.builder;

import java.io.IOException;
import java.io.Serializable;

import org.apache.hadoop.conf.Configuration;

// Static Java Interface
// http://stackoverflow.com/questions/512877/why-cant-i-define-a-static-method-in-a-java-interface

// Static abstract
// http://stackoverflow.com/questions/370962/why-cant-static-methods-be-abstract-in-java
// http://stackoverflow.com/questions/1916019/java-abstract-static-workaround

// Static + Generics
// http://stackoverflow.com/questions/936377/static-method-in-a-generic-class

public abstract class HadoopConfStockable implements Serializable {

	// public static <T> T fromConf(Configuration conf, String keySuffix);

	public abstract void toConf(Configuration conf, String keySuffix)
			throws IOException;

	// public static <T> T fromConf(Configuration conf) {
	// return fromConf(conf, "");
	// }

	public void toConf(Configuration conf) throws IOException {
		toConf(conf, "");
	}
}
