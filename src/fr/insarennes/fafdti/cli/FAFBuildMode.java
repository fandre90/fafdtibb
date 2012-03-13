package fr.insarennes.fafdti.cli;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import fr.insarennes.fafdti.builder.*;
import fr.insarennes.fafdti.tree.*;
import fr.insarennes.fafdti.visitors.XmlExporter;

public class FAFBuildMode {
	
	static Logger log = Logger.getLogger(FAFBuildMode.class);
	
	public static final String APP_NAME = "fafbuild";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	public static final String NAMES = "names";
	public static final String DATA = "data";
	public static final String OUT = "output";
	public static final String BAGGING = "bagging";
	public static final String CRITERIA = "crit";
	public static final String MAXLEAVES = "leavmx";
	public static final String MINEXBYLEAF = "minex";
	
	public static Options opts;
	
	public static void initOptions(){
		opts = new Options();
		Option o1 = new Option(NAMES.substring(0, 1), NAMES, true, "Set .names filename");
		Option o2 = new Option(DATA.substring(0, 1), DATA, true, "Set .data filename");
		Option o3 = new Option(OUT.substring(0, 1), OUT, true, "Set output filename (optional)");
		Option o4 = new Option(BAGGING.substring(0,1), BAGGING, true, "Set number of trees buildt for bagging (optional)");
		Option o5 = new Option(CRITERIA.substring(0,1), CRITERIA, true, "Set the criteria used to build the tree (optional)");
		Option o6 = new Option(MAXLEAVES.substring(0,1), MAXLEAVES, true, "Choose the maximum number of leaves for one tree (optional)");
		Option o7 = new Option(MINEXBYLEAF.substring(0,1), MINEXBYLEAF, true, "Choose the minimum number of examples by leaf (optional)");
		o1.setRequired(true);
		o2.setRequired(true);
		opts.addOption(o1);
		opts.addOption(o2);
		opts.addOption(o3);
		opts.addOption(o4);
		opts.addOption(o5);
		opts.addOption(o6);
		opts.addOption(o7);
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
			log.log(Level.INFO, e.getMessage());
			displayHelp();
			System.exit(0);
		}
		
		//si pas de sortie précisée, même nom que le .data par défaut
		String out = cmdline.getOptionValue(OUT, cmdline.getOptionValue(DATA));
		
		log.log(Level.INFO, "Parsing done");
		log.log(Level.INFO, "names = "+cmdline.getOptionValue(NAMES));
		log.log(Level.INFO, "data = "+cmdline.getOptionValue(DATA));
		log.log(Level.INFO, "output = "+out);
		
		
		//Construction du FeatureSpec à partir du .names
		DotNamesInfo fs = null;
		try {
			fs = new DotNamesInfo(new Path(cmdline.getOptionValue(NAMES)+".names"), FileSystem.get(new Configuration()));
		} catch (fr.insarennes.fafdti.builder.ParseException e) {
			// TODO Auto-generated catch block
			Writer w = new StringWriter();
			PrintWriter pw = new PrintWriter(w);
			e.printStackTrace(pw);
			log.log(Level.ERROR, w.toString());
			log.log(Level.ERROR, e.getMessage());
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Writer w = new StringWriter();
			PrintWriter pw = new PrintWriter(w);
			e.printStackTrace(pw);
			log.log(Level.ERROR, w.toString());
			System.exit(0);
		}
		
		//Lancement de la construction de l'arbre
		DecisionTree root = null;
		//*****************//
		
		
		//*****************//
		//Export du résultat
		XmlExporter xmlexp = new XmlExporter(root, out);
		xmlexp.launch();
	}
}
