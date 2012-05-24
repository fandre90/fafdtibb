package fr.insarennes.fafdti;

import java.util.Arrays;
/**
 * Classe utilitaire
 */
public class Util {

	public static int[] substractVectStat(int[] vect, int[] vectToSub) {
		int[] substracted = new int[vect.length];
		for(int i=0; i < vect.length; i++) {
			substracted[i] = vect[i] - vectToSub[i];
		}
		return substracted;
	}
	
	/** Génère une liste triée d'entiers aléatoires compris entre 0 et max
	 * @param size la taille de la liste a générée
	 * @param max le max
	 * @return la liste générée
	 */
	public static int[] getSortedRandomIntList(int size, int max){
		int[] res = new int[size];
		for(int i=0 ; i<size ; i++){
			res[i] = (int)(Math.random() * max); 
		}
		Arrays.sort(res);
		return res;
	}
}
