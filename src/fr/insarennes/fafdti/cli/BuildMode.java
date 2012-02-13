package fr.insarennes.fafdti.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.insarennes.fafdti.cli.CLIEntryPoint.CMode;

public class BuildMode implements IMode{

	private Options opts;
	
	BuildMode(){
		opts = new Options();
		Option o1 = new Option(CMode.BUILDMODE.substring(2,3), CMode.BUILDMODE.substring(2), false, "Choose building mode");
		Option o2 = new Option("n", "names", true, "Set .names filename");
		Option o3 = new Option("d", "data", true, "Set .data filename");
		Option o4 = new Option("o", "output", true, "Set output filename");
		o2.setRequired(true);
		o3.setRequired(true);
		opts.addOption(o1);
		opts.addOption(o2);
		opts.addOption(o3);
		opts.addOption(o4);
	}
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
		
		String out = new String(cmdline.getOptionValue('d'));
		if(cmdline.hasOption('o'))	out = cmdline.getOptionValue('o');
		
		System.out.println("names = "+cmdline.getOptionValue('n'));
		System.out.println("data = "+cmdline.getOptionValue('d'));
		System.out.println("output = "+out);
	}
	public static void displayHelp() {
		System.out.println("---Build mode help---");
		System.out.println("USAGE : "+CMode.BUILDMODE+" --names filename --data filename [--output filename]");
	}

}
