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
import fr.insarennes.fafdti.bagging.BaggingInterrogator;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.tree.ImportXML;
import fr.insarennes.fafdti.tree.LeafLabels;
import fr.insarennes.fafdti.visitors.QuestionExample;
/**
 * Point d'entrée du mode d'interrogation de FAFDTIBB
 * 
 * On peut soit directement poser une question sur un arbre (le format est 
 * le suivant : les valeurs des différents attributs doivent être en ordre, 
 * séparer par une virgule et si besoin le tout entouré de guillemets), 
 * 
 * soit lancer une campagne de test sur l'entrée standard et générer un rapport html
 */

public class FAFQueryMode {
	
	static Logger log = Logger.getLogger(FAFQueryMode.class);
	
	public static final String APP_NAME = "fafquery";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	public static final String IN = "input";
	public static final String QUESTION = "question";
	public static final String OUT = "output";
	public static final String DETAILS = "details";
	
	public static Options opts;
	
	public static final String DELIMITER = ",";
	
	public static void initOptions(){
		opts = new Options();
		Option o1 = new Option(IN.substring(0, 1), IN, true, "Set .xml filename");
		Option o2 = new Option(QUESTION.substring(0, 1), QUESTION, true, "Set the question for asking to .xml tree (optional) (default read the standard input and launch a stats campaign)");
		Option o3 = new Option(OUT.substring(0,1), OUT, true, "Set the .txt filename where write output (default in console) (optional)");
		Option o4 = new Option(DETAILS.substring(0,1), DETAILS, false, "Choose to see details of answer (optional)");
		o1.setRequired(true);
		opts.addOption(o1);
		opts.addOption(o2);
		opts.addOption(o3);
		opts.addOption(o4);
	}
	
	public static void displayHelp(){
		HelpFormatter h = new HelpFormatter();
		Writer w = new StringWriter();
		PrintWriter pw = new PrintWriter(w, true);
		h.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, HEAD_USAGE, "", opts, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		log.log(Level.INFO, w.toString());
		System.exit(FAFExitCode.EXIT_ERROR);
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
		}
		
		log.log(Level.INFO, "Parsing done");
		
		//si pas de question, on lance une session de stats sur l'entrée standart
		if(!cmdline.hasOption(QUESTION)){
			//Importer bagging trees
			String xmlInput = cmdline.getOptionValue(IN);
			BaggingTrees trees = null;
			ImportXML importer = new ImportXML(xmlInput);
			try {
				importer.launch();
			} catch (FAFException e) {
				log.error("Xml import failed");
				System.exit(FAFExitCode.EXIT_UNOCCURED_EXCEPTION);
			}
			trees = importer.getResult();
			//Launch stater
			QueryStater stater = new QueryStater(trees);
			try {
				log.info("Query process starting...");
				stater.launch();
			} catch (IOException e) {
				log.error("Error occured while reading standard input");
				System.exit(FAFExitCode.EXIT_UNOCCURED_EXCEPTION);
			} catch (FAFException e) {
				log.error(e.getMessage());
				System.exit(FAFExitCode.EXIT_UNOCCURED_EXCEPTION);
			}
			//log result in console
			log.info(stater.getFastResult());			
			//Make html output
			HtmlStater html = new HtmlStater(importer.getBuildingParameters(), stater);
			String output = cmdline.getOptionValue(OUT, xmlInput);
			html.make(output);
			log.info("Full report has been generated in "+output+".html");
		}
			
		//sinon on pose la question normalement
		else{
			//On construit l'objet QuestionExample nécessaire pour interroger un arbre
			StringTokenizer tk = new StringTokenizer(cmdline.getOptionValue(QUESTION));
			List<String> qList = new ArrayList<String>();
			while(tk.hasMoreElements())
				qList.add(tk.nextToken(DELIMITER).trim());
			
			QuestionExample qExample = new QuestionExample(qList);
			log.log(Level.INFO, qExample.toString());
			
			//On construit l'arbre à partir du fichier d'entrée
			ImportXML importer = new ImportXML(cmdline.getOptionValue(IN));
			try {
				importer.launch();
			} catch (FAFException e) {
				// TODO Auto-generated catch block
				log.log(Level.ERROR, "Xml import failed");
				System.exit(FAFExitCode.EXIT_UNOCCURED_EXCEPTION);
			}
			log.log(Level.INFO, "Xml import done");
			//On visite !
			BaggingInterrogator bgint = new BaggingInterrogator(importer.getResult());
			LeafLabels res = bgint.query(qExample);
			String answer = "";
			if(cmdline.hasOption(DETAILS))
				answer = res.toString();
			else
				answer = "The example has been classified in class "+res.getBestScore();
			log.log(Level.INFO, "Query process done");
			log.log(Level.INFO, "Here is the answer");
			log.log(Level.INFO, answer);
			
			//si OUT précisé, on écrit à la fin du fichier OUT la réponse
			if(cmdline.hasOption(OUT)){
				Writer w = null;
				try {
					w = new FileWriter(cmdline.getOptionValue(OUT), true);
				} catch (IOException e) {
					log.log(Level.ERROR, "Unable to open file '"+cmdline.getOptionValue(OUT)+ "' when trying to write answer in it");
					System.exit(FAFExitCode.EXIT_UNOCCURED_EXCEPTION);
				}
				PrintWriter pw = new PrintWriter(w);
				pw.println("###Query on "+cmdline.getOptionValue(IN)+".xml tree###");
				pw.println("##With question :");
				pw.print(qExample.toString());
				pw.println("#Answer is :");
				pw.println(answer);
				pw.flush();
				pw.close();
				log.log(Level.INFO, "Answer output writing done");
			}
		}
		
	}
}
