package fr.insarennes.fafdti.visitors;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.iterators.UnmodifiableIterator;
import org.apache.hadoop.io.Writable;
/** Classe encapsulant les valeurs d'une question posée
 * Ces valeurs sont sous forme de String et rangées dans le même ordre que spécifier
 * dans le fichier de données ayant servi à construire l'arbre.
 * On peut voir cette classe comme le containeur d'une ligne d'exemple de données
 * sans le dernier champs qui est la classe à laquelle appartient cet exemple
 */
//Implémente la structure de données stockant une question sur un arbre
public class QuestionExample implements Iterable<String>, Writable{
	private List<String> qExample;
	
	public QuestionExample(List<String> ex){
		qExample = ex;
	}

	/**
	 * @param label le numéro de l'attribut désiré
	 * @return la valeur de cet attribut
	 */
	public String getValue(int label) {
		return qExample.get(label);
	}
	
	public String toString(){
		String res = "";
		for(int i=0 ; i<qExample.size() ; i++)
			res+="Feature "+i+", Value="+qExample.get(i)+"\n";
		return res;
		
	}

	@Override
	public Iterator<String> iterator() {
		// TODO Auto-generated method stub
		return UnmodifiableIterator.decorate(this.qExample.iterator());
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		int size = in.readInt();
		this.qExample = new ArrayList<String>();
		for(int i=0; i<size; i++) {
			qExample.add(in.readUTF());
		}
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(qExample.size());
		for(String value : qExample) {
			out.writeUTF(value);
		}
	}

}
