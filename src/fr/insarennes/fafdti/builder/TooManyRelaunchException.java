package fr.insarennes.fafdti.builder;

import fr.insarennes.fafdti.FAFException;

public class TooManyRelaunchException extends FAFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1340387189421661779L;
	public TooManyRelaunchException(){
		super();
	}
	public TooManyRelaunchException(String msg){
		super(msg);
	}
}
