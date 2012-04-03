package fr.insarennes.fafdti.builder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Scheduler{
	
	private final static int DEFAULT_POOL_SIZE = 50;
	public static ExecutorService INSTANCE = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	
	//make it before first call of Scheduler.INSTANCE
	public static void setPoolSize(int new_size){
		INSTANCE = Executors.newFixedThreadPool(new_size);
	}
}