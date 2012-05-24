package fr.insarennes.fafdti.builder.namesinfo;

import fr.insarennes.fafdti.FAFException;

public enum AttrType {
	CONTINUOUS, TEXT, DISCRETE;
	
	public static AttrType getFromString(String type) throws FAFException{
		if(type.equals("CONTINUOUS"))
			return CONTINUOUS;
		else if(type.equals("TEXT"))
			return TEXT;
		else if(type.equals("DISCRETE"))
			return DISCRETE;
		else
			throw new FAFException("ERROR : Ask AttrType from unknown String");
	}
}
