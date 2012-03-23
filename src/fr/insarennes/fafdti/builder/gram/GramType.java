package fr.insarennes.fafdti.builder.gram;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.builder.AttrType;

public enum GramType {
	FGRAM, NGRAM, SGRAM;
	
	public static GramType getFromString(String type) throws FAFException{
		if(type.equals("FGRAM"))
			return FGRAM;
		else if(type.equals("NGRAM"))
			return NGRAM;
		else if(type.equals("SGRAM"))
			return SGRAM;
		else
			throw new FAFException("ERROR : Ask GramType from unknown String");
	}
}
