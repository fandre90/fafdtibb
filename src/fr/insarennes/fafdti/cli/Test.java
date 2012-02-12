package fr.insarennes.fafdti.cli;

public class Test {
	public static void main(String[] args) {
		String sline = "--build   -n titi    -d toto -output tutout";
		new CLIEntryPoint(sline);
		String sline2 = "--help";
		new CLIEntryPoint(sline2);
		String sline3 = "--query -i tree --question 10;true;vrai;sarkozy";
		new CLIEntryPoint(sline3);
	}
}
