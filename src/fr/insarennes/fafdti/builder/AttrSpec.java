package fr.insarennes.fafdti.builder;

import java.io.Serializable;
import java.util.Iterator;

import fr.insarennes.fafdti.builder.gram.GramType;

public abstract class AttrSpec implements Cloneable, Serializable {
	private static final long serialVersionUID = -2277003629169821957L;
	protected AttrType type;
	private GramType gramType;
	private int expertLevel;
	private int expertLength;
	protected String name;
	
	public AttrSpec(String name){
		this.name = name;
	}
	public AttrType getType() {
		return this.type;
	}
	public String getName(){
		return this.name;
	}

	public Object clone() {
		Object o = null;
		try {
			o = super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + expertLength;
		result = prime * result + expertLevel;
		result = prime * result
				+ ((gramType == null) ? 0 : gramType.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttrSpec other = (AttrSpec) obj;
		if (expertLength != other.expertLength)
			return false;
		if (expertLevel != other.expertLevel)
			return false;
		if (gramType != other.gramType)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	

}
