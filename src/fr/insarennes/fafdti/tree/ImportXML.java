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
import fr.insarennes.fafdti.builder.AttrType;
import fr.insarennes.fafdti.builder.Question;
import fr.insarennes.fafdti.visitors.GraphicExporter;
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
			
		}catch(ParserConfigurationException pce){
			log.log(Level.ERROR, "Erreur de configuration du parseur DOM");
		}catch(SAXException se){
			log.log(Level.ERROR, "Erreur lors du parsing du document");
		}catch(IOException ioe){
			log.log(Level.ERROR,"Erreur d'entrée/sortie");
		}
	}
	
	public void launch() throws FAFException{
		log.log(Level.INFO, "Import from xml starting...");
		//Recuperation de la racine trees
		Element root = doc.getDocumentElement();
		// On recupere ses fils : tree
		NodeList liste = root.getChildNodes();

	    for (int i = 0; i < liste.getLength(); i++) {
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
		if(name.equals("question")){
			//Construction du noeud avec la question
			NamedNodeMap map = n.getAttributes();
			String feat = map.getNamedItem("feature").getNodeValue();
			String test = map.getNamedItem("test").getNodeValue();
			AttrType type = AttrType.getFromString(map.getNamedItem("type").getNodeValue());
			
			if(type==AttrType.DISCRETE || type==AttrType.TEXT)
				res = new DecisionTreeQuestion(new Question(Integer.parseInt(feat),type,test));
			else
				res = new DecisionTreeQuestion(new Question(Integer.parseInt(feat),type,new Double(test)));
			//appel récursif fils gauche et droit
			NodeList sons = n.getChildNodes();
			if(sons.getLength()!=2)
				throw new XmlMalformedException("The node is not binary");
			
			Node son1 = sons.item(0);
			Node son2 = sons.item(1);
			DecisionTree yes = null;
			DecisionTree no = null;
			if(son1.getAttributes().getNamedItem("answer").getNodeValue().equals("yes") && 
				son2.getAttributes().getNamedItem("answer").getNodeValue().equals("no")){
				yes = buildOneTree(son1);
				no = buildOneTree(son2);
			}
			else if(son2.getAttributes().getNamedItem("answer").getNodeValue().equals("yes") && 
					son1.getAttributes().getNamedItem("answer").getNodeValue().equals("no")){
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
		else if(name.equals("distribution")){
			Map<String,Double> hm = new HashMap<String,Double>();
			NodeList dist = n.getChildNodes();
			for(int i=0 ; i<dist.getLength() ; i++){
				Node result = dist.item(i);
				NamedNodeMap map = result.getAttributes();
				hm.put(map.getNamedItem("class").getNodeValue(), Double.valueOf(map.getNamedItem("percentage").getNodeValue()));
			}
			res = new DecisionTreeLeaf(new LeafLabels(hm));
		}
		else
			throw new XmlMalformedException();
		
		return res;
	}
	
	public DecisionTree getResult(){
		return listTree.get(0);
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
			XmlExporter exp = new XmlExporter(xml.getResult(), "testimport");
			exp.launch();
			
			GraphicExporter graph = new GraphicExporter(xml.getResult(), "testimport");
			graph.launch();
			if ((System.getProperty("os.name")).toLowerCase().contains("linux")){
				try {
					Runtime.getRuntime().exec("dot -Tpng -otestimport.png testimport.dot");
					Runtime.getRuntime().exec("display testimport.png");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("png export needs ImageMagick library installed");
				}
			}
			else{
				System.out.println("png export only available under Linux !");
			}
	}

}
