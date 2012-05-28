package fr.insarennes.fafdti.builder.gram;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import fr.insarennes.fafdti.builder.Question;

public class GramContainer implements WritableComparable<GramContainer>, Cloneable {
	private FGram fGram;
	private SGram sGram;
	private GramType type = null;

	public static final String DELIMITER = ";";
	
	public GramContainer() {
		this.sGram = new SGram();
		this.fGram = new FGram();
	}

	public GramContainer(FGram fGram) {
		this();
		this.set(fGram);
	}

	public GramContainer(SGram sGram) {
		this();
		this.set(sGram);
	}

	public void set(SGram sGram) {
		this.type = GramType.SGRAM;
		this.sGram = (SGram) sGram.cloneGram();
	}
	
	public void set(FGram fGram) {
		this.type = GramType.FGRAM;
		this.fGram = (FGram) fGram.cloneGram();
	}

	public FGram getfGram() {
		return fGram;
	}

	public SGram getsGram() {
		return sGram;
	}

	public Gram getGram() {
		if(this.type == null) {
			return null;
		}
		switch (this.type) {
		case FGRAM:
			return this.fGram;
		case SGRAM:
			return this.sGram;
		}
		return null;
	}

	public GramType getType() {
		return type;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		char typeChar = in.readChar();
		this.type = charToType(typeChar);
		switch (this.type) {
		case FGRAM:
			this.fGram.readFields(in);
			break;
		case SGRAM:
			this.sGram.readFields(in);
			break;
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		char typeChar = typeToChar(this.type);
		out.writeChar(typeChar);
		switch(this.type) {
		case FGRAM:
			this.fGram.write(out);
			break;
		case SGRAM:
			this.sGram.write(out);
			break;
		}
	}

	private char typeToChar(GramType type) {
		switch (type) {
		case FGRAM:
			return 'F';
		case SGRAM:
			return 'S';
		default:
			throw new IllegalArgumentException("Unsupported Gram type "
					+ this.type);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GramContainer other = (GramContainer) obj;
		return this.compareTo(other) == 0;
	}
	
	@Override
	public Object clone() {
	    GramContainer newGramContainer = null;
	    try {
	    	newGramContainer = (GramContainer) super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	cnse.printStackTrace(System.err);
	    }
	    switch(this.type) {
	    case SGRAM:
	    	newGramContainer.sGram = (SGram) this.sGram.cloneGram();
	    	break;
	    case FGRAM:
	    	newGramContainer.fGram = (FGram) this.fGram.cloneGram();
	    	break;
	    }
	    return newGramContainer;
	}

	@Override
	public int hashCode() {
		int result = 1;
		if(this.type != null) {
			switch(this.type) {
			case FGRAM:
				result = (this.fGram.hashCode() << 1);
				break;
			case SGRAM:
				result = (this.sGram.hashCode() << 1);
				break;
			}
		}
		return result;
	}
	
	private GramType charToType(char typeChar) {
		switch (typeChar) {
		case 'F':
			return GramType.FGRAM;
		case 'S':
			return GramType.SGRAM;
		default:
			throw new IllegalArgumentException(
					"Invalid Gram description character " + typeChar);
		}
	}

	public boolean query(String textData) {
		if (this.type == null) {
			throw new UnsupportedOperationException(
					"GramConainer is unitialized !");
		}
		switch (this.type) {
		case FGRAM:
			return this.fGram.query(textData);
		case SGRAM:
			return this.sGram.query(textData);
		}
		return false;
	}

	@Override
	public int compareTo(GramContainer other) {
		int comparison = this.type.compareTo(other.type);
		if(comparison != 0) {
			return comparison;
		}
		switch(this.type) {
		case FGRAM:
			return this.fGram.compareTo(other.fGram);
		case SGRAM:
			return this.sGram.compareTo(other.sGram);
		}
		return 0;
	}

	@Override
	public String toString() {
		String strRepr = "";
		char typeChar = typeToChar(this.type);
		strRepr += typeChar + DELIMITER;
		switch(typeChar) {
		case 'F':
			strRepr += fGram.toString();
			break;
		case 'S':
			strRepr += sGram.toString();
			break;
		}
		return strRepr;
	}
	
	public void fromString(String strRepr) {
		String[] fields = strRepr.split(DELIMITER, 2);
		this.type = charToType(fields[0].charAt(0));
		switch(this.type) {
		case FGRAM:
			this.fGram.fromString(fields[1]);
			break;
		case SGRAM:
			this.sGram.fromString(fields[1]);
			break;
		}
	}
}
