package fr.insarennes.fafdti.builder.nodebuilder;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper.Context;

import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.namesinfo.TextAttrSpec;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.gram.SGram;


public class GramGenerator {
	
	public static Set<FGram> generateNFGramForSize(int size, 
		String[] words)
				throws IOException, InterruptedException {
		Set<FGram> fGramSet = new HashSet<FGram>();
		for(int i=0; i<words.length; ++i) {
			String[] gramWords = new String[size];
			if(i + size - 1 < words.length) {
				for(int j=0; j < size; j++) {
					gramWords[j] = words[i + j];
				}
				fGramSet.add(new FGram(gramWords));
			}
		}
		return fGramSet;
	}
	
	public static Set<FGram> generateAllNFGram(TextAttrSpec textAttr,
			String[] words) throws IOException, InterruptedException {
		int minSize = 1;
		int maxSize = textAttr.getExpertLength();
		if(textAttr.getExpertType() == GramType.FGRAM) {
			minSize = maxSize;
		}
		Set<FGram> globFGramSet = new HashSet<FGram>();
		for(int size = minSize; size <= maxSize; ++size) {
			Set<FGram> fGramSet = generateNFGramForSize(size,
					words);
			globFGramSet.addAll(fGramSet);
		}
		return globFGramSet;
	}

	public static Set<SGram> generateSGram(TextAttrSpec textAttr,
			String[] words) 
					throws IOException, InterruptedException {
		int expLen = textAttr.getExpertLength();
		Set<SGram> sGramSet = new HashSet<SGram>();
		for(int dist = 0; dist <= expLen; ++dist) {
			for(int i=0; i< words.length - dist - 1; ++i) {
				String firstWord = words[i];
				String lastWord = words[i + dist + 1];
				sGramSet.add(new SGram(firstWord, lastWord, expLen));
			}
		}
		return sGramSet;
	}
}
