package fr.insarennes.fafdti.builder;

import fr.insarennes.fafdti.builder.gram.GramType;


public class TextAttrSpec extends AttrSpec {

	private int expertLevel;
	private int expertLength;
	private GramType expertType;


	public TextAttrSpec(GramType expertType, int expertLength, int expertLevel, String name) {
		super(name);
		this.expertType = expertType;
		this.expertLength = expertLength;
		this.expertLevel = expertLevel;
		this.type = AttrType.TEXT;
	}

	public int getExpertLength() {
		return this.expertLength;
	}
	

	public GramType getExpertType() {
		return expertType;
	}

	public void setExpertType(GramType expertType) {
		this.expertType = expertType;
	}

	public int getExpertLevel() {
		return this.expertLevel;
	}
}