/**
 * Classe représentant l'objet commun que possède tous les threads d'un même arbre
 * C'est un objet partagé avec des accès concurrent qui permet entre autre de savoir
 * quand est-ce que le processus de construction est totalement terminé.
 * Il permet aussi de récupérer un identifiant unique dans l'arbre ou encore de
 * savoir quel pourcentage d'exemples a déjà été classifié.
 */

package fr.insarennes.fafdti.builder;

import java.util.Observable;

import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;

public class StatBuilder extends Observable{
	
	private static Logger log = Logger.getLogger(StatBuilder.class);
			
	private int nbPendingLeft;
	private int totalEx;
	private int nbExClassified;
	private int id;
	
	public StatBuilder(int nbPendingLeft){
		super();
		this.nbPendingLeft = nbPendingLeft;
		this.totalEx = 0;
		this.nbExClassified = 0;
		this.id = -1;
	}
	
	/**
	 * 
	 * @return un identifiant unique dans l'arbre
	 */
	synchronized int getNextId(){
		return ++id;
	}
	
	/**
	 * 
	 * @return le nombre de sous-arbre encore en construction
	 */
	synchronized public int getNbPending(){
		return nbPendingLeft;
	}
	
	/**
	 * Incrémente le nombre de sous-arbre encore en construction et notifie ces Observers
	 */
	synchronized public void incrementPending(){
		nbPendingLeft++;
		this.setChanged();
	}
	
	/**
	 * Décrémente le nombre de sous-arbre encore en construction et notifie ces Observers
	 */
	synchronized public void decrementPending(){
		nbPendingLeft--;
		this.setChanged();
		this.notifyObservers(new Integer(nbPendingLeft));			
	}
	
	/** Méthode à n'appeler qu'une fois pour initialiser le nombre total d'exemples à classer
	 * @param tot le nombre total d'exemples à classer
	 */
	synchronized public void setTotalEx(int tot){
		totalEx = tot;
	}
	/** Met à jour le nombre d'exemples classer
	 * @param nbEx le nombre d'exemples classer en plus
	 * @throws FAFException si {@link #totalEx} est égal à zéro
	 */
	synchronized public void addExClassified(int nbEx) throws FAFException{
		nbExClassified += nbEx;
		if(totalEx==0)
			throw new FAFException("Set total before add examples classified");
		log.info(((double)nbExClassified / (double)totalEx)*100.0+"% examples classified");
	}

}
