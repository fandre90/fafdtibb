package fr.insarennes.fafdti.cli;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.tree.ImportXML;
import fr.insarennes.fafdti.visitors.Interrogator;
import fr.insarennes.fafdti.visitors.QuestionExample;


public class FAFQueryMode {
	
	static Logger log = Logger.getLogger(FAFQueryMode.class);
	
	public static final String APP_NAME = "fafquery";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	public static final String IN = "input";
	public static final String QUESTION = "question";
	public static final String OUT = "output";
	
	public static Options opts;
	
	public static final String DELIMITER = ",";
	
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
		Writer w = new StringWriter();
		PrintWriter pw = new PrintWriter(w, true);
		h.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, HEAD_USAGE, "", opts, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		log.log(Level.INFO, w.toString());
	}
	
	public static void main(String[] args) {
		LoggerManager.setupLogger();
		
		initOptions();
		
		CommandLineParser parser = new GnuParser();
		CommandLine cmdline = null;
		try {
			cmdline = parser.parse(opts, args);
		} catch (ParseException e) {
			log.log(Level.ERROR, e.getMessage());
			displayHelp();
			System.exit(0);
		}
		
		log.log(Level.INFO, "Parsing done");
		//On construit l'objet QuestionExample nécessaire pour interroger un arbre
		StringTokenizer tk = new StringTokenizer(cmdline.getOptionValue(QUESTION));
		List<String> qList = new ArrayList<String>();
		while(tk.hasMoreElements())
			qList.add(tk.nextToken(DELIMITER));
		
		QuestionExample qExample = new QuestionExample(qList);
		log.log(Level.INFO, qExample.toString());
		
		//On construit l'arbre à partir du fichier d'entrée
		ImportXML importer = new ImportXML(cmdline.getOptionValue(IN));
		try {
			importer.launch();
		} catch (FAFException e) {
			// TODO Auto-generated catch block
			log.log(Level.ERROR, "Xml import failed");
			System.exit(0);
		}
		log.log(Level.INFO, "Xml import done");
		//On visite !
		Interrogator query = new Interrogator(qExample);
		importer.getResult().accept(query);
		log.log(Level.INFO, "Query process done");
		log.log(Level.INFO, "Here is the answer");
		log.log(Level.INFO, query.getResult().toString());
		
		//si OUT précisé, on écrit à la fin du fichier OUT la réponse
		if(cmdline.hasOption(OUT)){
			Writer w = null;
			try {
				w = new FileWriter(cmdline.getOptionValue(OUT), true);
			} catch (IOException e) {
				log.log(Level.ERROR, "Unable to open file '"+cmdline.getOptionValue(OUT)+ "' when trying to write answer in it");
				System.exit(0);
			}
			PrintWriter pw = new PrintWriter(w);
			pw.println("###Query on "+cmdline.getOptionValue(IN)+".xml tree###");
			pw.println("##With question :");
			pw.print(qExample.toString());
			pw.println("#Answer is :");
			pw.println(query.getResult().toString());
			pw.flush();
			pw.close();
			log.log(Level.INFO, "Answer output writing done");
		}
	}
}
