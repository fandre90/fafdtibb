package fr.insarennes.fafdti;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import fr.insarennes.fafdti.builder.namesinfo.DotNamesInfo;
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
	public static double getSize(String path){
		double size = 0;
		try {
			FileSystem fileSystem = FileSystem.get(new Configuration());
			size = fileSystem.getFileStatus(new Path(path)).getLen();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return size;
	}

	public static double getSize(Path path){
		double size = 0;
		try {
			FileSystem fileSystem = FileSystem.get(new Configuration());
			size = fileSystem.getFileStatus(path).getLen();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return size;
	}
	
	public static Path getPartNonEmptyPath(Path inputDir) throws IOException {
		Configuration conf = new Configuration();
		FileSystem fileSystem;
		fileSystem = FileSystem.get(conf);
		FileStatus[] files = fileSystem.listStatus(inputDir);
		for (int i = 0; i < files.length; i++) {
			Path tmp = files[i].getPath();
			if (tmp.getName().startsWith("part")
					&& fileSystem.getFileStatus(tmp).getLen() > 0)
				return tmp;
		}
		return null;
	}
}
