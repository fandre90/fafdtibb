package fr.insarennes.fafdti.builder;


public class TextAttrSpec extends AttrSpec {

	private int expertLevel;
	private int expertLength;
	private GramType expertType;

	public TextAttrSpec(GramType expertType, int expertLength, int expertLevel) {
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
