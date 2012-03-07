package fr.insarennes.fafdti.tree;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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

	//private String filename;
	Document doc;
	List<DecisionTree> listTree;
	
	ImportXML(String filename){
		listTree = new ArrayList<DecisionTree>();
		try{
			// création d'une fabrique de documents
			DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();
			
			// création d'un constructeur de documents
			DocumentBuilder constructeur = fabrique.newDocumentBuilder();
			
			// lecture du contenu d'un fichier XML avec DOM
			File xml = new File(filename+".xml");
			doc = constructeur.parse(xml);
			
			//traitement du document
			//voir ExempleDOM.zip
		
		}catch(ParserConfigurationException pce){
			System.out.println("Erreur de configuration du parseur DOM");
			System.out.println("lors de l'appel à fabrique.newDocumentBuilder();");
		}catch(SAXException se){
			System.out.println("Erreur lors du parsing du document");
			System.out.println("lors de l'appel à construteur.parse(xml)");
		}catch(IOException ioe){
			System.out.println("Erreur d'entrée/sortie");
			System.out.println("lors de l'appel à construteur.parse(xml)");
		}
		try {
			this.launch();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.println("Bad xml format");
		}
	}
	
	private void launch() throws NumberFormatException, FAFException{
		//Recuperation de la racine trees
		Element root = doc.getDocumentElement();
		// On recupere ses fils : tree
		NodeList liste = root.getChildNodes();

	    for (int i = 0; i < liste.getLength(); i++) {
	        Node noeud = liste.item(i);
	        DecisionTree tree = buildOneTree(noeud);
	        listTree.add(tree);
	      }
		
	}
	
	private DecisionTree buildOneTree(Node tree) throws NumberFormatException, FAFException{
		DecisionTree res = null;
		
		NodeList nl = tree.getChildNodes();
		if(nl.getLength()!=1)	
			System.out.println("erreur1");
		
		Node n = nl.item(0);
		String name = n.getNodeName();
		if(name.equals("question")){
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
				System.out.println("erreur3");
			
			DecisionTree yes = buildOneTree(sons.item(0));
			DecisionTree no = buildOneTree(sons.item(1));
			((DecisionTreeQuestion)res).yesSetter().set(yes);
			((DecisionTreeQuestion)res).noSetter().set(no);
			
		}
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
			System.out.println("erreur2");
		
		return res;
	}
	
	DecisionTree getResult(){
		
		return listTree.get(0);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
			ImportXML xml = new ImportXML("monxml");
			XmlExporter exp = new XmlExporter(xml.getResult(), "testimport");
			exp.launch();
//			
//			GraphicExporter graph = new GraphicExporter(xml.getResult(), "testimport");
//			graph.launch();
//			try {
//				Runtime.getRuntime().exec("dot -Tpng -otest.png test.dot");
//				Runtime.getRuntime().exec("display test.png");
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	}

}
