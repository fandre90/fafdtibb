package fr.insarennes.fafdti.builder.scheduler;


public class DumbScheduler implements IScheduler {

	@Override
	public void execute(Runnable command) {
		command.run();
	}

}
