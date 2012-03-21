package fr.insarennes.fafdti.builder;

import java.util.Observable;

public class StatBuilder extends Observable{
	
	private int nbPendingLeft;
	
	public StatBuilder(int nbPendingLeft){
		super();
		this.nbPendingLeft = nbPendingLeft;
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
		this.notifyObservers(this);			
	}

}
