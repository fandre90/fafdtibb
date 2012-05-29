package fr.insarennes.fafdti.builder.scheduler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.ClusterStatus;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.builder.treebuilder.TreeBuilderRecursive;

public class SmartScheduler implements IScheduler {
	public final static int DEFAULT_POOL_SIZE = 50;
	public final static int INCREMENT_DIVISION_FACTOR = 2;
	private final ThreadPoolExecutor pool;
	public static final SmartScheduler INSTANCE = new SmartScheduler();
	private static Logger log = Logger.getLogger(SmartScheduler.class);
	private JobClient jobClient = null;

	private SmartScheduler() {
		Configuration conf = new Configuration();
		String tracker = conf.get("mapred.job.tracker");
		if(tracker.indexOf(':') == -1) {
			log.warn("Cannot connect to " + tracker + ". " 
				+ "Disabling smart thread pool resizing.");
		} else {
			String[] tokens = tracker.split(":");
			try {
				this.jobClient = new JobClient(new InetSocketAddress(tokens[0].trim(), 
						Integer.parseInt(tokens[1].trim())), 
					new Configuration());
			} catch (Exception e) {
				log.error("Could not connect to JobTracker to enable "
						+ "smart thread pool resizing", e);
			}
		}
		pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	}

	/** Methode permettant de modifier la taille par défaut du pool
	 * Attention : à appeler avant le premier appel de Scheduler.INSTANCE.execute(...)
	 * 
	 * @param new_size la nouvelle taille
	 */
	public void setPoolSize(int new_size) {
		pool.setCorePoolSize(new_size);
	}

	public int getPoolSize() {
		return pool.getCorePoolSize();
	}

	@Override
	public void execute(Runnable command) {
		log.info("Executing new thread");
		if(jobClient != null) {
			try {
				ClusterStatus status = jobClient.getClusterStatus();
				int freeSlots = (status.getMaxMapTasks() - status.getMapTasks());
				if(freeSlots > 1) {
					int addSlots = (freeSlots / INCREMENT_DIVISION_FACTOR);
					log.info("Free map slots : " + freeSlots 
							+ "Adding " + addSlots + " thre" );
				} else {
					log.info("No free map slots. Decrementing ");
				}
			} catch (IOException e) {
				log.error("Got exception while connecting to JobTracker to "
					+ "get cluster status", e);
			}
		}
		pool.execute(command);
	}

	public void shutdown() {
		pool.shutdown();
	}
}
