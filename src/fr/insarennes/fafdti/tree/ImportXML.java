package fr.insarennes.fafdti.tree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.builder.gram.FGram;
import fr.insarennes.fafdti.builder.gram.GramType;
import fr.insarennes.fafdti.builder.gram.SGram;
import fr.insarennes.fafdti.visitors.GraphicExporter;
import fr.insarennes.fafdti.visitors.XmlConst;
import fr.insarennes.fafdti.visitors.XmlExporter;

public class ImportXML {

	private Logger log;
	private Document doc;
	private List<DecisionTree> listTree;
	
	public ImportXML(String filename){
		log = Logger.getLogger(ImportXML.class);
		listTree = new ArrayList<DecisionTree>();
		try{
			// création d'une fabrique de documents
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
			
			// création d'un constructeur de documents
			DocumentBuilder constructeur = fabrique.newDocumentBuilder();
			
			// lecture du contenu d'un fichier XML avec DOM
			File xml = new File(filename+".xml");
			doc = constructeur.parse(xml);
			
		} catch(Exception e){
			log.error(e.getMessage());
		}
	}
	
	public void launch() throws FAFException{
		log.log(Level.INFO, "Import from xml starting...");
		//Recuperation de la racine trees
		Element root = doc.getDocumentElement();
		// On recupere ses fils : tree
		NodeList liste = root.getChildNodes();
		
		//warning = node 0 = building options
	    for (int i = 1; i < liste.getLength(); i++) {
	        Node noeud = liste.item(i);
	        DecisionTree tree = buildOneTree(noeud);
	        listTree.add(tree);
	        log.log(Level.INFO, "...");
	      }
		log.log(Level.INFO, "Import from xml finished sucessfully!");
	}
	
	private DecisionTree buildOneTree(Node tree) throws FAFException{
		DecisionTree res = null;
		
		NodeList nl = tree.getChildNodes();
		if(nl.getLength()!=1)	
			throw new XmlMalformedException();
		
		Node n = nl.item(0);
		String name = n.getNodeName();
		//cas noeud
		if(name.equals(XmlConst.QUESTION)){
			//Construction du noeud avec la question
			NamedNodeMap map = n.getAttributes();
			String feat = map.getNamedItem(XmlConst.FEATURE).getNodeValue();
			AttrType type = AttrType.getFromString(map.getNamedItem(XmlConst.TYPE).getNodeValue());
			
			if(type==AttrType.DISCRETE){
				String test = map.getNamedItem(XmlConst.TEST).getNodeValue();
				res = new DecisionTreeQuestion(new Question(Integer.parseInt(feat),type,test));
			}
			else if (type==AttrType.TEXT){
				GramType gramtype = GramType.getFromString(map.getNamedItem(XmlConst.GRAM).getNodeValue());
				if(gramtype==GramType.SGRAM){
					String fw = map.getNamedItem(XmlConst.FIRSTWORD).getNodeValue();
					String lw = map.getNamedItem(XmlConst.LASTWORD).getNodeValue();
					String maxdist = map.getNamedItem(XmlConst.MAXDIST).getNodeValue();
					SGram sgram = new SGram(fw, lw, Integer.parseInt(maxdist));
					res = new DecisionTreeQuestion(new Question(Integer.parseInt(feat), type, sgram));
				}
				else if(gramtype==GramType.FGRAM){
					String words = map.getNamedItem(XmlConst.WORDS).getNodeValue();
					String[] ws = words.split(XmlConst.DELIMITER);
					FGram fgram = new FGram(ws);
					res = new DecisionTreeQuestion(new Question(Integer.parseInt(feat), type, fgram));
				}
			}
			else{	//CONTINUOUS
				String test = map.getNamedItem(XmlConst.TEST).getNodeValue();
				res = new DecisionTreeQuestion(new Question(Integer.parseInt(feat),type,new Double(test)));
			}
			//appel récursif fils gauche et droit
			NodeList sons = n.getChildNodes();
			if(sons.getLength()!=2)
				throw new XmlMalformedException("The node is not binary");
			
			Node son1 = sons.item(0);
			Node son2 = sons.item(1);
			DecisionTree yes = null;
			DecisionTree no = null;
			if(son1.getAttributes().getNamedItem(XmlConst.ANSWER).getNodeValue().equals(XmlConst.YESANSWER) && 
				son2.getAttributes().getNamedItem(XmlConst.ANSWER).getNodeValue().equals(XmlConst.NOANSWER)){
				yes = buildOneTree(son1);
				no = buildOneTree(son2);
			}
			else if(son2.getAttributes().getNamedItem(XmlConst.ANSWER).getNodeValue().equals(XmlConst.YESANSWER) && 
					son1.getAttributes().getNamedItem(XmlConst.ANSWER).getNodeValue().equals(XmlConst.NOANSWER)){
					yes = buildOneTree(son2);
					no = buildOneTree(son1);
				}
			else
				throw new XmlMalformedException("The node doesn't have a 'yes' and a 'no' son");
			//On attache les fils au noeud courant
			((DecisionTreeQuestion)res).yesSetter().set(yes);
			((DecisionTreeQuestion)res).noSetter().set(no);
		}
		//cas feuille
		else if(name.equals(XmlConst.DISTRIB)){
			Map<String,Double> hm = new HashMap<String,Double>();
			NodeList dist = n.getChildNodes();
			for(int i=0 ; i<dist.getLength() ; i++){
				Node result = dist.item(i);
				NamedNodeMap map = result.getAttributes();
				hm.put(map.getNamedItem(XmlConst.CLASS).getNodeValue(), Double.valueOf(map.getNamedItem(XmlConst.PERCENT).getNodeValue()));
			}
			String nbCl = n.getAttributes().getNamedItem(XmlConst.NBCLASSFD).getNodeValue();
			res = new DecisionTreeLeaf(new LeafLabels(hm), Integer.parseInt(nbCl));
		}
		else
			throw new XmlMalformedException();
		
		return res;
	}
	
	public BaggingTrees getResult(){
		return new BaggingTrees(listTree);
	}
	
	public class XmlMalformedException extends FAFException{
		public XmlMalformedException() {
			super();
		}
		public XmlMalformedException(String string) {
			super(string);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 6393835555398240583L;
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws FAFException{
			ImportXML xml = new ImportXML("monxml");
			xml.launch();
//			XmlExporter exp = new XmlExporter(xml.getResult(), "testimport", "commentaire");
//			exp.launch();
			
//			GraphicExporter graph = new GraphicExporter(xml.getResult(), "testimport");
//			graph.launch();
//			if ((System.getProperty("os.name")).toLowerCase().contains("linux")){
//				try {
//					Runtime.getRuntime().exec("dot -Tpng -otestimport.png testimport.dot");
//					Runtime.getRuntime().exec("display testimport.png");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//					System.out.println("png export needs ImageMagick library installed");
//				}
//			}
//			else{
//				System.out.println("png export only available under Linux !");
//			}
	}

}
