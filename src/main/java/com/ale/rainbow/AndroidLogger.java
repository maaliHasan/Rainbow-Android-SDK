/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : AndroidLogger.java
 * Summary : 
 ******************************************************************************
 * History
 * 2010/11/05 M.Geyer crms00272218 if the mobile is in "black screen", the MIC incoming call screen isn't displayed
 *   Changed default log level to INFO 
 * 2011/01/19 Franci11 crms00287216 Always initialize log size with the file length if this one already exists
 * 2011/01/19 Franci11 crms00287219 if the log is deleted, create it on MIC startup 
 */
package com.ale.rainbow;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.ale.util.log.ILogger;
import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;
import com.google.code.microlog4android.appender.FileAppender;
import com.google.code.microlog4android.format.Formatter;
import com.google.code.microlog4android.format.PatternFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger for android.
 */
public class AndroidLogger implements ILogger
{
	private static final String RAINBOW_TAG = "RAINBOW";
	public static final String USERACTION_FILENAME = "Rainbow-Android-UserActions.txt";
	public static final String LOG_FILEPATH = "Rainbow/";
	public static final String FILENAME = "Rainbow-Android-logs.txt";
	public static final String LOG_FILENAME = LOG_FILEPATH + FILENAME;
	private static final int DEFAULT_FILE_SIZE_THRESH = 1022 * 1024; // 1MB - 2kB
	private static final int DEFAULT_MAX_FILE_SIZE = 1024 * 1024; // 1MB
	
	private long m_maxFileSize = DEFAULT_MAX_FILE_SIZE;
	private long m_fileSizeThresh = DEFAULT_FILE_SIZE_THRESH;
	public static final int MAX_BACKUP_INDEX = 5; // 5 files
	private static final int LOG_OVERHEAD_SIZE = 70; // Over-estimated size for the date, level...
	private static final Level DEFAULT_LOG_LEVEL = Level.INFO;

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
	private SimpleDateFormat hhdateFormat = new SimpleDateFormat("HH:mm:ss");
	
	public long getMaxFileSize()
	{
		return m_maxFileSize;
	}
	
	public void setMaxFileSize(long maxFileSize)
	{
		m_maxFileSize = maxFileSize;
	}
	
	public static int getMaxBackupIndex()
	{
		return MAX_BACKUP_INDEX;
	}
	
	public long getFileSizeThresh()
	{
		return m_fileSizeThresh;
	}
	
	public void setFileSizeThresh(long fileSizeThresh)
	{
		m_fileSizeThresh = fileSizeThresh;
	}
	
	private Level m_logLevel = DEFAULT_LOG_LEVEL;
	
	private FileWriter m_userActionFile = null;
	private long m_userActionEstimatedSize = 0;

	private boolean m_unitTestMode = false;
	private Logger m_micrologger = null;
	private FileAppender m_appender = null;
	private Formatter m_formatter = null;
	
	// Estimation of the file size
	private long m_estimatedSize = -1;
	private boolean m_rolling = false;
	private Context m_context;

	public void setUnitTestMode(boolean unitTestMode) {
		m_unitTestMode = unitTestMode;
	}

	public boolean isUnitTestMode() {
		return m_unitTestMode;
	}

	public AndroidLogger(Context context, String level)
	{
		m_context = context;
		
		m_micrologger = LoggerFactory.getLogger();
		m_micrologger.setClientID(RAINBOW_TAG);
		
		setLevel(level);
		
		m_formatter = new PatternFormatter();
		m_formatter.setProperty("pattern", "%d{DATE} %P [%t] %m %T");
		
		createLogDirectoryIfNeeded();
		
		m_appender = new FileAppender();
		m_appender.setAppend(true);
		m_appender.setFileName(LOG_FILENAME);
		m_appender.setFormatter(m_formatter);
		
		openAppender();
		
		openUserActionFile();
	}
	
	private void createLogDirectoryIfNeeded()
	{
		// if directory does not exist, create it
		File directory = new File(Environment.getExternalStorageDirectory(), LOG_FILEPATH);
		if (!directory.exists())
		{
			info(RAINBOW_TAG, "AndroidLogger directory " + LOG_FILEPATH + " not found, creating it");
			directory.mkdirs();
		}
	}
	
	@Override
	public void clearUserActionLog()
	{
		openUserActionFile();
	}
	
