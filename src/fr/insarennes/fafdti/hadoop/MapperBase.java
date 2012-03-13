package fr.insarennes.fafdti.hadoop;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;

import fr.insarennes.fafdti.builder.DotNamesInfo;

public class MapperBase<K1, V1, K2, V2> extends Mapper<K1, V1, K2, V2> {
	protected DotNamesInfo fs;

	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		try {
			Configuration conf = context.getConfiguration();
			fs = DotNamesInfo.fromConf(conf);
		} catch (ClassNotFoundException e) {
			// FIXME Auto-generated catch block
			// LOG ERROR MESSAGE HERE
			e.printStackTrace();
		}
	}
}
