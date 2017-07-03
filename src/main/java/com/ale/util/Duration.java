/******************************************************************************
 * Copyright © 2010 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * File    : Duration.java
 * Author  : geyer2 8 oct. 2010
 * Summary : 
 ******************************************************************************
 * History
 * 8 oct. 2010  geyer2
 *  Creation
 *  2012/01/24 cebruckn crms00357468 [Favorites]-Sometimes List is not complete
 */

package com.ale.util;

/**
 * Duration constants definitions
 * 
 * @author geyer2
 * 
 */
public final class Duration
{
	/** Ten milliseconds given in number of MILLISECONDS */
	public static final long TEN_MILLISECONDS_IN_MILLISECONDS = 10;
	
	/** One hundred milliseconds given in number of MILLISECONDS */
	public static final int ONE_HUNDRED_MILLISECONDS_IN_MILLISECONDS = 100;
	
	/** Six hundred milliseconds given in number of MILLISECONDS */
	public static final int SIX_HUNDRED_MILLISECONDS_IN_MILLISECONDS = ONE_HUNDRED_MILLISECONDS_IN_MILLISECONDS * 6;
	
	/** Eight hundred milliseconds given in number of MILLISECONDS */
	public static final int EIGHT_HUNDRED_MILLISECONDS_IN_MILLISECONDS = ONE_HUNDRED_MILLISECONDS_IN_MILLISECONDS * 8;
	
	/** Half a second given in number of MILLISECONDS */
	public static final long HALF_A_SECOND_IN_MILLISECONDS = 500;
	
	/** One second given in number of MILLISECONDS */
	public static final long ONE_SECOND_IN_MILLISECONDS = 1000;
	
	/** Two seconds given in number of MILLISECONDS */
	public static final long TWO_SECONDS_IN_MILLISECONDS = 2000;

	/** Three seconds given in number of MILLISECONDS */
	public static final long THREE_SECONDS_IN_MILLISECONDS = 3000;

	/** Foor seconds given in number of MILLISECONDS */
	public static final long FOUR_SECONDS_IN_MILLISECONDS = 4000;

	/** Five seconds given in number of MILLISECONDS */
	public static final long FIVE_SECONDS_IN_MILLISECONDS = 5000;

	/** Ten seconds given in number of MILLISECONDS */
	public static final long TEN_SECONDS_IN_MILLISECONDS = 10000;

	/** Fifteen seconds given in number of MILLISECONDS */
	public static final long FIFTEEN_SECONDS_IN_MILLISECONDS = 15000;

	/** Twenty seconds given in number of MILLISECONDS */
	public static final long TWENTY_SECONDS_IN_MILLISECONDS = 20000;
	
	/** Twenty seconds given in number of MILLISECONDS */
	public static final long THIRTY_SECONDS_IN_MILLISECONDS = 30000;

	/** One minute given in number of MILLISECONDS */
	public static final long ONE_MINUTE_IN_MILLISECONDS = 60*1000;

	/** Two minutes given in number of MILLISECONDS */
	public static final long TWO_MINUTES_IN_MILLISECONDS = 2*ONE_MINUTE_IN_MILLISECONDS;

	/** Three minutes given in number of MILLISECONDS */
	public static final long THREE_MINUTES_IN_MILLISECONDS = 3*ONE_MINUTE_IN_MILLISECONDS;

	/** Four minutes given in number of MILLISECONDS */
	public static final long FOUR_MINUTES_IN_MILLISECONDS = 3*ONE_MINUTE_IN_MILLISECONDS;

	/** Five minutes given in number of MILLISECONDS */
	public static final long FIVE_MINUTES_IN_MILLISECONDS = 5*ONE_MINUTE_IN_MILLISECONDS;

	/** Ten minutes given in number of MILLISECONDS */
	public static final long TEN_MINUTES_IN_MILLISECONDS = 10*ONE_MINUTE_IN_MILLISECONDS;

	/** Thirty minutes given in number of MILLISECONDS */
	public static final long THIRTY_MINUTES_IN_MILLISECONDS = 30*ONE_MINUTE_IN_MILLISECONDS;

	/** One Hour given in number of MILLISECONDS */
	public static final long ONE_HOUR_IN_MILLISECONDS = 60*ONE_MINUTE_IN_MILLISECONDS;

	/** Two Hours given in number of MILLISECONDS */
	public static final long TWO_HOURS_IN_MILLISECONDS = 2*ONE_HOUR_IN_MILLISECONDS;

	/** Three Hours given in number of MILLISECONDS */
	public static final long THREE_HOURS_IN_MILLISECONDS = 3*ONE_HOUR_IN_MILLISECONDS;

	/** Four Hours given in number of MILLISECONDS */
	public static final long FOUR_HOURS_IN_MILLISECONDS = 4*ONE_HOUR_IN_MILLISECONDS;

	/** One DAY given in number of MILLISECONDS */
	public static final long ONE_DAY_IN_MILLISECONDS = 24*ONE_HOUR_IN_MILLISECONDS;

	/** One minute given in number of SECONDS */
	public static final long ONE_MINUTES_IN_SECONDS = 60;
	
	/** One hour given in number of SECONDS */
	public static final long ONE_HOUR_IN_SECONDS = 60*ONE_MINUTES_IN_SECONDS;

	/** Four hours given in number of SECONDS */
	public static final long FOUR_HOURS_IN_SECONDS = 4*ONE_HOUR_IN_SECONDS;

	/**
	 * Declare constructor as private (static class)
	 */
	private Duration()
	{
	}
}
