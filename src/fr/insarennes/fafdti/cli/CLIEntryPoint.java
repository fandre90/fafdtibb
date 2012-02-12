package fr.insarennes.fafdti.cli;

public class CLIEntryPoint {
	
	public static void displayHelp(){
		System.out.println("display help");
	}
	
	public CLIEntryPoint(String sline) {
		String[] line = sline.split(" ");
		String mode = line[0];
		/* On récupère le mode à la main pour appeler le bon mode avec les bonnes options */
		//Build mode
		if(mode.equals("--build") || mode.equals("-b")){
			(new BuildMode()).execute(line);
		}
		//Query mode
		else if (mode.equals("--query") || mode.equals("-q")){
			(new QueryMode()).execute(line);
		}
		else if(mode.equals("--help") || mode.equals("-h")){
			displayHelp();
		}
		else{
			displayHelp();
		}
	}
}
