package fr.insarennes.fafdti.builder;

public class DumbScheduler implements IScheduler {

	@Override
	public void execute(Runnable command) {
		command.run();
	}

}
