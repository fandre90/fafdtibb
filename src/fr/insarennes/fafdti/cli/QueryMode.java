package fr.insarennes.fafdti.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.insarennes.fafdti.cli.CLIEntryPoint.CMode;
import fr.insarennes.fafdti.visitors.QuestionExample;

public class QueryMode implements IMode{

	private Options opts;
	
	QueryMode(){
		opts = new Options();
		Option o1 = new Option(CMode.QUERYMODE.substring(2,3) ,CMode.QUERYMODE.substring(2) , false, "Choose query mode");
		Option o2 = new Option("i", "input", true, "Set .xml filename");
		Option o3 = new Option("a", "ask", true, "Set the question for asking to tree.xml");
		o2.setRequired(true);
		o3.setRequired(true);
		opts.addOption(o1);
		opts.addOption(o2);
		opts.addOption(o3);
	}
	public void execute(String[] line) {
		CommandLineParser parser = new GnuParser();
		CommandLine cmdline = null;
		try {
			cmdline = parser.parse(opts, line);
		} catch (ParseException e) {
			e.printStackTrace();
			this.displayHelp();
			return;
		}
		
		System.out.println("xml = "+cmdline.getOptionValue('i'));
		System.out.println("question = "+cmdline.getOptionValue('a'));
		
		//On construit l'objet QuestionExample nécessaire pour interroger un arbre
		StringTokenizer tk = new StringTokenizer(cmdline.getOptionValue('a'));
		List<String> qList = new ArrayList<String>();
		while(tk.hasMoreElements())
			qList.add(tk.nextToken(";"));
		
		QuestionExample qExample = new QuestionExample(qList);
		System.out.println(qExample.toString());
		
		//On construit l'arbre à partir du fichier d'entrée
		
		//On visite !
		
	}
	@Override
	public void displayHelp() {
		System.out.println("Query mode help");
	}

}
