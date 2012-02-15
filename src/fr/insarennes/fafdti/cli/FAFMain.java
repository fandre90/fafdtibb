package fr.insarennes.fafdti.cli;

public class FAFMain {
	
	public static final String APP_NAME = "faf";
	public static final int MAJOR_VERSION = 1;
	public static final int MINOR_VERSION = 0;
	public static final String HEAD_USAGE = "java -jar "+APP_NAME+MAJOR_VERSION+"."+MINOR_VERSION+".jar";
	
	public static void main(String[] args) {
		String sline = "--build   -n titi    -d toto --output tutout";
		new CLIEntryPoint(sline);
		String sline2 = "--help";
		new CLIEntryPoint(sline2);
		String sline3 = "-q -i tree --ask 10;true;vrai;sarkozy";
		new CLIEntryPoint(sline3);
		String sline4 = "--utils mkpng -i jiji";
		new CLIEntryPoint(sline4);
		String sline5 = "-u";
		new CLIEntryPoint(sline5);
//		/* futur main/point d'entré de FAFDTIBB ! */
//		if(args.length < 1)
//			CLIEntryPoint.displayHelp();
//		else 
//			new CLIEntryPoint(args);	
//			//modifier le constructeur pour prendre String[] en param
//			//au lieu de String et de splitter !
	}
}
