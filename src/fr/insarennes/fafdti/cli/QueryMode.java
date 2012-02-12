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

import fr.insarennes.fafdti.visitors.QuestionExample;

public class QueryMode implements Mode{

	private Options opts;
	
	QueryMode(){
		opts = new Options();
		Option o1 = new Option("q", "query", false, "Choose query mode");
		Option o2 = new Option("i", "input", true, "Set .xml filename");
		Option o3 = new Option("q", "question", true, "Set the question for asking to tree.xml");
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(cmdline == null)	{
			CLIEntryPoint.displayHelp();
			System.exit(0);
		}
		
		System.out.println("xml = "+cmdline.getOptionValue('i'));
		System.out.println("question = "+cmdline.getOptionValue('q'));
		
		//On construit l'objet QuestionExample nécessaire pour interroger un arbre
		StringTokenizer tk = new StringTokenizer(cmdline.getOptionValue('q'));
		List<String> qList = new ArrayList<String>();
		while(tk.hasMoreElements())
			qList.add(tk.nextToken(";"));
		
		QuestionExample qExample = new QuestionExample(qList);
		System.out.println(qExample.toString());
		
		//On construit l'arbre à partir du fichier d'entrée
		
		//On visite !
		
	}

}
