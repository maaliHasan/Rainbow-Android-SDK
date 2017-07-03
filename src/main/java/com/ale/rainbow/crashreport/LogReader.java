package com.ale.rainbow.crashreport;

import android.os.Environment;

import com.ale.rainbow.AndroidLogger;
import com.ale.util.log.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LogReader
{
	private static final String LOG_TAG = "LogReader";
	
	public static final String ABERS_LOG_FILENAME = "Abers.log";
	public static final String SIPPHONE_LOG_FILENAME = "sipphonelogTrace.txt";
	
	private static final String LOG_ROTATE_DOT_PATTERN = "%s.%d";
	private static final String LOG_ROTATE_UNDERSCORE_PATTERN = "%s_%d";
	
	public String getOtcLog(final int size)
	{
		StringBuilder builder = new StringBuilder();
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath();
		builder.append("===").append(AndroidLogger.FILENAME).append("===").append(System.getProperty("line.separator"));
		builder.append(getLog(size, path + "/" + AndroidLogger.LOG_FILENAME, LOG_ROTATE_DOT_PATTERN));
		
		return builder.toString();
	}
	
	/**
	 * Read logs from SDCard
	 * 
	 * @param size
	 *            the amount of log to read and return in byte
	 * @param logFileName
	 *            the name of the log file to read
	 * @param format
	 *            the format to use when the log rotate occurred (it should include a %s and a %d)
	 * @return last logs
	 */
	private String getLog(final int size, final String logFileName, final String format)
	{
		
		StringBuffer fullLog = new StringBuffer();
		
		try
		{
			for (int i = 0; i < AndroidLogger.MAX_BACKUP_INDEX; i++)
			{
				
				// file name of the log file: the first one is '.txt' and others are '.txt.x'
				String fileName = logFileName;
				
				if (i != 0)
					fileName = String.format(format, logFileName, i);
				
				File file = new File(fileName);
				List<String> currentLog = new ArrayList<String>();
				FileInputStream fileInput = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(new InputStreamReader(fileInput));
				
				// Read current log file
				String line;
				while ((line = reader.readLine()) != null)
				{
					currentLog.add(0, line);
				}
				reader.close();
				
				for (String log : currentLog)
				{
					if (log.length() + fullLog.length() < size)
						fullLog.insert(0, log + System.getProperty("line.separator"));
					else
						break;
				}
			}
		}
		catch (IOException e)
		{
			Log.getLogger().warn(LOG_TAG, "No enough log remain");
		}
		
		return fullLog.toString();
	}
}
