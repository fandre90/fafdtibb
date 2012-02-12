package fr.insarennes.fafdti.cli;

public interface IMode {
	void execute(String[] line);
	void displayHelp();
}