	private void openUserActionFile()
	{
		try
		{
			if (m_userActionFile != null)
			{
				m_userActionFile.close();
				m_userActionFile = null;
			}
			m_userActionEstimatedSize = 0;
			
			File fileStreamPath = m_context.getFileStreamPath(USERACTION_FILENAME);
			m_userActionFile = new FileWriter(fileStreamPath, false);
			// FileOutputStream outputStream = m_context.openFileOutput(USERACTION_FILENAME,
			// Context.MODE_PRIVATE);
			// m_userActionFile = new FileWriter(outputStream.getFD());
			
			if (m_userActionFile != null)
			{
				Date now = new Date();
				StringBuffer datestrg = new StringBuffer();
				datestrg.append("*** DATE ; ");
				datestrg.append(dateFormat.format(now));
				datestrg.append(" ***");
				
				System.out.println(datestrg.toString());
				
				datestrg.append("\r\n");
				
				m_userActionFile.write(datestrg.toString());
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 private void startFileObserver() { if (m_fileObserver == null) { m_fileDeletionDetected =
	 * false; m_fileObserver = new FileObserver("/sdcard/" + LOG_FILENAME, FileObserver.DELETE_SELF)
	 * {
	 * 
	 * @Override public void onEvent(int event, String path) {
	 *           System.out.println("FileObserver; FILE DELETED"); closeAppender();
	 *           m_fileDeletionDetected = true; } }; m_fileObserver.startWatching(); } }
	 */
	
	/**
	 * Method ; openAppender
	 */
	private void openAppender()
	{
		try
		{
			m_appender.open();
			m_micrologger.addAppender(m_appender);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Method ; closeAppender
	 */
	public void closeAppender()
	{
		try
		{
			m_appender.close();
			m_micrologger.removeAllAppenders();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setLevel(String level)
	{
		if (level == null)
		{
			m_logLevel = DEFAULT_LOG_LEVEL;
		}
		else
		{
			m_logLevel = convertLevel(level);
		}
		
		if (null != m_micrologger)
		{
			m_micrologger.setLevel(m_logLevel);
			
			info(RAINBOW_TAG, "Logging level set to " + m_logLevel.name());
		}
	}

	@Override
	public void initFileLogging()
	{
		if (m_micrologger == null)
			return;
		
		createLogDirectoryIfNeeded();
		
		File externalStorageDirectory = Environment.getExternalStorageDirectory();
		File file = new File(externalStorageDirectory, LOG_FILENAME);
		if (!file.exists())
		{
			closeAppender();
		}
		
		openAppender();
		
		info(RAINBOW_TAG, "Logging to file enabled");
	}

	@Override
	public void debug(String tag, String message)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.DEBUG_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);

			m_micrologger.debug(sbLog.toString());
			checkRollOver(sbLog);
		}
	}

	@Override
	public void debug(String tag, String message, Throwable t)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.DEBUG_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);

			m_micrologger.debug(sbLog.toString(), t);
			checkRollOver(sbLog);
		}
	}

	@Override
	public void error(String tag, String message)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.ERROR_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);

