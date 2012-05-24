package fr.insarennes.fafdti.builder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
/** Classe encapsulant un pool de thread permettant de contrôler le nombre maximum
 * de thread s'exécutant dans la jvm.
 */

public class Scheduler implements IScheduler {
	
	private final static int DEFAULT_POOL_SIZE = 50;
	/** le singleton */
	private final ThreadPoolExecutor pool;
	
	public final Scheduler INSTANCE = new Scheduler();
	private Scheduler() {
		pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	}
	/** Methode permettant de modifier la taille par défaut du pool
	 * Attention : à appeler avant le premier appel de Scheduler.INSTANCE.execute(...)
	 * 
	 * @param new_size la nouvelle taille
	 */
	public void setPoolSize(int new_size){
		pool.setCorePoolSize(new_size);
	}
	
	public int getPoolSize(){
		return pool.getCorePoolSize();
	}

	@Override
	public void execute(Runnable command) {
		// TODO Auto-generated method stub
		
	}
}