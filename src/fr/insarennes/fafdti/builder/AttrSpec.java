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

	static class LazyDiscreteQuestionGenerator implements Iterable<Question> {
		private String attrValue;
		private int col;
	
		class LazyDiscreteQuestionIterator implements Iterator<Question> {
			private boolean wasGenerated = false;
			
			@Override
			public boolean hasNext() {
				return !wasGenerated;
			}

			@Override
			public Question next() {
				double value = Double.parseDouble(attrValue);
				wasGenerated = true;
				return new Question(col, AttrType.CONTINUOUS, value);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"Questions are generated on the fly. It is impossible to remove one");
			}
		}
		
		public LazyDiscreteQuestionGenerator(int col, String attrValue) {
			this.col = col;
			this.attrValue = attrValue;
		}
		@Override
		public Iterator<Question> iterator() {
			return new LazyDiscreteQuestionIterator();
		}
	}
	
	static class LazyTextQuestionGenerator implements Iterable<Question> {
		private String attrValue;
		private int col;
	
		class LazyDiscreteQuestionIterator implements Iterator<Question> {
			private boolean wasGenerated = false;
			
			@Override
			public boolean hasNext() {
				return !wasGenerated;
			}

			@Override
			public Question next() {
				double value = Double.parseDouble(attrValue);
				wasGenerated = true;
				return new Question(col, AttrType.CONTINUOUS, value);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException(
						"Questions are generated on the fly. It is impossible to remove one");
			}
		}
		
		public LazyTextQuestionGenerator(int col, String attrValue) {
			this.col = col;
			this.attrValue = attrValue;
		}

		@Override
		public Iterator<Question> iterator() {
			return new LazyDiscreteQuestionIterator();
		}
	}

	public Iterable<Question> generateQuestions(int col, String attrValue) {
		switch(this.type) {
		case DISCRETE:
			return new LazyDiscreteQuestionGenerator(col, attrValue);
		case TEXT:
			return new LazyTextQuestionGenerator(col, attrValue);
		}
		throw new UnsupportedOperationException("Cannot generate question for attribute type : " + this.type) ;
	}
}
