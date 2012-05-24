package fr.insarennes.fafdti.tree;

import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.builder.DirDeleter;
import fr.insarennes.fafdti.builder.Question;

/**
 * Classe lié à son dossier sur le hdfs, qui va supprimer ces données une fois que ces
 * 2 fils se sont construit
 * @author momo
 *
 */
public class LinkedDecisionTreeQuestion extends DecisionTreeQuestion {
	private int nbPendingDone;
	private Path dirPath;
	public LinkedDecisionTreeQuestion(Question q, Path dirPath) {
		super(q);
		nbPendingDone = 0;
		this.dirPath = dirPath;
	}
	private void tryDelDir(){
		if(nbPendingDone==2)
			new DirDeleter(this.dirPath);
	}
	public DecisionNodeSetter yesSetter(){
		return new DecisionNodeSetter() {
			public void set(DecisionTree node) throws CannotOverwriteTreeException{
				if(yesTree.canOverwrite()){
					yesTree = node;
					nbPendingDone++;
					tryDelDir();
				} else {
					throw new CannotOverwriteTreeException();
				}
			}
			
		};
	}
	public DecisionNodeSetter noSetter(){
		return new DecisionNodeSetter() {
			public void set(DecisionTree node) throws CannotOverwriteTreeException{
				if(noTree.canOverwrite()){
					noTree = node;
					nbPendingDone++;
					tryDelDir();
				} else {
					throw new CannotOverwriteTreeException();
				}
			}
			
		};
	}

}
