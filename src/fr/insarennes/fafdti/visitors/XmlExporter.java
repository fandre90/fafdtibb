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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.Question;
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
	DecisionTree tree;
	Document doc;
	String filename;
	Stack<Element> stack;
	
	public XmlExporter(DecisionTree dt, String filenam){
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
		tree = dt;
		stack = new Stack<Element>();
		
		Element root = doc.createElement("tree");
        doc.appendChild(root);
        stack.push(root);
	}
	
	@Override
	public void visitQuestion(DecisionTreeQuestion dtq) {
		Question q = dtq.getQuestion();
		Element child = doc.createElement("question");
        child.setAttribute("feature", String.valueOf(q.getCol()));
        child.setAttribute("test", q.getStringValue());
        stack.peek().appendChild(child);
        
        Element treeY = doc.createElement("tree");
        treeY.setAttribute("answer", "yes");
        child.appendChild(treeY);
        stack.push(treeY);
        dtq.getYesTree().accept(this);
        
        stack.pop();
        
        Element treeN = doc.createElement("tree");
        treeN.setAttribute("answer", "no");
        child.appendChild(treeN);
        stack.push(treeN);
        dtq.getNoTree().accept(this);
        
        stack.pop();

	}

	@Override
	public void visitLeaf(DecisionTreeLeaf dtl) {
		Map<String, Double> map = dtl.getLabels().getLabels();
		Element child = doc.createElement("distribution");
		Set<Entry<String,Double>> set = map.entrySet();
		for(Entry<String,Double> e : set){
			Element result = doc.createElement("result");
	        result.setAttribute("class", e.getKey());
	        result.setAttribute("percentage", e.getValue().toString());
	        child.appendChild(result);
		}
		
		stack.peek().appendChild(child);
	}
	
	@Override
	public void visitPending(DecisionTreePending dtl) throws InvalidCallException {
		throw new InvalidCallException(this.getClass().getName()+" cannot visit a DecisionTreePending");
		

	}

	public void launch(){
		tree.accept(this);
		finish();
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
		
		XmlExporter xml = new XmlExporter(gtree, "monxml");
		xml.launch();
	}
	
}
