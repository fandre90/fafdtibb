package fr.insarennes.fafdti.builder;

import fr.insarennes.fafdti.FAFException;

public class ParseException extends FAFException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ParseException() {
		super();
	}
	
	public ParseException(String message) {
		super(message);
	}
}
