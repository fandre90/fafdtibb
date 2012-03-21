package fr.insarennes.fafdti.builder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Scheduler{
	
	private static int POOL_SIZE = 50;
	public final static ExecutorService INSTANCE = Executors.newFixedThreadPool(POOL_SIZE);

	//make it before first call of Scheduler.INSTANCE
	public static void setPoolSize(int new_size){
		POOL_SIZE = new_size;
	}
}