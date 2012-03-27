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
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fr.insarennes.fafdti.FAFException;
import fr.insarennes.fafdti.bagging.BaggingTrees;
import fr.insarennes.fafdti.tree.ImportXML;
import fr.insarennes.fafdti.visitors.GraphicExporter;

public class FAFUtilsMode {
	
	static Logger log = Logger.getLogger(FAFUtilsMode.class);
	
	public static final String APP_NAME = "fafutils";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	public static final String PNG = "png";
	public static final String DOT = "dot";
	public static final String IN = "in";
	public static final String OUT = "out";
	public static final String DISPLAY = "display";
	//pour choisir quel arbre exporter parmi tous ceux présent dans le bagging
	public static final String INDEX = "index";
	
	public static final String DEFAULT_INDEX = "0";
	
	public static Options opts_mode;
	public static Options opts_png;
	public static Options opts_dot;
	
	public static void initOptions(){
		opts_mode = new Options();
		opts_png = new Options();
		opts_dot = new Options();
		//mode
		OptionGroup mode = new OptionGroup();
		Option o1 = new Option(PNG.substring(0,1), PNG, false, "Make .png from .xml tree");
		Option o2 = new Option(DOT.substring(0,1), DOT, false, "Make .dot from .xml tree");
		mode.addOption(o1);
		mode.addOption(o2);
		mode.setRequired(true);
		opts_mode.addOptionGroup(mode);
		//options pour mode png
		Option png1 = new Option(IN.substring(0,1), IN, true, "Set .xml filename");
		Option png2 = new Option(OUT.substring(0,1), OUT, true, "Set .png filename (optional)");
		Option png3 = new Option(DISPLAY.substring(0,1).toUpperCase(), DISPLAY, false, "Display image when generation done if checked (optional)");
		Option png4 = new Option(INDEX.substring(0,1).toUpperCase(), INDEX, true, "Set the index of tree to be export among every trees in bagging input (optional)");
		png1.setRequired(true);
		opts_png.addOption(png1);
		opts_png.addOption(png2);
		opts_png.addOption(png3);
		opts_png.addOption(png4);
		//options pour mode dot
		Option dot1 = new Option(IN.substring(0,1), IN, true, "Set .xml filename");
		Option dot2 = new Option(OUT.substring(0,1), OUT, true, "Set .dot filename (optional)");
		Option dot3 = new Option(INDEX.substring(0,1).toUpperCase(), INDEX, true, "Set the index of tree to be export among every trees in bagging input (optional)");
		dot1.setRequired(true);
		opts_dot.addOption(dot1);
		opts_dot.addOption(dot2);
		opts_dot.addOption(dot3);
	}
	
	public static void displayHelp(){
		HelpFormatter h = new HelpFormatter();
		
		Writer w = new StringWriter();
		PrintWriter pw = new PrintWriter(w, true);
		h.printHelp(pw, HelpFormatter.DEFAULT_WIDTH, HEAD_USAGE, "", opts_mode, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		
		Writer wpng = new StringWriter();
		PrintWriter pwpng = new PrintWriter(wpng, true);
		h.printHelp(pwpng, HelpFormatter.DEFAULT_WIDTH, "png mode", "", opts_png, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		
		Writer wdot = new StringWriter();
		PrintWriter pwdot = new PrintWriter(wdot, true);
		h.printHelp(pwdot, HelpFormatter.DEFAULT_WIDTH, "dot mode", "", opts_dot, HelpFormatter.DEFAULT_LEFT_PAD, HelpFormatter.DEFAULT_DESC_PAD, "");
		
		log.log(Level.INFO, w.toString());
		log.log(Level.INFO, wpng.toString());
		log.log(Level.INFO, wdot.toString());
	}
	public static void makePng(CommandLine cmdline){
		log.log(Level.INFO, "Starting makepng");
		//on fait le dot
		makeDot(cmdline);
		
		String out = cmdline.getOptionValue(OUT, cmdline.getOptionValue(IN));
		//on fait le png à partir du dot
		if ((System.getProperty("os.name")).toLowerCase().contains("linux")){
			try {
				String cmd = "dot -Tpng -o"+out+".png "+out+".dot";
				Process p = Runtime.getRuntime().exec(cmd);
				log.log(Level.DEBUG, cmd);
				try {
					log.log(Level.DEBUG, "return="+p.waitFor());
				} catch (InterruptedException e) {
					log.log(Level.ERROR, "dot command failed");
					return;
				}
				log.log(Level.INFO, "png export done");
				//on affiche si demandé
				if(cmdline.hasOption(DISPLAY))
					Runtime.getRuntime().exec("display "+out+".png");
					log.log(Level.DEBUG, "display png done");
			} catch (IOException e) {
				log.log(Level.ERROR, "png export needs ImageMagick library installed");
				return;
			}
		}
		else{
			log.log(Level.ERROR, "png export only available under Linux !");
			return;
		}
		log.log(Level.INFO, "makepng done");
	}
	public static void makeDot(CommandLine cmdline){
		log.log(Level.INFO, "Starting makedot");
		
		ImportXML importer = new ImportXML(cmdline.getOptionValue(IN));
		try {
			importer.launch();
		} catch (FAFException e1) {
			log.log(Level.ERROR, "Xml import failed");
			return;
		}
		log.log(Level.INFO, "Xml import done");
		BaggingTrees bt = importer.getResult();
		
		String out = cmdline.getOptionValue(OUT, cmdline.getOptionValue(IN));
		String index = cmdline.getOptionValue(INDEX, DEFAULT_INDEX);
		int intindex = Integer.parseInt(index);
		//test du paramètre de la valeur rentrée par l'utilisateur
		if(intindex<0 || intindex>bt.getSize()){
			log.error("Parameter <"+INDEX+"> must be an integer between 0 and the number of trees in bagging - 1");
			System.exit(0);
		}
		//on fait le dot
		GraphicExporter graph = new GraphicExporter(bt.getTree(intindex), out);
		graph.launch();
		log.log(Level.INFO, "makedot done");
	}
	public static void main(String[] args) {
		LoggerManager.setupLogger();
		
		initOptions();
		
		if(args.length<1)
			displayHelp();
		else{			
			//parsing du mode
			String[] smode = new String[1];
			smode[0] = args[0];
			CommandLineParser parsermode = new GnuParser();
			CommandLine cmdlinemode = null;
			try {
				cmdlinemode = parsermode.parse(opts_mode, smode);
			} catch (ParseException e) {
				log.log(Level.INFO, e.getMessage());
				displayHelp();
				System.exit(0);
			}
			//parsing du reste des options suivant le mode choisi
			String[] sargs = new String[args.length - 1];
			for(int i=0 ; i<sargs.length ; i++)
				sargs[i] = args[i+1];
			
			Options os = new Options();
			if(cmdlinemode.hasOption(PNG))
				os = opts_png;
			else if(cmdlinemode.hasOption(DOT))
				os = opts_dot;
			
			CommandLineParser parser = new GnuParser();
			CommandLine cmdline = null;
			try {
				cmdline = parser.parse(os, sargs);
			} catch (ParseException e) {
				log.log(Level.ERROR, e.getMessage());
				displayHelp();
				System.exit(0);
			}
			
			log.log(Level.INFO, "Parsing done");
			
			//on apelle la bonne fonction suivant le mode avec le reste des options en paramètres
			if(cmdlinemode.hasOption(PNG)){
				makePng(cmdline);
			}
			else if(cmdlinemode.hasOption(DOT)){
				makeDot(cmdline);
			}
		}
	}
}
