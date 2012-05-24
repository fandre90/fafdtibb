package fr.insarennes.fafdti;
/** Classe mère de toutes les exceptions levées par FAFDTIBB
 */
public class FAFException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FAFException(){
		super();
	}

	public FAFException(String message) {
		super(message);
	}

}
