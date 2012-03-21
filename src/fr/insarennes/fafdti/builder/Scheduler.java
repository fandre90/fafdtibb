package fr.insarennes.fafdti.builder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Scheduler{
	
	private static final int POOL_SIZE = 50;
	public static final ExecutorService INSTANCE = Executors.newFixedThreadPool(POOL_SIZE);
}

//import java.util.ArrayList;
//import java.util.List;
//
//public class Scheduler extends Thread {
// 	public static final Scheduler INSTANCE = new Scheduler();
//
//	private List<Runnable> fifo;
//	private List<Runnable> runningList;
//	private boolean running;
//	private static final int MAX_RUNNING_TASKS = 50;
//	
//	
//	private Scheduler(){
//		fifo = new ArrayList<Runnable>();
//		runningList = new ArrayList<Runnable>();
//		running = true;
//	}
//	
//	public void execute(Runnable task){
//		fifo.add(task);
//	}
//	
//	public void stopMe(){
//		running = false;
//	}
//	
//	public void done(Runnable task){
//		runningList.remove(task);
//	}
//	
//	public boolean everythingIsDone(){
//		return fifo.isEmpty() && runningList.isEmpty();
//	}
//	
//	public void run(){
//		while(running){
//			if(!fifo.isEmpty() && runningList.size()<MAX_RUNNING_TASKS){
//				Runnable task = fifo.get(0);
//				Thread thread = new Thread(task);
//				thread.start();
//				runningList.add(task);
//				fifo.remove(0);	
//			}
//		}
//	}
//}
