package fr.insarennes.fafdti.cli;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.bagging.Launcher;
import fr.insarennes.fafdti.builder.*;
import fr.insarennes.fafdti.builder.stopcriterion.DepthMax;
import fr.insarennes.fafdti.builder.stopcriterion.ExampleMin;
import fr.insarennes.fafdti.builder.stopcriterion.GainMin;
import fr.insarennes.fafdti.builder.stopcriterion.StoppingCriterion;

public class FAFBuildMode {
	
	static Logger log = Logger.getLogger(FAFBuildMode.class);
	
	public static final String APP_NAME = "fafbuild";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	//options
	public static final String NAMES = "names";
	public static final String DATA = "data";
	public static final String OUT = "output";
	public static final String WORKINGDIR = "workdir";
	public static final String BAGGING = "bagging";
	public static final String CRITERION = "criterion";
	public static final String MAXDEPTH = "maxdepth";
	public static final String MINEXBYLEAF = "minex";
	public static final String GAINMIN = "gainmin";
	public static final String PERCENTBAGGING = "percent";
	
	public static Options opts;
	
	//criterion names constants
	public static final String ENTROPY = "entropy";
	public static final String GINI = "gini";
	
	//default values constants
	public static final String DEFAULT_WORKING_DIR = "./working_dir";
	public static final String DEFAULT_CRITERION = ENTROPY;
	public static final String DEFAULT_BAGGING = "1";
	public static final String DEFAULT_MINEX = "1";
	public static final String DEFAULT_GAINMIN = "10e-3";
	public static final String DEFAULT_MAXDEPTH = Integer.MAX_VALUE+"";
	public static final String DEFAULT_PERCENTBAGGING = "0.6";
	
	public static void initOptions(){
		opts = new Options();
		Option o1 = new Option(NAMES.substring(0, 1), NAMES, true, "Set .names filename");
		Option o2 = new Option(DATA.substring(0, 1), DATA, true, "Set .data filename");
		Option o3 = new Option(OUT.substring(0, 1), OUT, true, "Set output filename (optional)");
		Option o4 = new Option(BAGGING.substring(0,1), BAGGING, true, "Set number of trees buildt for bagging (optional)");
		Option o5 = new Option(CRITERION.substring(0,1), CRITERION, true, "Set the criteria used to build the tree (optional)");
		Option o6 = new Option(MAXDEPTH.substring(0,1).toUpperCase(), MAXDEPTH, true, "Choose the maximum number of leaves for one tree (optional)");
		Option o7 = new Option(MINEXBYLEAF.substring(0,1), MINEXBYLEAF, true, "Choose the minimum number of examples by leaf (optional)");
		Option o8 = new Option(GAINMIN.substring(0,1), GAINMIN, true, "Choose the minimum gain to make a node (optional)");
		Option o9 = new Option(WORKINGDIR.substring(0,1), WORKINGDIR, true, "Set the directorie where hadoop will work (optional)");
		Option o10 = new Option(PERCENTBAGGING.substring(0,1), PERCENTBAGGING, true, "Set the percentage of data file used to build each trees (optional)");
		o1.setRequired(true);
		o2.setRequired(true);
		opts.addOption(o1);
		opts.addOption(o2);
		opts.addOption(o3);
		opts.addOption(o4);
		opts.addOption(o5);
		opts.addOption(o6);
		opts.addOption(o7);
		opts.addOption(o8);
		opts.addOption(o9);
		opts.addOption(o10);
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
		
		String names = cmdline.getOptionValue(NAMES);
		String data = cmdline.getOptionValue(DATA);
		//si pas de sortie précisée, même nom que le .data par défaut
		String out = cmdline.getOptionValue(OUT, data);
		
		log.log(Level.INFO, "Parsing done");
		log.log(Level.INFO, "names = "+names);
		log.log(Level.INFO, "data = "+data);
		log.log(Level.INFO, "output = "+out);
		
		String workingdir = cmdline.getOptionValue(WORKINGDIR, DEFAULT_WORKING_DIR);
		String bagging = cmdline.getOptionValue(BAGGING, DEFAULT_BAGGING);
		String maxdepth = cmdline.getOptionValue(MAXDEPTH, DEFAULT_MAXDEPTH);
		String minex = cmdline.getOptionValue(MINEXBYLEAF, DEFAULT_MINEX);
		String gainmin = cmdline.getOptionValue(GAINMIN, DEFAULT_GAINMIN);
		String crit = cmdline.getOptionValue(CRITERION, DEFAULT_CRITERION);
		String percent = cmdline.getOptionValue(PERCENTBAGGING, DEFAULT_PERCENTBAGGING);
		
		//construction des critères d'arrêt
		List<StoppingCriterion> stopping = new ArrayList<StoppingCriterion>();
		stopping.add(new DepthMax(Integer.parseInt(maxdepth)));
		stopping.add(new ExampleMin(Integer.parseInt(minex)));
		stopping.add(new GainMin(Double.parseDouble(gainmin)));
		
		//construction du critère de construction
		Criterion criterion = null;
		if(crit.equals(ENTROPY))
			criterion = new EntropyCriterion();
		else{
			log.error("Criterion <"+crit+"> not recognized");
			System.exit(0);
		}
		
		//on lance le launcher
		try {
			new Launcher(names+".names", data+".data", workingdir, out, stopping, criterion, Integer.parseInt(bagging), Double.parseDouble(percent));
		} catch (fr.insarennes.fafdti.builder.ParseException e) {
			log.error("ParseException : file "+names+".names malformed");
		}
	}
}
