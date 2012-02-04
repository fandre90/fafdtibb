package fr.insarennes.fafdti.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.insarennes.fafdti.AttrType;
import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.Question;
import fr.insarennes.fafdti.visitors.*;

public class Test {

	public static void main(String[] args) throws FAFException{
		/**********test visitor Interrogator***********/
		System.out.println("-----Test 1-----");
		//Création de 3 feuilles
		HashMap<String,Double> hm1 = new HashMap<String,Double>();
		hm1.put("caca", 1.0);
		HashMap<String,Double> hm2 = new HashMap<String,Double>();
		hm2.put("pipi", 1.0);
		HashMap<String,Double> hm3 = new HashMap<String,Double>();
		hm3.put("vomi", 0.4253); hm3.put("ducul", 0.49);
		DecisionTree y = new DecisionTreeLeaf(new LeafLabels(hm1));
		DecisionTree n = new DecisionTreeLeaf(new LeafLabels(hm2));
		DecisionTree f = new DecisionTreeLeaf(new LeafLabels(hm3));
		//Création de la racine  qui a un fils feuille et un fils noeud qui a 2 fils feuilles ;-)
		DecisionTree son = new DecisionTreeQuestion(new Question(0,AttrType.CONTINUOUS,70), y, n);
		DecisionTree tree = new DecisionTreeQuestion(new Question(1,AttrType.DISCRETE,"vrai"), f, son);
		//Création de l'exemple a testé
		List<String> ex = new ArrayList<String>();
		ex.add("80"); ex.add("vrai");
		//Création de l'interrogator avec la question
		Interrogator inter = new Interrogator(new QuestionExample(ex));
		//On lance la visite sur la racine
		/*attention à ça, parceque si on caste et qu'il s'avère que le noeud n'a pas de fils, ça plante dans le visiteur!
		 * vaut mieux appeler avec un DecisionTreeQuestion pour etre sur qu'il a des fils */
		inter.visitQuestion((DecisionTreeQuestion)tree);
		//On affiche le résultat
		System.out.println(inter.getResult().toStr());
		System.out.println("------------------------------------");
		/***********test visitor ToString*****************/
		System.out.println("-----Test 2-----");
		//On visite pour afficher l'arbre
		ToString tostr = new ToString();
		tostr.visitQuestion((DecisionTreeQuestion)tree);
		System.out.println(tostr.getStr());
		System.out.println("------------------------------------");
		/*************************************************/
		/*************test de construction d'arbre avec Pending et affichage*********/
		System.out.println("-----Test 3-----");
		ToString tostr2 = new ToString();
		DecisionTreePending dtp1 = new DecisionTreePending();
		DecisionTreePending dtp2 = new DecisionTreePending();
		DecisionTreeQuestion root = new DecisionTreeQuestion(new Question(0,AttrType.CONTINUOUS,70), dtp1, dtp2);
		tostr2.visitQuestion(root);
		System.out.println(tostr2.getStr());
		//on continue de construire l'arbre et on le réaffiche
		tostr2 = new ToString();
		HashMap<String,Double> hm3b = new HashMap<String,Double>();
		hm3b.put("tt3", 1.0);
		DecisionTree ly = new DecisionTreeLeaf(new LeafLabels(hm3b));
		root.yesSetter().set(ly);
		tostr2.visitQuestion(root);
		System.out.println(tostr2.getStr());
		System.out.println("------------------------------------");
	}
}
