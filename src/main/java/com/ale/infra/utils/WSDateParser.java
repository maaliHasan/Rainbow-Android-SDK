package com.ale.infra.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WSDateParser {

	private static final SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	public static Date parse(String val){
		
		try
		{
			ISO8601FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
			return ISO8601FORMAT.parse(val);
		}
		catch (ParseException e)
		{
			return null;
		}
	}
	
	@SuppressWarnings("deprecation")
	public static String toString(Date val){
		return 
			String.format("%1d4-%2d2-%3d2T%4d2:%5d2:%6d2Z", 
					val.getYear(),
					// java considers 0 as January
					val.getMonth()+1,
			        // java considers 0 as 1st of Month
					val.getDay()+1,
					val.getHours(),
					val.getMinutes(),
					val.getSeconds());
	}
	
}
