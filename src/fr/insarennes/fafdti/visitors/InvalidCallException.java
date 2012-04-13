/** Exception levée lorsqu'une méthode de visite est appelée alors que
 * le visiteur ne peut pas être appelé sur ce type d'arbre (Pending notamment)
 * 
 */

package fr.insarennes.fafdti.visitors;

import fr.insarennes.fafdti.FAFException;

public class InvalidCallException extends FAFException {
	
	InvalidCallException(){super();}
	InvalidCallException(String msg){super(msg);}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
