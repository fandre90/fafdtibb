package fr.insarennes.fafdti.visitors;

import java.io.File;
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

import org.apache.hadoop.io.Text;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
	
	public XmlExporter(BaggingTrees bt, String filenam){
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
		
		Element trees = doc.createElement(XmlConst.TREES);
		doc.appendChild(trees);
		stack.push(trees);
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
        		child.setAttribute("words", ws.substring(0, ws.length() - 2));

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
        File file = new File(filename+".xml");
        Result res = new StreamResult(file);

        DOMSource source = new DOMSource(doc);
        
        Transformer xformer;
		try {
			xformer = TransformerFactory.newInstance().newTransformer();
			xformer.transform(source, res);
			log.log(Level.INFO, "Xml generation done");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Writer w = new StringWriter();
			PrintWriter pw = new PrintWriter(w);
			e.printStackTrace(pw);
			log.log(Level.INFO, w.toString());    
		}
	}
	
	public static void main(String[] args) throws CannotOverwriteTreeException, InvalidProbabilityComputationException {
		DecisionTreeQuestion gtree = new DecisionTreeQuestion(new Question(2,AttrType.TEXT,"t1"));
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
		
		ys.yesSetter().set(new DecisionTreeLeaf(new LeafLabels(m1)));
		ys.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m2)));
		ns.yesSetter().set(ns2);
		ns.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m4)));
		ns2.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m3)));
		ns2.yesSetter().set(new DecisionTreeLeaf(new LeafLabels(n2)));
		
		BaggingTrees btrees = new BaggingTrees(1);
		btrees.setTree(0, gtree);
		XmlExporter xml = new XmlExporter(btrees, "monxml");
		xml.launch();
	}
	
}
