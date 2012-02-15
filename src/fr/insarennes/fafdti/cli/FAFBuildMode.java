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
	
	public static Options opts;
	
	public static void initOptions(){
		opts = new Options();
		Option o1 = new Option(NAMES.substring(0, 1), NAMES, true, "Set .names filename");
		Option o2 = new Option(DATA.substring(0, 1), DATA, true, "Set .data filename");
		Option o3 = new Option(OUT.substring(0, 1), OUT, true, "Set output filename");
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
		
		String out = cmdline.getOptionValue(OUT, cmdline.getOptionValue(DATA));
		

		System.out.println("names = "+cmdline.getOptionValue(NAMES));
		System.out.println("data = "+cmdline.getOptionValue(DATA));
		System.out.println("output = "+out);
	}

}
