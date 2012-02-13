package fr.insarennes.fafdti.cli;

public class CLIEntryPoint {
	
	public CLIEntryPoint(String sline) {
		String[] line = sline.split(" ");
		String mode = line[0];
		/* On récupère le mode à la main pour appeler le bon mode avec les bonnes options */
		//Build mode
		if(mode.equals(CMode.BUILDMODE) || mode.equals(CMode.BUILDMODE.substring(1,3))){
			(new BuildMode()).execute(line);
		}
		//Query mode
		else if (mode.equals(CMode.QUERYMODE) || mode.equals(CMode.QUERYMODE.substring(1,3))){
			(new QueryMode()).execute(line);
		}
		else if(mode.equals(CMode.HELPMODE) || mode.equals(CMode.HELPMODE.substring(1,3))){
			displayHelp();
		}
		else{
			displayHelp();
		}
	}
	public static void displayHelp(){
		System.out.println("===FAFDTIBB help===");
		BuildMode.displayHelp();
		QueryMode.displayHelp();
	}
	public class CMode{
		public static final String QUERYMODE = "--query";
		public static final String BUILDMODE = "--build";
		public static final String HELPMODE = "--help";
	}
}
