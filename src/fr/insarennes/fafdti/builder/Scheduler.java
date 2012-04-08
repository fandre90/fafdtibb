package fr.insarennes.fafdti.builder;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class Scheduler{
	
	private final static int DEFAULT_POOL_SIZE = 50;
	public final static ThreadPoolExecutor INSTANCE = (ThreadPoolExecutor)Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	
	//make it before first call of Scheduler.INSTANCE.execute(...)
	public static void setPoolSize(int new_size){
		INSTANCE.setCorePoolSize(new_size);
	}
	
	public static int getPoolSize(){
		return INSTANCE.getCorePoolSize();
	}
}