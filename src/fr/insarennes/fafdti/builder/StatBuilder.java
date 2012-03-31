package fr.insarennes.fafdti.builder;

import java.util.Observable;

import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;

public class StatBuilder extends Observable{
	
	private static Logger log = Logger.getLogger(StatBuilder.class);
			
	private int nbPendingLeft;
	private int totalEx;
	private int nbExClassified;
	
	public StatBuilder(int nbPendingLeft){
		super();
		this.nbPendingLeft = nbPendingLeft;
		this.totalEx = 0;
		this.nbExClassified = 0;
	}
	
	synchronized public int getNbPending(){
		return nbPendingLeft;
	}
	
	synchronized public void incrementPending(){
		nbPendingLeft++;
		this.setChanged();
	}
	
	synchronized public void decrementPending(){
		nbPendingLeft--;
		this.setChanged();
		this.notifyObservers(new Integer(nbPendingLeft));			
	}
	
	synchronized public void setTotalEx(int tot){
		totalEx = tot;
	}
	
	synchronized public void addExClassified(int nbEx) throws FAFException{
		nbExClassified += nbEx;
		if(totalEx==0)
			throw new FAFException("Set total before add examples classified");
		log.info(((double)nbExClassified / (double)totalEx)*100.0+"% examples classified");
	}

}
