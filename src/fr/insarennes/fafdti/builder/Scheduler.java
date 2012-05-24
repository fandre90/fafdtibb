package fr.insarennes.fafdti.builder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
/** Classe encapsulant un pool de thread permettant de contrôler le nombre maximum
 * de thread s'exécutant dans la jvm.
 */

public class Scheduler{
	
	private final static int DEFAULT_POOL_SIZE = 50;
	/** le singleton */
	public final static ThreadPoolExecutor INSTANCE = (ThreadPoolExecutor)Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	
	/** Methode permettant de modifier la taille par défaut du pool
	 * Attention : à appeler avant le premier appel de Scheduler.INSTANCE.execute(...)
	 * 
	 * @param new_size la nouvelle taille
	 */
	public static void setPoolSize(int new_size){
		INSTANCE.setCorePoolSize(new_size);
	}
	
	public static int getPoolSize(){
		return INSTANCE.getCorePoolSize();
	}
}