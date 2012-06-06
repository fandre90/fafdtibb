package fr.insarennes.fafdti.tree;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.builder.QuestionLabeled;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.builder.namesinfo.AttrType;
import fr.insarennes.fafdti.visitors.XmlConst;
/** Classe permettant à partir d'un arbre dans un fichier xml de générer sa
 * représentation interne sous forme de {@link DecisionTree} ainsi que de récupérer
 * les paramètres qui ont servi à sa construction
 */
public class ImportXML extends DefaultHandler{
	
	//private static Logger log = Logger.getLogger(ImportXML.class);
	private Map<String, String> buildopts;
	private BaggingTrees trees;
	private String filename;
	private Stack<DecisionTree> treeStack;
	private Stack<Boolean> answerStack;
	private Map<String,Double> labels;
	private int nbCl;
	
	public ImportXML(String filename){
		this.filename = filename;
		buildopts = new HashMap<String, String>();
		treeStack = new Stack<DecisionTree>();
		answerStack = new Stack<Boolean>();
		labels = new HashMap<String,Double>();
		nbCl = 0;
	}
	
	public void launch() throws FAFException{
		try{
			SAXParserFactory fabrique = SAXParserFactory.newInstance();
			SAXParser parseur = fabrique.newSAXParser();
	
			File fichier = new File(filename);
			DefaultHandler gestionnaire = this;
			parseur.parse(fichier, gestionnaire);
		}catch (IOException e) {
			throw new FAFException("IOException");
		} catch (SAXException e) {
			throw new FAFException("SAXException");
		} catch (ParserConfigurationException e) {
			throw new FAFException("ParserConfigurationException");
		}
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try{
			if(qName.equals(XmlConst.TREES)){
				
			}else if(qName.equals(XmlConst.BUILDOPTS)){
				for (int i = 0; i < attributes.getLength(); i++)
					buildopts.put(attributes.getQName(i), attributes.getValue(i));
			}else if(qName.equals(XmlConst.QUESTION)){
				DecisionTree res = null;
				String feat = attributes.getValue(XmlConst.FEATURE);
				String feat_name = attributes.getValue(XmlConst.FEATURE_NAME);
				AttrType type = AttrType.getFromString(attributes.getValue(XmlConst.TYPE));
				
				if(type==AttrType.DISCRETE){
					String test = attributes.getValue(XmlConst.TEST);
					res = new DecisionTreeQuestion(new QuestionLabeled(Integer.parseInt(feat),type,test, feat_name));
				}
				else if (type==AttrType.TEXT){
					GramType gramtype = GramType.getFromString(attributes.getValue(XmlConst.GRAM));
					if(gramtype==GramType.SGRAM){
						String fw = attributes.getValue(XmlConst.FIRSTWORD);
						String lw = attributes.getValue(XmlConst.LASTWORD);
						String maxdist = attributes.getValue(XmlConst.MAXDIST);
						SGram sgram = new SGram(fw, lw, Integer.parseInt(maxdist));
						res = new DecisionTreeQuestion(new QuestionLabeled(Integer.parseInt(feat), type, sgram, feat_name));
					}
					else if(gramtype==GramType.FGRAM){
						String words = attributes.getValue(XmlConst.WORDS);
						String[] ws = words.split(XmlConst.DELIMITER);
						FGram fgram = new FGram(ws);
						res = new DecisionTreeQuestion(new QuestionLabeled(Integer.parseInt(feat), type, fgram, feat_name));
					}
				}else if (type==AttrType.CONTINUOUS){
					String test = attributes.getValue(XmlConst.TEST);
					res = new DecisionTreeQuestion(new QuestionLabeled(Integer.parseInt(feat),type,new Double(test), feat_name));
				}else{
					throw new SAXException();
				}
				
				treeStack.push(res);
			}else if(qName.equals(XmlConst.TREE)){
				String answer = attributes.getValue(XmlConst.ANSWER);
				if(answer != null){
					answerStack.push(answer.equals(XmlConst.YESANSWER));
				}
			}else if(qName.equals(XmlConst.DISTRIB)){
				labels.clear();
				nbCl = Integer.parseInt(attributes.getValue(XmlConst.NBCLASSFD));
			}else if(qName.equals(XmlConst.RESULT)){
				labels.put(attributes.getValue(XmlConst.CLASS),
						Double.parseDouble(attributes.getValue(XmlConst.PERCENT)));
			}
		}catch (FAFException e) {
			throw new SAXException();
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		try{
			if(qName.equals(XmlConst.QUESTION)){
				DecisionTree dt = treeStack.pop();
				if(!answerStack.empty()){
					if(answerStack.pop()){
						((DecisionTreeQuestion)treeStack.peek()).yesSetter().set(dt);
					}else{
						((DecisionTreeQuestion)treeStack.peek()).noSetter().set(dt);
					}
				}else{
					trees.setTree(dt);
				}
			}else if(qName.equals(XmlConst.DISTRIB)){
				DecisionTree dt = new DecisionTreeLeaf(new LeafLabels(new HashMap<String, Double>(labels)), nbCl);
				if(!answerStack.empty()){
					if(answerStack.pop()){
						((DecisionTreeQuestion)treeStack.peek()).yesSetter().set(dt);
					}else{
						((DecisionTreeQuestion)treeStack.peek()).noSetter().set(dt);
					}
				}else{
					trees.setTree(dt);
				}
			}
		}catch (FAFException e) {
			throw new SAXException();
		}
	}
	
	@Override
	public void startDocument() throws SAXException {
		buildopts.clear();
		treeStack.clear();
		answerStack.clear();
		trees = new BaggingTrees();
	}
	
	@Override
	public void endDocument() throws SAXException {
		if(!treeStack.empty()) throw new SAXException("treeStack not empty!");
		if(!answerStack.empty()) throw new SAXException("answerStack not empty!");
	}
	
	public BaggingTrees getResult(){
		return trees;
	}
	
	public Map<String,String> getBuildingParameters(){
		return buildopts;
	}
}
