package fr.insarennes.fafdti.tree;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import fr.insarennes.fafdti.AttrType;
import fr.insarennes.fafdti.Question;
import fr.insarennes.fafdti.visitors.Interrogator;
import fr.insarennes.fafdti.visitors.QuestionExample;

public class DecisionTreeQuestion implements DecisionTree {
	private DecisionTree _yesTree;
	private DecisionTree _noTree;
	private Question _question;
	public DecisionTreeQuestion() {
		_yesTree = new DecisionTreePending();
		_noTree = new DecisionTreePending();
	}
	public DecisionTreeQuestion(Question q, DecisionTree dtyes, DecisionTree dtno) {
		_yesTree = dtyes;
		_noTree = dtno;
		_question = q;
	}
	@Override
	public void accept(DecisionTreeVisitor dtv) {
		dtv.visitQuestion(this);
		
	}

	@Override
	public boolean canOverwrite() {
		return false;
	}
	
	public DecisionNodeSetter yesSetter(){
		return new DecisionNodeSetter(_yesTree);
	}
	public DecisionNodeSetter noSetter(){
		return new DecisionNodeSetter(_noTree);
	}
	public Question getQuestion() {
		return _question;
	}
	public DecisionTree getYesTree() {
		return _yesTree;
	}
	public DecisionTree getNoTree() {
		return _noTree;
	}

	public static void main(String[] args) throws InvalidProbabilityComputationException {
		//Création de 3 feuilles
		HashMap<String,Double> hm1 = new HashMap<String,Double>();
		hm1.put("caca", 1.0);
		HashMap<String,Double> hm2 = new HashMap<String,Double>();
		hm2.put("pipi", 1.0);
		HashMap<String,Double> hm3 = new HashMap<String,Double>();
		hm3.put("vomi", 0.5); hm3.put("ducul", 0.49);
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
		try {
			System.out.println(inter.getResult().toStr());
		} catch (InvalidProbabilityComputationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
