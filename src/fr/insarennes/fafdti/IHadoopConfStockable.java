package fr.insarennes.fafdti;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;

public interface IHadoopConfStockable {
	public void toConf(Configuration conf, String keySuffix)
			throws IOException;
	public void toConf(Configuration conf) throws IOException;
}
