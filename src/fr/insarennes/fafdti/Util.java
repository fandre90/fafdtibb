package fr.insarennes.fafdti;

public class Util {

	public static int[] substractVectStat(int[] vect, int[] vectToSub) {
		int[] substracted = new int[vect.length];
		for(int i=0; i < vect.length; i++) {
			substracted[i] = vect[i] - vectToSub[i];
		}
		return substracted;
	}
}
