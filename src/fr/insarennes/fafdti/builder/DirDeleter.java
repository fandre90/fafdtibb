package fr.insarennes.fafdti.builder;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;
/** autostart thread
 * 
 * @author momo
 *
 */
public class DirDeleter extends Thread {
	protected static Logger log = Logger.getLogger(DirDeleter.class);
	private Path path;
	public DirDeleter(Path path){
		super();
		this.path = path;
		this.start();
	}
	
	public void run(){
		FileSystem fs = null;
		boolean success = false;
		try {
			fs = FileSystem.get(new Configuration());
			success = fs.delete(this.path, true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(!success)
			log.warn("Unable to delete dir : "+this.path.toString());
		else
			log.info("DirDeleter deletes : "+this.path.toString());
	}
}
