package fr.insarennes.fafdti.builder;

import java.io.Serializable;

public abstract class AttrSpec implements Cloneable, Serializable {
	private static final long serialVersionUID = -2277003629169821957L;
	public AttrType type;

	public AttrType getType() {
		return this.type;
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
		if (type != other.type)
			return false;
		return true;
	}
}
