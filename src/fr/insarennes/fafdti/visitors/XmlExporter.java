package fr.insarennes.fafdti.visitors;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FSOutputSummer;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.tree.CannotOverwriteTreeException;
import fr.insarennes.fafdti.tree.DecisionTree;
import fr.insarennes.fafdti.tree.DecisionTreeLeaf;
import fr.insarennes.fafdti.tree.DecisionTreePending;
import fr.insarennes.fafdti.tree.DecisionTreeQuestion;
import fr.insarennes.fafdti.tree.DecisionTreeVisitor;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.tree.LeafLabels.InvalidProbabilityComputationException;

public class XmlExporter implements DecisionTreeVisitor {
	Logger log;
	BaggingTrees baggingTrees;
	Document doc;
	String filename;
	Stack<Element> stack;
	
	public XmlExporter(BaggingTrees bt, String filenam, Map<String,String> comments){
		log = Logger.getLogger(XmlExporter.class);
		// creation document
		 DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder;
		try {
			docBuilder = dbfac.newDocumentBuilder();
	         doc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		filename = filenam;
		baggingTrees = bt;
		stack = new Stack<Element>();
		
		//add main trees root
		Element trees = doc.createElement(XmlConst.TREES);
		doc.appendChild(trees);
		stack.push(trees);
		
		//add building attributes
		Element buildopts = doc.createElement(XmlConst.BUILDOPTS);
		Set<Entry<String,String>> set = comments.entrySet();
		for(Entry<String,String> e : set)
			buildopts.setAttribute(e.getKey(), e.getValue());
		trees.appendChild(buildopts);
	}
	
	public void launch(){
		for(int i=0 ; i<baggingTrees.getSize() ; i++){
			Element root = doc.createElement(XmlConst.TREE);
	        stack.peek().appendChild(root);
	        stack.push(root);
	        baggingTrees.getTree(i).accept(this);
	        stack.pop();
		}
		finish();
	}
	
	@Override
	public void visitQuestion(DecisionTreeQuestion dtq) {
		Question q = dtq.getQuestion();
		Element child = doc.createElement(XmlConst.QUESTION);
        child.setAttribute(XmlConst.FEATURE, String.valueOf(q.getCol()));
        child.setAttribute(XmlConst.TYPE, String.valueOf(q.getType()));
        if(q.getType()==AttrType.TEXT){
        	GramType type = q.getGram().getType();
        	child.setAttribute(XmlConst.GRAM, String.valueOf(type));
        	if(type==GramType.SGRAM){
        		SGram sgram = q.getGram().getsGram();
        		child.setAttribute(XmlConst.FIRSTWORD, sgram.getFirstWord().toString());
        		child.setAttribute(XmlConst.LASTWORD, sgram.getLastWord().toString());
        		child.setAttribute(XmlConst.MAXDIST, String.valueOf(sgram.getMaxDistance()));
        	}
        	else if(type==GramType.FGRAM){
        		FGram fgram = q.getGram().getfGram();
        		Text[] words = fgram.getWords();
        		String ws = "";
        		for(int i=0 ; i<words.length ; i++)
        			ws+=words[i].toString()+XmlConst.DELIMITER;
        		child.setAttribute(XmlConst.WORDS, ws.substring(0, ws.length() - 1));

        	}
        }
        else //DISCRETE && CONTINOUS
        	child.setAttribute(XmlConst.TEST, q.getStringValue());
        
        stack.peek().appendChild(child);
        
        Element treeY = doc.createElement(XmlConst.TREE);
        treeY.setAttribute(XmlConst.ANSWER, XmlConst.YESANSWER);
        child.appendChild(treeY);
        stack.push(treeY);
        dtq.getYesTree().accept(this);
        
        stack.pop();
        
        Element treeN = doc.createElement(XmlConst.TREE);
        treeN.setAttribute(XmlConst.ANSWER, XmlConst.NOANSWER);
        child.appendChild(treeN);
        stack.push(treeN);
        dtq.getNoTree().accept(this);
        
        stack.pop();

	}

	@Override
	public void visitLeaf(DecisionTreeLeaf dtl) {
		Map<String, Double> map = dtl.getLabels().getLabels();
		Element child = doc.createElement(XmlConst.DISTRIB);
		try {
			child.setAttribute(XmlConst.NBCLASSFD, String.valueOf(dtl.getNbClassified()));
		} catch (FAFException e1) {
			// TODO Auto-generated catch block
			log.error("Cannot export a distribution leaf if its nbClassified attribute is not set");
		}
		Set<Entry<String,Double>> set = map.entrySet();
		for(Entry<String,Double> e : set){
			Element result = doc.createElement(XmlConst.RESULT);
	        result.setAttribute(XmlConst.CLASS, e.getKey());
	        result.setAttribute(XmlConst.PERCENT, e.getValue().toString());
	        child.appendChild(result);
		}
		
		stack.peek().appendChild(child);
	}
	
	@Override
	public void visitPending(DecisionTreePending dtl) throws InvalidCallException {
		throw new InvalidCallException(this.getClass().getName()+" cannot visit a DecisionTreePending");
		

	}
	
	private void finish(){
		log.log(Level.DEBUG, "final stack size="+stack.size());
		// export dans un fichier
		log.log(Level.INFO, "Xml file creation");
		FileSystem fs = null;
		FSDataOutputStream file = null;
		try {
			fs = FileSystem.get(new Configuration());
			file = fs.create(new Path(filename+".xml"));
		} catch (IOException e1) {
			log.error(e1.getMessage());
		}
        Result res = new StreamResult(file);

        DOMSource source = new DOMSource(doc);
        Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, res);
			file.flush();
			file.close();
			log.log(Level.INFO, "Xml generation done");
			
		} catch (Exception e) {
			log.error(e.getMessage());    
		}
	}
	
	public static void main(String[] args) throws CannotOverwriteTreeException, InvalidProbabilityComputationException {
		DecisionTreeQuestion gtree = new DecisionTreeQuestion(new Question(2,AttrType.DISCRETE,"t1"));
		DecisionTreeQuestion ys = new DecisionTreeQuestion(new Question(0,AttrType.DISCRETE, "true"));
		DecisionTreeQuestion ns = new DecisionTreeQuestion(new Question(1,AttrType.CONTINUOUS, 42));
		DecisionTreeQuestion ns2 = new DecisionTreeQuestion(new Question(1,AttrType.CONTINUOUS, 55));
		gtree.yesSetter().set(ys);
		gtree.noSetter().set(ns);
		Map<String,Double> m1 = new HashMap<String,Double>();
		Map<String,Double> m2 = new HashMap<String,Double>();
		Map<String,Double> m3 = new HashMap<String,Double>();
		Map<String,Double> m4 = new HashMap<String,Double>();
		m1.put("c1", 1.0);
		m2.put("c2", 1.0);
		m3.put("c1", 0.95); m3.put("c3", 0.04);
		m4.put("c3", 1.0);
		Map<String,Double> n2 = new HashMap<String,Double>();
		n2.put("ujhh", 0.99);
		
		ys.yesSetter().set(new DecisionTreeLeaf(new LeafLabels(m1), 1));
		ys.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m2), 1));
		ns.yesSetter().set(ns2);
		ns.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m4), 1));
		ns2.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m3), 2));
		ns2.yesSetter().set(new DecisionTreeLeaf(new LeafLabels(n2), 1));
//		
//		BaggingTrees btrees = new BaggingTrees(1);
//		btrees.setTree(0, gtree);
//		XmlExporter xml = new XmlExporter(btrees, "monxml", "commentaires");
//		xml.launch();
	}
	
}
