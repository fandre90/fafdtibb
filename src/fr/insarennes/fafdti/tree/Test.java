package fr.insarennes.fafdti.tree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.insarennes.fafdti.AttrType;
import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Question;
import fr.insarennes.fafdti.visitors.*;

public class Test {

	public static void main(String[] args) throws FAFException{
//		/**********test visitor Interrogator***********/
//		System.out.println("-----Test 1-----");
//		//Création de 3 feuilles
//		HashMap<String,Double> hm1 = new HashMap<String,Double>();
//		hm1.put("class1", 1.0);
//		HashMap<String,Double> hm2 = new HashMap<String,Double>();
//		hm2.put("class2", 1.0);
//		HashMap<String,Double> hm3 = new HashMap<String,Double>();
//		hm3.put("class3", 0.4253); hm3.put("class4", 0.49);
//		DecisionTree y = new DecisionTreeLeaf(new LeafLabels(hm1));
//		DecisionTree n = new DecisionTreeLeaf(new LeafLabels(hm2));
//		DecisionTree f = new DecisionTreeLeaf(new LeafLabels(hm3));
//		//Création de la racine  qui a un fils feuille et un fils noeud qui a 2 fils feuilles ;-)
//		DecisionTree son = new DecisionTreeQuestion(new Question(0,AttrType.CONTINUOUS,70), y, n);
//		DecisionTree tree = new DecisionTreeQuestion(new Question(1,AttrType.DISCRETE,"vrai"), f, son);
//		//Création de l'exemple a testé
//		List<String> ex = new ArrayList<String>();
//		ex.add("80"); ex.add("vrai");
//		//Création de l'interrogator avec la question
//		Interrogator inter = new Interrogator(new QuestionExample(ex));
//		//On lance la visite sur la racine
//		/*attention à ça, parceque si on caste et qu'il s'avère que le noeud n'a pas de fils, ça plante dans le visiteur!
//		 * vaut mieux appeler avec un DecisionTreeQuestion pour etre sur qu'il a des fils */
//		tree.accept(inter);
//		//On affiche le résultat
//		System.out.println(inter.getResult().toString());
//		System.out.println("------------------------------------");
		
//		/***********test visitor ToString*****************/
//		System.out.println("-----Test 2-----");
//		//On visite pour afficher l'arbre
//		ToString tostr = new ToString();
//		tree.accept(tostr);
//		System.out.println(tostr.getStr());
//		System.out.println("------------------------------------");
//		/*************************************************/
//		/*************test de construction d'arbre avec Pending et affichage*********/
//		System.out.println("-----Test 3-----");
//		ToString tostr2 = new ToString();
//		DecisionTreePending dtp1 = new DecisionTreePending();
//		DecisionTreePending dtp2 = new DecisionTreePending();
//		DecisionTreeQuestion root = new DecisionTreeQuestion(new Question(0,AttrType.CONTINUOUS,70), dtp1, dtp2);
//		root.accept(tostr2);
//		System.out.println(tostr2.getStr());
//		//on continue de construire l'arbre et on le réaffiche
//		tostr2 = new ToString();
//		HashMap<String,Double> hm3b = new HashMap<String,Double>();
//		hm3b.put("classtt3", 1.0);
//		DecisionTree ly = new DecisionTreeLeaf(new LeafLabels(hm3b));
//		root.yesSetter().set(ly);
//		root.accept(tostr2);
//		System.out.println(tostr2.getStr());
//		System.out.println("------------------------------------");
//		/**************************************************/
//		/*******test de construction avec holder et pending****************/
//		System.out.println("-----Test 4-----");
//		DecisionTreeHolder holder = new DecisionTreeHolder();
//		DecisionTreeQuestion root2 = new DecisionTreeQuestion(new Question(0,AttrType.TEXT,"texxxxxxt"));
//		holder.getNodeSetter().set(root2);
//		ToString tostr3 = new ToString();
//		holder.getRoot().accept(tostr3);
//		System.out.println(tostr3.getStr());
//		System.out.println("------------------------------------");		
//		
//		/*****test pour un interrogator qui a pas encore visiter !***********/
//		System.out.println("----Test 5-----");
//		List<String> lex = new ArrayList<String>();
//		lex.add("jjjj");
//		Interrogator in = new Interrogator(new QuestionExample(lex));
//		System.out.println(in.getResult().toString());
//		System.out.println("-----------------------------------");
		DecisionTreeQuestion gtree = new DecisionTreeQuestion(new Question(2,AttrType.TEXT,"t1"));
		DecisionTreeQuestion ys = new DecisionTreeQuestion(new Question(0,AttrType.DISCRETE, "true"));
		DecisionTreeQuestion ns = new DecisionTreeQuestion(new Question(1,AttrType.CONTINUOUS, 42));
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
		ys.yesSetter().set(new DecisionTreeLeaf(new LeafLabels(m1)));
		ys.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m2)));
		ns.yesSetter().set(new DecisionTreeLeaf(new LeafLabels(m3)));
		ns.noSetter().set(new DecisionTreeLeaf(new LeafLabels(m4)));
		GraphicExporter graph = new GraphicExporter(gtree, "test");
		graph.launch();
		try {
			Runtime.getRuntime().exec("dot -Tpng -otest.png test.dot");
			Runtime.getRuntime().exec("display test.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
