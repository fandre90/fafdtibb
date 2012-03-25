package fr.insarennes.fafdti.bagging;

import java.util.ArrayList;
import java.util.List;

import fr.insarennes.fafdti.visitors.StatChecker;

public class BaggingStatChecker {

	private BaggingTrees trees;
	private boolean check;
	private List<String> stats;
	
	
	public BaggingStatChecker(BaggingTrees trees){
		this.trees = trees;
		this.check = true;
		this.stats = new ArrayList<String>();
	}
	
	public void launch(){
		for(int i=0 ; i<trees.getSize() ; i++){
			StatChecker sc = new StatChecker();
			trees.getTree(i).accept(sc);
			check &= sc.checkOK();
			stats.add(sc.toString());
		}
	}
	
	public boolean checkOK(){
		return check;
	}
	
	public String toString(){
		String res = "";
		for(String s : stats){
			res+=s+"\n";
		}
		return res;
	}
	
}
