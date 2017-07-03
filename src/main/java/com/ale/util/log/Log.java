/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : Log.java
 * Summary : 
 ******************************************************************************
 * History
 * 
 */
package com.ale.util.log;

/**
 * A generic Log Factory
 */
public final class Log
{
	/**
	 * @uml.property  name="logger"
	 * @uml.associationEnd  
	 */
	private static ILogger logger = null;

	/**
	 * Set the logger
	 * @param log  the logger
	 * @uml.property  name="logger"
	 */
	public static void setLogger(ILogger log)
	{
		logger = log;
	}

	/**
	 * Construct a default logger
	 *
	 * @return the default logger
	 */
	private static ILogger createDefaultLogger()
	{
		return new DefaultLogger();
	}
	
	/**
	 * @return  the logger
	 * @uml.property  name="logger"
	 */
	public static ILogger getLogger()
	{
		if (logger == null)
		{
			logger = createDefaultLogger();
		}
		
		return logger;
	}
	
	/**
	 * Instantiating utility classes does not make sense. Hence the constructors should either be
	 * private or (if you want to allow subclassing) protected. A common mistake is forgetting to
	 * hide the default constructor.
	 */
	private Log()
	{
		throw new UnsupportedOperationException();
	}	
}
