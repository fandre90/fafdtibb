import org.apache.log4j.Logger;
import fr.insarennes.fafdti.cli.LoggerManager;

public class TestLogger {
	static Logger log = Logger.getLogger(TestLogger.class);

	public static void main(String[] args) {
		LoggerManager.setupLogger();
		
		log.info("info message");
		log.warn("warn message");
	}

}
