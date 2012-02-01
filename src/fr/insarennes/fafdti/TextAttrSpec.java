package fr.insarennes.fafdti;

public class TextAttrSpec extends AttrSpec {
	
	public enum ExpertType {
		NGRAM
	}

	private int expertLevel;
	private int expertLength;
	private ExpertType expertType;

	public TextAttrSpec(ExpertType expertType, int expertLength, int expertLevel) {
		this.expertType = expertType;
		this.expertLength = expertLength;
		this.expertLevel = expertLevel;
		this.type = AttrType.TEXT;
	}

	public int getExpertLength() {
		return this.expertLength;
	}

	public int getExpertLevel() {
		return this.expertLevel;
	}
}
