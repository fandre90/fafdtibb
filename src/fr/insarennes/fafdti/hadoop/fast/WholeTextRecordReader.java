package fr.insarennes.fafdti.hadoop.fast;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.lib.CombineFileSplit;

public class WholeTextRecordReader implements RecordReader<NullWritable, Text> {

	private CombineFileSplit combineFileSplit;
	private Configuration conf;
	private boolean processed = false;
	private FileSystem fileSystem;
	
	public WholeTextRecordReader(CombineFileSplit combineFileSplit,
			Configuration conf) throws IOException {
		this.combineFileSplit = combineFileSplit;
		this.conf = conf;
		this.fileSystem = FileSystem.get(conf);
	}

	@Override
	public void close() throws IOException {
		// We need to do nothing clear as we close
		// stream
	}

	@Override
	public NullWritable createKey() {
		return NullWritable.get();
	}

	@Override
	public Text createValue() {
		return new Text();
	}

	@Override
	public long getPos() throws IOException {
		return processed ? combineFileSplit.getLength() : 0;
	}

	@Override
	public float getProgress() throws IOException {
		return processed ? 1.0f : 0.0f;
	}

	@Override
	public boolean next(NullWritable key, Text value) throws IOException {
		if (!processed) {
			byte[] contents = new byte[(int) combineFileSplit.getLength()];
			int off = 0;
			for (Path file : combineFileSplit.getPaths()) {
				FileSystem fs = file.getFileSystem(conf);
				FSDataInputStream in = null;
				try {
					int len = (int) fileSystem.getFileStatus(file).getLen();
					in = fs.open(file);
					IOUtils.readFully(in, contents, off, len);
					off += len;
				} finally {
					IOUtils.closeStream(in);
				}
			}
			value.set(contents, 0, contents.length);
			processed = true;
			return true;
		}
		return false;
	}
}