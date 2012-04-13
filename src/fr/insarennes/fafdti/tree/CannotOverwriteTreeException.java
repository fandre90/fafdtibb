/**Classe représantant l'exception levée lorsque l'on essaie d'appeler la méthode set()
 * d'un DecisionNodeSetter alors que celle-ci à déjà été appelée
 */

package fr.insarennes.fafdti.tree;

import fr.insarennes.fafdti.FAFException;

public class CannotOverwriteTreeException extends FAFException {

	public CannotOverwriteTreeException() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CannotOverwriteTreeException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
