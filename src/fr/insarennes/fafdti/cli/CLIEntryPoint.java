package fr.insarennes.fafdti.cli;

public class CLIEntryPoint {
	
	public CLIEntryPoint(String sline) {
		String[] line = sline.split(" ");
		String mode = line[0];
		/* On récupère le mode à la main pour appeler le bon mode avec les bonnes options */
		//Build mode
		if(checkMode(mode,CMode.BUILDMODE)){
			(new BuildMode()).execute(line);
		}
		//Query mode
		else if (checkMode(mode,CMode.QUERYMODE)){
			(new QueryMode()).execute(line);
		}
		//Utils mode
		else if(checkMode(mode,CMode.UTILSMODE)){
			//This mode need an argument for option mode to specify what to do
			if(line.length<2)
				UtilsMode.displayHelp();
			else
				(new UtilsMode(line[1])).execute(line);
		}
		else if(checkMode(mode,CMode.HELPMODE)){
			displayHelp();
		}
		else{
			displayHelp();
		}
	}
	public static void displayHelp(){
		System.out.println("===FAFDTIBB help===");
		System.out.println("USAGE : "+FAFMain.HEAD_USAGE+" --help");
		BuildMode.displayHelp();
		QueryMode.displayHelp();
		UtilsMode.displayHelp();
	}
	private boolean checkMode(String mode, String withlongMode){
		return mode.equals(withlongMode) || mode.equals(getShort(withlongMode));
	}
	private String getShort(String longOption){
		return longOption.substring(1,3);
	}
	public class CMode{
		public static final String QUERYMODE = "--query";
		public static final String BUILDMODE = "--build";
		public static final String UTILSMODE = "--utils";
		public static final String HELPMODE = "--help";
	}
}
