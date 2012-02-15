package fr.insarennes.fafdti.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.insarennes.fafdti.cli.IMode;
import fr.insarennes.fafdti.cli.CLIEntryPoint.CMode;


public class UtilsMode implements IMode {

	private Options opts;
	private static final String MAKEPNG = "mkpng";
	private static final String MAKEDOT = "mkdot";

	public UtilsMode(String mode){
		opts = new Options();
		Option o1 = new Option(CMode.UTILSMODE.substring(2,3), CMode.UTILSMODE.substring(2), true, "Choose utils mode and what module used");
		opts.addOption(o1);
		//All others options needed to handle all utils mode
		//Mode=Make png graph
		if(mode.equals(MAKEPNG) || mode.equals(getShort(MAKEPNG))){
			Option o2 = new Option("i", "input", true, "Set .xml input filename");
			Option o3 = new Option("o", "output", true, "Set .png output filename");
			o2.setRequired(true);
			opts.addOption(o2);
			opts.addOption(o3);
		}
		//Mode=Make dot file
		else if(mode.equals(MAKEDOT) || mode.equals(getShort(MAKEDOT))){
			Option o2 = new Option("i", "input", true, "Set .xml input filename");
			Option o3 = new Option("o", "output", true, "Set .png output filename");
			o2.setRequired(true);
			opts.addOption(o2);
			opts.addOption(o3);
		}
		else{
			displayHelp();
			System.exit(0);
		}
	}
	@Override
	public void execute(String[] line) {
		CommandLineParser parser = new GnuParser();
		CommandLine cmdline = null;
		try {
			cmdline = parser.parse(opts, line);
		} catch (ParseException e) {
			e.printStackTrace();
			displayHelp();
			return;
		}
		String mode = cmdline.getOptionValue('u');
		//make png mode
		if(mode.equals(MAKEPNG))
			makePng(cmdline);
		else if (mode.equals(MAKEDOT))
			makeDot(cmdline);
		else
			displayHelp();
	}
	
	
	public static void displayHelp(){
		System.out.println("---Utils mode help---");
		System.out.println("USAGE : "+FAFMain.HEAD_USAGE+" "+CMode.UTILSMODE+" \t"+MAKEPNG+" --input filename [--output filename]");
		System.out.println("\t\t\t\t|\t"+MAKEDOT+" --input filename [--output filename]");		
	}
	public void makePng(CommandLine cmdline){
		System.out.println("make png");
		//construction de l'arbre par import xml
		//copier/coller du main de la classe Test dans fafdti.tree
	}
	public void makeDot(CommandLine cmdline){
		System.out.println("make dot");
		//comme makePng, sauf qu'on appel pas les 2 commandes pour faire le png !
	}
	private String getShort(String longmode){
		return longmode.substring(0,1);
	}

}

