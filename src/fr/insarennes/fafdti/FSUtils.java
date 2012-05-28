package fr.insarennes.fafdti;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

public class FSUtils {
	FileSystem fileSystem;
	protected static Logger log = Logger.getLogger(FSUtils.class);

	public FSUtils() throws IOException {
		this.fileSystem = FileSystem.get(new Configuration());
	}

	public FSUtils(FileSystem fileSystem) throws IOException {
		this.fileSystem = fileSystem;
	}

	public long getSize(Path path) throws IOException {
		return fileSystem.getFileStatus(path).getLen();
	}
	
	public Path getPartNonEmptyPath(Path inputDir) throws IOException {
		FileStatus[] files = fileSystem.listStatus(inputDir);
		for (int i = 0; i < files.length; i++) {
			Path tmp = files[i].getPath();
			if (tmp.getName().startsWith("part")
					&& getSize(tmp) > 0)
				return tmp;
		}
		return null;
	}

	public String readNonEmptyPartFirstLine(Path inputDir) throws IOException {
		FSDataInputStream in = getPartNonEmpty(inputDir);
		if (in == null)
			return "";
		LineReader lr = new LineReader(in);
		Text line = new Text();
		lr.readLine(line);
		return line.toString();
	}

	public FSDataInputStream getPartNonEmpty(Path inputDir) throws IOException {
		Path path = getPartNonEmptyPath(inputDir);
		if (path == null) {
			log.warn("No non-empty part file found");
			return null;
		}
		FSDataInputStream in = fileSystem.open(path);
		return in;
	}

	
	public void deleteDir(Path dir) {
		try {
			fileSystem.delete(dir, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
