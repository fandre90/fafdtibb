package fr.insarennes.fafdti.cli;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Classe qui sert a initialiser Log4j
 * 
 * @author festi
 */
public class LoggerManager {
	private static final String DEFAULT_CONF_FILE = "log4j.conf";
	
	/**
	 * initialise Log4j en tentant de charger le fichier de configuration 'DEFAULT_CONF_FILE'
	 */
	public static void setupLogger(){
		setupLogger(DEFAULT_CONF_FILE);
	}
	
	/**
	 * initialise Log4j en tentant de charger le fichier de configuration 'confFile'
	 * @param confFile fichier de configuration de Log4j
	 */
	public static void setupLogger(String confFile){
		Logger log = Logger.getLogger(LoggerManager.class);
		
		if(new File(confFile).exists()){
			PropertyConfigurator.configure(confFile);
			log.info(DEFAULT_CONF_FILE + " loaded");
		}else{
			BasicConfigurator.configure();
			log.warn("unable to load " + DEFAULT_CONF_FILE);
		}
	}
}