			m_micrologger.error(sbLog.toString());
			checkRollOver(sbLog);
		}
	}

	@Override
	public void error(String tag, String message, Throwable t)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.ERROR_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);

			m_micrologger.error(sbLog.toString(), t);
			checkRollOver(sbLog);
		}
	}

	@Override
	public void info(String tag, String message)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.INFO_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);
			
			m_micrologger.info(sbLog.toString());
			checkRollOver(sbLog);
		}
	}

	@Override
	public void info(String tag, String message, Throwable t)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.INFO_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);
			
			m_micrologger.info(sbLog.toString(), t);
			checkRollOver(sbLog);
		}
	}

	@Override
	public void verbose(String tag, String message)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.TRACE_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);
			
			m_micrologger.trace(sbLog.toString());
			checkRollOver(sbLog);
		}
	}

	@Override
	public void verbose(String tag, String message, Throwable t)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.TRACE_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);
			
			m_micrologger.trace(sbLog.toString(), t);
			checkRollOver(sbLog);
		}
	}

	@Override
	public void warn(String tag, String message)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.WARN_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);
			
			m_micrologger.warn(sbLog.toString());
			checkRollOver(sbLog);
		}
	}

	@Override
	public void warn(String tag, String message, Throwable t)
	{
		if( isUnitTestMode() ) {
			System.out.println(tag+" : "+message);
			return;
		}

		if (m_logLevel.toInt() <= Level.WARN_INT)
		{
			StringBuilder sbLog = new StringBuilder();
			
			sbLog.append(tag);
			sbLog.append(" - ");
			sbLog.append(message);
			
			m_micrologger.warn(sbLog.toString(), t);
			checkRollOver(sbLog);
		}
	}
	
	@Override
	public void userAction(String message)
	{
		verbose("USER_ACTION", message);
		
		StringBuffer sbLog = new StringBuffer();
		Date date = new Date();

		
		sbLog.append(hhdateFormat.format(date));
		sbLog.append(" ; ");
		sbLog.append(message);
		
		try
		{
			m_userActionEstimatedSize += sbLog.length();
			if (m_userActionEstimatedSize > getMaxFileSize())
			{
				openUserActionFile();
			}
			
			sbLog.append("\r\n");
			if (m_userActionFile != null)
			{
				m_userActionFile.write(sbLog.toString());
				m_userActionFile.flush();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public String getFullUserActionLog()
	{
		StringBuffer fullLog = new StringBuffer();
		
		try
		{
			FileInputStream fileInput = m_context.openFileInput(USERACTION_FILENAME);
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileInput));
			
			// BufferedReader reader = new BufferedReader(new
			// FileReader(getUserActionLogFullName()));
			String line;
			while ((line = reader.readLine()) != null)
			{
				fullLog.append(line);
			}
			reader.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return fullLog.toString();
	}
	
	@Override
	public boolean isDebug()
	{
		return (m_logLevel.toInt() <= Level.DEBUG_INT);
	}
	
	/**
	 * Check for roll-over
	 */
	private void checkRollOver(StringBuilder sbLogStr)
	{
		// Si estimedSize vaut -1 ,alors la variable n'a pas ete initialis�e avec la taille reelle
		// du fichier (ne pas forcer a 0 car un fichier existe peut-etre deja)
		if (m_estimatedSize != -1)
		{
			m_estimatedSize += (LOG_OVERHEAD_SIZE + sbLogStr.length());
		}
		
		if ((m_estimatedSize == -1) || (m_estimatedSize > m_fileSizeThresh))
		{
			File file = null;
			String externalStorageState = Environment.getExternalStorageState();
			File externalStorageDirectory = Environment.getExternalStorageDirectory();
			
			if (externalStorageState.equals(Environment.MEDIA_MOUNTED) && externalStorageDirectory != null)
			{
				file = new File(externalStorageDirectory, LOG_FILENAME);
			}
			
			if (file == null)
			{
				// pb with file: reset size estimation and check next time
				m_estimatedSize = 0;
				Log.e(RAINBOW_TAG, "Problem with rolling file appender check");
			}
			else
			{
				// estimated size is now exact size !
				m_estimatedSize = file.length();
				
				if ((m_estimatedSize >= m_maxFileSize) && (!m_rolling))
				{
					m_rolling = true;
					rollOver();
					m_estimatedSize = 0;
					m_rolling = false;
				}
			}
		}
	}
	
	/**
	 * Rolling files
	 */
	private void rollOver()
	{
		File target;
		boolean renameSucceeded = true;
		String externalStorageState = Environment.getExternalStorageState();
		File externalStorageDirectory = Environment.getExternalStorageDirectory();
		
		Log.d(RAINBOW_TAG, "Rolling over log files...");
		
		// This test should always be true, but who knows...
		if (!externalStorageState.equals(Environment.MEDIA_MOUNTED) || externalStorageDirectory == null)
		{
			return;
		}
		
		// Delete the oldest file
		File file = new File(externalStorageDirectory, LOG_FILENAME + '.' + MAX_BACKUP_INDEX);
		if (file.exists())
			renameSucceeded = file.delete();
		
		// Map {(MAX_BACKUP_INDEX - 1), ..., 2, 1} to {MAX_BACKUP_INDEX, ..., 3, 2}
		for (int i = MAX_BACKUP_INDEX - 1; i >= 1 && renameSucceeded; i--)
		{
			file = new File(externalStorageDirectory, LOG_FILENAME + "." + i);
			if (file.exists())
			{
				target = new File(externalStorageDirectory, LOG_FILENAME + '.' + (i + 1));
				renameSucceeded = file.renameTo(target);
			}
		}
		
		if (renameSucceeded)
		{
			// Rename fileName to fileName.1
			target = new File(externalStorageDirectory, LOG_FILENAME + "." + 1);
			
			try
			{
				if (m_appender != null)
					m_appender.close();
			}
			catch (IOException e)
			{
				if (e instanceof InterruptedIOException)
				{
					Thread.currentThread().interrupt();
				}
				Log.e(RAINBOW_TAG, "Close file for log appending failed.", e);
			}
			
			file = new File(externalStorageDirectory, LOG_FILENAME);
			file.renameTo(target);
		}
		
		//
		// In any case (rename successful or not), reopen the file appender
		//
		if (m_appender != null)
		{
			try
			{
				m_appender.open();
			}
			catch (IOException e)
			{
				if (e instanceof InterruptedIOException)
				{
					Thread.currentThread().interrupt();
				}
				Log.e(RAINBOW_TAG, "Reopen file for log appending failed.", e);
			}
		}
		
		Log.d(RAINBOW_TAG, "Log files rolled over");
	}
	
	/**
	 * Rolling files
	 */
	private Level convertLevel(String level)
	{
		int levelValue;
		
		try
		{
			levelValue = Integer.valueOf(level);
		}
		catch (NumberFormatException e)
		{
			levelValue = Level.INFO_INT;
		}
		
		switch (levelValue)
		{
		case Level.FATAL_INT:
			return Level.FATAL;
		case Level.ERROR_INT:
			return Level.ERROR;
		case Level.WARN_INT:
			return Level.WARN;
		case Level.INFO_INT:
			return Level.INFO;
		case Level.DEBUG_INT:
			return Level.DEBUG;
		case Level.TRACE_INT:
			return Level.TRACE;
			
		default:
			break;
		}
		return DEFAULT_LOG_LEVEL;
	}
	
	@Override
	public String getLevelName()
	{
		return m_logLevel.name();
	}
}
