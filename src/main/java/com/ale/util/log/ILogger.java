/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : ILogger.java
 * Summary : 
 ******************************************************************************
 * History
 * 
 */
package com.ale.util.log;

/**
 * A Logger provide method to log message. A log is a message send to the logging systeme for a
 * specified level. Log message are associated to a tag specifiying a category.
 * 
 */
public interface ILogger
{
	/**
	 * Set the log level.
	 * 
	 * @param level
	 *            the level to set
	 */
	void setLevel(String level);
	
	/**
	 * Enable the file logging.
	 * 
	 */
	void initFileLogging();
	
	/**
	 * Test the log level.
	 * 
	 * @return true if the debug level is activated.
	 */
	boolean isDebug();
	
	/**
	 * Verbose log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 */
	void verbose(String tag, String message);
	
	/**
	 * Debug log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 */
	void debug(String tag, String message);
	
	/**
	 * Info log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 */
	void info(String tag, String message);
	
	/**
	 * Warning log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 */
	void warn(String tag, String message);
	
	/**
	 * Error log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 */
	void error(String tag, String message);
	
	/**
	 * Verbose log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 * @param t
	 *            the throwable
	 */
	void verbose(String tag, String message, Throwable t);
	
	/**
	 * Debug log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 * @param t
	 *            the throwable
	 */
	void debug(String tag, String message, Throwable t);
	
	/**
	 * Info log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 * @param t
	 *            the throwable
	 */
	void info(String tag, String message, Throwable t);
	
	/**
	 * Warning log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 * @param t
	 *            the throwable
	 */
	void warn(String tag, String message, Throwable t);
	
	/**
	 * Error log
	 * 
	 * @param tag
	 *            the tag identifying the source
	 * @param message
	 *            the message
	 * @param t
	 *            the throwable
	 */
	void error(String tag, String message, Throwable t);
	
	/**
	 * User Action Log
	 * 
	 * @param message
	 *            the message
	 */
	void userAction(String message);
	
	String getFullUserActionLog();
	
	void clearUserActionLog();
	
	String getLevelName();
	
}
