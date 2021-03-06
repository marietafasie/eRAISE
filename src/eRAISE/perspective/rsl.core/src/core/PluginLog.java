/**
 * 
 */
package core;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

/**
 * @author Marieta V. Fasie
 * 	marietafasie at gmail dot com
 *
 */
public class PluginLog {
	
	/**
	 * Instance to the logging class
	 */
	private static PluginLog instance;
	
	/**
	 * The system logger
	 */
	private ILog logger;
	
	/**
	 * Plug-in name
	 */
	private String pluginName;
	
	/**
	 * DEBUG level can be true or false
	 * and is set in .properties
	 */
	private static boolean DEBUG =  false;
			
	
	public PluginLog(){
		
		logger = Activator.getDefault().getLog();
		
		pluginName = logger.getBundle().getSymbolicName();
		
		DEBUG = Activator.getDefault().isDebugging() &&
				"true".equalsIgnoreCase( Platform.getDebugOption("rsl.core/debug/debugmsg") );
		
	}
	
	/**
	 * Method for Singleton
	 * 
	 * @return Instance of the PluginLog 
	 */
	public static PluginLog getInstance(){
		if(instance == null){
			instance = new PluginLog();
		}
		return instance;			
	}
	
	/**
	 * Adds error to the system error log
	 * 
	 * @param msg Error message
	 * @param e Exception
	 */
	public void error(String msg, Exception e) {
		logger.log(new Status(Status.ERROR, pluginName, Status.OK, msg, e));
	}
	
	/**
	 * Adds warning to the system log
	 * @param msg Warning message
	 */
	public void warning(String msg){
		logger.log(new Status(Status.WARNING, pluginName, msg));
	}
	
	/**
	 * Info messages added to the system log
	 * 
	 * @param msg Info message
	 */
	public void info(String msg){
			logger.log(new Status(Status.INFO, pluginName, msg));
	}
	
	/**
	 * Debug messages are displayed to the Console
	 * but only if DEBUG is set to true
	 * 
	 * The value on DEBUG is set inside the plug-in
	 * .properties file
	 * 
	 * @param msg Debug message
	 */
	public void debug(String msg){
		if(DEBUG == true){
			System.out.println("DEBUG: "+msg);
		}
	}
}
