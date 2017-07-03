/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : DefaultLogger.java
 * Summary : 
 ******************************************************************************
 * History
 * 
 */
package com.ale.util.log;

/**
 * The Class DefaultLogger.
 */
public final class DefaultLogger implements ILogger
{
	@Override
	public void setLevel(String level)
	{
		if( level != null) {
			System.out.println("set level : [" + level + "]");
		}
	}
	
	@Override
	public void initFileLogging()
	{
		System.out.println("init file logging : [ true ]");
	}
	
	@Override
	public void debug(String tag, String message)
	{
		if( tag != null && message != null) {
			System.out.println("debug : [" + tag + "]: " + message);
		}
	}
	
	@Override
	public void error(String tag, String message)
	{
		if( tag != null && message != null) {
			System.out.println("error : [" + tag + "]: " + message);
		}
	}
	
	@Override
	public void info(String tag, String message)
	{
		if( tag != null && message != null) {
			System.out.println("info :  [" + tag + "]: " + message);
		}
	}
	
	@Override
	public void verbose(String tag, String message)
	{
		if( tag != null && message != null) {
			System.out.println("verbo : [" + tag + "]: " + message);
		}
	}
	
	@Override
	public void warn(String tag, String message)
	{
		if( tag != null && message != null) {
			System.out.println("warn : [" + tag + "]: " + message);
		}
	}
	
	@Override
	public void debug(String tag, String message, Throwable t)
	{
		if( tag != null && message != null) {
			System.out.println("debug : [" + tag + "]: " + message);
			if( t != null)
				t.printStackTrace(System.out);
		}
	}
	
	@Override
	public void error(String tag, String message, Throwable t)
	{
		if( tag != null && message != null) {
			System.out.println("error : [" + tag + "]: " + message);
			if( t != null)
				t.printStackTrace(System.out);
		}
	}
	
	@Override
	public void info(String tag, String message, Throwable t)
	{
		if( tag != null && message != null) {
			System.out.println("info : [" + tag + "]: " + message);
			if( t != null)
				t.printStackTrace(System.out);
		}
	}
	
	@Override
	public void verbose(String tag, String message, Throwable t)
	{
		if( tag != null && message != null) {
			System.out.println("verbo : [" + tag + "]: " + message);
			if( t != null)
				t.printStackTrace(System.out);
		}
	}
	
	@Override
	public void warn(String tag, String message, Throwable t)
	{
		if( tag != null && message != null) {
			System.out.println("warn : [" + tag + "]: " + message);
			t.printStackTrace(System.out);
		}
	}
	
	@Override
	public void userAction(String message)
	{
		if( message != null) {
			verbose("LOGGEDINUSER ACTION", "*** " + message + " ***");
		}
	}
	
	@Override
	public boolean isDebug()
	{
		return true;
	}
	
	@Override
	public String getFullUserActionLog()
	{
		return new String();
	}
	
	@Override
	public void clearUserActionLog()
	{
	}
	
	@Override
	public String getLevelName()
	{
		return "";
	}
}