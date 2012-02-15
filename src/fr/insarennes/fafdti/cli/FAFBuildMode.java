package fr.insarennes.fafdti.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FAFBuildMode {
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
		Option o3 = new Option(OUT.substring(0, 1), OUT, true, "Set output filename");
		Option o4 = new Option(BAGGING.substring(0,1), BAGGING, true, "Set number of trees buildt for bagging");
		Option o5 = new Option(CRITERIA.substring(0,1), CRITERIA, true, "Set the criteria used to build the tree");
		Option o6 = new Option(MAXLEAVES.substring(0,1), MAXLEAVES, true, "Choose the maximum number of leaves for one tree");
		Option o7 = new Option(MINEXBYLEAF.substring(0,1), MINEXBYLEAF, true, "Choose the minimum number of examples by leaf");
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
		
		String out = cmdline.getOptionValue(OUT, cmdline.getOptionValue(DATA));
		

		System.out.println("names = "+cmdline.getOptionValue(NAMES));
		System.out.println("data = "+cmdline.getOptionValue(DATA));
		System.out.println("output = "+out);
	}

}
