package fr.insarennes.fafdti.builder;

import java.util.ArrayList;
import java.util.List;

public class Scheduler extends Thread {
	public static final Scheduler INSTANCE = new Scheduler();

	private List<Runnable> fifo;
	private boolean running;
	
	
	private Scheduler(){
		fifo = new ArrayList<Runnable>();
		running = true;
	}
	
	public void execute(Runnable task){
		fifo.add(task);
	}
	
	public void stopMe(){
		running = false;
	}
	
	public void run(){
		while(running){
			if(!fifo.isEmpty()){
				int index = fifo.size() - 1;
				Runnable task = fifo.get(index);
				fifo.remove(index);
				
				Thread thread = new Thread(task);
				thread.start();	
			}
		}
	}
}
