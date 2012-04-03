package fr.insarennes.fafdti;

import java.util.Date;

public class Chrono {
	private Date startTime;
	private long passedTimeInMilliseconds;
	
	public Chrono(){
		passedTimeInMilliseconds = 0;
	}
	
	public void start(){
		startTime = new Date();
	}
	
	public void stop() throws FAFException{
		Date stopTime = new Date();
		if(startTime==null)
			throw new FAFException("start() chrono before stop() it !");
		passedTimeInMilliseconds = stopTime.getTime() - startTime.getTime();
	}
	
	public void raz(){
		passedTimeInMilliseconds = 0;
	}
	
	public long getTimeInSeconds(){
		return passedTimeInMilliseconds / 1000;
	}
	
	public long getTimeInMinutes(){
		return getTimeInSeconds() / 60;
	}
}
