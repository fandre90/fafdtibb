package fr.insarennes.fafdti.cli;

public class Test {
	public static void main(String[] args) {
		String sline = "--build   -n titi    -d toto --output tutout";
		new CLIEntryPoint(sline);
		String sline2 = "--help";
		new CLIEntryPoint(sline2);
		String sline3 = "-q -i tree --ask 10;true;vrai;sarkozy";
		new CLIEntryPoint(sline3);
//		/* futur main/point d'entré de FAFDTIBB ! */
//		if(args.length < 1)
//			CLIEntryPoint.displayHelp();
//		else 
//			new CLIEntryPoint(args);	
//			//modifier le constructeur pour prendre String[] en param
//			//au lieu de String et de splitter !
	}
}
