package fr.insarennes.fafdti.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.insarennes.fafdti.visitors.QuestionExample;


public class FAFQueryMode {
	public static final String APP_NAME = "fafquery";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	public static final String IN = "input";
	public static final String QUESTION = "question";
	public static final String OUT = "output";
	
	public static Options opts;
	
	public static void initOptions(){
		opts = new Options();
		Option o1 = new Option(IN.substring(0, 1), IN, true, "Set .xml filename");
		Option o2 = new Option(QUESTION.substring(0, 1), QUESTION, true, "Set the question for asking to .xml tree");
		Option o3 = new Option(OUT.substring(0,1), OUT, true, "Set the .txt filename where write output (default in console) (optional)");
		o1.setRequired(true);
		o2.setRequired(true);
		opts.addOption(o1);
		opts.addOption(o2);
		opts.addOption(o3);
	}
	
	public static void displayHelp(){
		HelpFormatter h = new HelpFormatter();
		h.printHelp(HEAD_USAGE, opts);
	}
	
	public static void main(String[] args) {
		initOptions();
		
		CommandLineParser parser = new GnuParser();
		CommandLine cmdline = null;
		try {
			cmdline = parser.parse(opts, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			displayHelp();
			System.exit(0);
		}
		
		//On construit l'objet QuestionExample nécessaire pour interroger un arbre
		StringTokenizer tk = new StringTokenizer(cmdline.getOptionValue(QUESTION));
		List<String> qList = new ArrayList<String>();
		while(tk.hasMoreElements())
			qList.add(tk.nextToken(";"));
		
		QuestionExample qExample = new QuestionExample(qList);
		System.out.println(qExample.toString());
		
		//On construit l'arbre à partir du fichier d'entrée
		
		//On visite !
	}
}
