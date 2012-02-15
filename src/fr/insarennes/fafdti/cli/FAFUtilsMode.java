package fr.insarennes.fafdti.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class FAFUtilsMode {
	public static final String APP_NAME = "fafutils";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	public static Options opts_mode;
	public static Options opts_png;
	public static Options opts_dot;
	
	public static void initOptions(){
		opts_mode = new Options();
		opts_png = new Options();
		opts_dot = new Options();
		//mode
		OptionGroup mode = new OptionGroup();
		Option o1 = new Option("p", "png", false, "Make .png from .xml tree");
		Option o2 = new Option("d", "dot", false, "Make .dot from .xml tree");
		mode.addOption(o1);
		mode.addOption(o2);
		mode.setRequired(true);
		opts_mode.addOptionGroup(mode);
		//options pour mode png
		Option png1 = new Option("i", "input", true, "Set .xml filename");
		Option png2 = new Option("o", "output", true, "Set .png filename");
		png1.setRequired(true);
		opts_png.addOption(png1);
		opts_png.addOption(png2);
		//options pour mode dot
		Option dot1 = new Option("i", "input", true, "Set .xml filename");
		Option dot2 = new Option("o", "output", true, "Set .dot filename");
		dot1.setRequired(true);
		opts_dot.addOption(dot1);
		opts_dot.addOption(dot2);
	}
	
	public static void displayHelp(){
		HelpFormatter h = new HelpFormatter();
		h.printHelp(HEAD_USAGE, opts_mode);
		h.printHelp("png mode", opts_png);
		h.printHelp("dot mode", opts_dot);
	}
	public static void makePng(CommandLine cmdline){
		System.out.println("makepng");
	}
	public static void makeDot(CommandLine cmdline){
		System.out.println("makedot");
	}
	public static void main(String[] args) {
		
		if(args.length<1)
			displayHelp();
		else{
			initOptions();
			
			String[] smode = new String[1];
			smode[0] = args[0];
			CommandLineParser parsermode = new GnuParser();
			CommandLine cmdlinemode = null;
			try {
				cmdlinemode = parsermode.parse(opts_mode, smode);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				displayHelp();
				System.exit(0);
			}
			
			String[] sargs = new String[args.length - 1];
			for(int i=0 ; i<sargs.length ; i++)
				sargs[i] = args[i+1];
			
			Options os = new Options();
			if(cmdlinemode.hasOption('p'))
				os = opts_png;
			else if(cmdlinemode.hasOption('d'))
				os = opts_dot;
			
			CommandLineParser parser = new GnuParser();
			CommandLine cmdline = null;
			try {
				cmdline = parser.parse(os, sargs);
			} catch (ParseException e) {
				System.out.println(e.getMessage());
				displayHelp();
				System.exit(0);
			}
			if(cmdlinemode.hasOption('p')){
				makePng(cmdline);
			}
			else if(cmdlinemode.hasOption('d')){
				makeDot(cmdline);
			}

		}
	}
}
