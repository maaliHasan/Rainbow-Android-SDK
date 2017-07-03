/******************************************************************************
 * Copyright © 2011 Alcatel-Lucent Enterprise, All rights reserved.
 * Project : MIC Mobile Android
 * Author  : geyer2 18 ao�t 2011
 * *****************************************************************************
 * Defects
 * crms000352333 from mm:ss to hh:mm:ss
 * crms000356331 from hh:mm:ss to mm:ss when duration < 1 hour
 * 2012/04/16 cebruckn crms00371526 [OXO] MyIC mobile Android - 'Pause' unavailable on playing a voicemail
 * 2013/01/09 cebruckn crms00414373 Call Grouping not 100% accurate
 */

package com.ale.util;

import com.ale.util.log.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * @author geyer2
 *
 */
public final class DateTimeUtil {

    private static final String LOG_TAG = "DateTimeUtil";

    public static int MILLISECONDS_PER_DAY = (24 * 60 * 60 * 1000);

    public static int ONE_SECONDS = 1;
    public static int TWO_SECONDS_ = 2;
    public static int THREE_SECONDS = 3;
    public static int FOUR_SECONDS = 4;
    public static int FIVE_SECONDS = 5;
    public static int TEN_SECONDS = 10;
    public static int ONE_MINUTE_IN_SECONDS = 60;
    public static int TWO_MINUTES_IN_SECONDS = 2 * ONE_MINUTE_IN_SECONDS;
    public static int ONE_HOUR_IN_SECONDS = 60 * ONE_MINUTE_IN_SECONDS;
    public static int TWO_HOURS_IN_SECONDS = 2 * ONE_HOUR_IN_SECONDS;
    public static int ONE_DAY_IN_SECONDS = 24 * ONE_HOUR_IN_SECONDS;
    public static int TWO_DAYS_IN_SECONDS = 2 * ONE_DAY_IN_SECONDS;
    public static final int ONE_MONTH_IN_SECONDS = 30 * ONE_DAY_IN_SECONDS;
    public static final int TWO_MONTHS_IN_SECONDS = 2 * ONE_MONTH_IN_SECONDS;
    public static final int ONE_YEAR_IN_SECONDS = 365 * ONE_DAY_IN_SECONDS;
    public static final int TWO_YEARS_IN_SECONDS = 2 * ONE_YEAR_IN_SECONDS;

    private static SimpleDateFormat m_formatForDateWithoutYear = null;

    private DateTimeUtil() {
        // This is a static class
    }

    public static int getNumberOfSecondsFromNow(Date previousDate) {
        if( previousDate == null ) {
            Log.getLogger().warn(LOG_TAG, "getNumberOfSecondsFromNow; given date is NULL");
            return 0;
        }
        Date now = new Date();
        long diff = (now.getTime() - previousDate.getTime());

        if (diff != 0)
            return (int) (diff / Duration.ONE_SECOND_IN_MILLISECONDS);
        else
            return 0;
    }

    public static int getNumberOfSecondsBetweenCalendarDates(Calendar end, Calendar start) {
        long diff = Math.abs(end.getTimeInMillis() - start.getTimeInMillis());
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diff);

        if (diffInSeconds > 0)
            return (int) diffInSeconds;
        else
            return 0;
    }

    public static int getNumberOfSecondsBetweenDates(Date date1, Date date2) {
        if( date1 == null || date2 == null) {
            Log.getLogger().warn(LOG_TAG, "given date is NULL");
            return 0;
        }
        long diff = Math.abs(date1.getTime() - date2.getTime());
        long diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diff);

        if (diffInSeconds > 0)
            return (int) diffInSeconds;
        else
            return 0;
    }

    public static int getNumberOfMilliSecondsBetweenDates(Date date1, Date date2) {
        long diff = Math.abs(date1.getTime() - date2.getTime());

        if (diff > 0)
            return (int) diff;
        else
            return 0;
    }

    public static int getDaysBetweenDates(Date startDate, Date endDate) {
        int nbYears = getYearsBetweenDates(startDate,endDate);

        Calendar startCalendar;
        Calendar endCalendar;
        if( startDate.getTime() > endDate.getTime()) {
            startCalendar = getCalendarDayDate(startDate);
            endCalendar = getCalendarDayDate(endDate);
        } else {
            startCalendar = getCalendarDayDate(endDate);
            endCalendar = getCalendarDayDate(startDate);
        }

        int startNbDays = startCalendar.get(Calendar.DAY_OF_YEAR);
        int endNbDays = endCalendar.get(Calendar.DAY_OF_YEAR);

        return Math.abs(startNbDays-endNbDays+nbYears*365);
    }

    public static int getMonthsBetweenDates(Date startDate, Date endDate) {
        int nbYears = getYearsBetweenDates(startDate,endDate);

        Calendar startCalendar;
        Calendar endCalendar;
        if( startDate.getTime() > endDate.getTime()) {
            startCalendar = getCalendarDayDate(startDate);
            endCalendar = getCalendarDayDate(endDate);
        } else {
            startCalendar = getCalendarDayDate(endDate);
            endCalendar = getCalendarDayDate(startDate);
        }

        int startNbMonth = startCalendar.get(Calendar.MONTH);
        int endNbMonth = endCalendar.get(Calendar.MONTH);

        return Math.abs(startNbMonth-endNbMonth+nbYears*12);
    }

    public static int getYearsBetweenDates(Date startDate, Date endDate) {
        Calendar startCalendar;
        Calendar endCalendar;
        if( startDate.getTime() > endDate.getTime()) {
            startCalendar = getCalendarDayDate(startDate);
            endCalendar = getCalendarDayDate(endDate);
        } else {
            startCalendar = getCalendarDayDate(endDate);
            endCalendar = getCalendarDayDate(startDate);
        }

        int startNbDays = startCalendar.get(Calendar.YEAR);
        int endNbDays = endCalendar.get(Calendar.YEAR);

        return Math.abs(endNbDays-startNbDays);
    }



    public static int getDatesDiffDays(Date last, Date givenNow) {
        Date now = givenNow;
        if (givenNow == null) {
            now = new Date();
        }

        if (last == null || (now.getTime() < last.getTime()))
            return 0;

        long nowDays = now.getTime() / MILLISECONDS_PER_DAY;
        long lastDays = last.getTime() / MILLISECONDS_PER_DAY;
        long diff = (nowDays - lastDays);
        return (int) diff;
    }

    public static Date getDateFromNow(long nbDaysBefore) {
        long nowMilliseconds = new Date().getTime();
        long milliseconds = nowMilliseconds - (MILLISECONDS_PER_DAY * nbDaysBefore);

        return new Date(milliseconds);
    }

    public static Date dateByAddingDuration(Date nextStartOccurrence, int durationType, int duration) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTime(nextStartOccurrence);

        calendar.add(durationType, duration);

        return calendar.getTime();
    }

    /*
    public static Date addSeconds(Date date, int nbSeconde) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, nbSeconde);
        return cal.getTime();
    }
    */

    /**
     * Format a date.
     *
     * @param theDate
     *            the date to format.
     * @return
     */
    public static String formatDate(Date theDate) {
        return SimpleDateFormat.getDateInstance().format(theDate);
    }

    /**
     * Format a date.
     *
     * @param theDate
     *            the date to format.
     * @return
     */
    public static String formatEventDateLong(Date theDate) {
        StringBuilder builder = new StringBuilder();
        builder.append(SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(theDate));
        builder.append("   ");
        builder.append(getShortTimeInstance(theDate));
        return builder.toString();
    }

    /**
     * Format a date.
     *
     * @param theDate
     *            the date to format.
     * @return
     */
    public static String formatEventDate(Date theDate) {
        if (theDate == null) {
            return "";
        }

        if (isDateBeforeToday(theDate)) {
            return getShortDateInstanceWithoutYears(theDate);
        } else {
            return getShortTimeInstance(theDate);
        }
    }

    public static String formatEventDateHourOnly(Date theDate) {
        SimpleDateFormat mySimpleHourFormat = new SimpleDateFormat("HH:mm:ss");

        return mySimpleHourFormat.format(theDate);
    }

    public static String getDelayFromLongValue(long time) {
        long currentTimeInMilliSeconds = System.currentTimeMillis();
        ;
        long sinceWhenUserIsOffline = currentTimeInMilliSeconds - (time * 1000);
        return formatDateForImMessage(new Date(sinceWhenUserIsOffline));
    }

    public static String formatDateForImMessage(Date theDate) {
        SimpleDateFormat mySimpleHourFormat;
        if (!isDateThisYear(theDate)) {
            mySimpleHourFormat = new SimpleDateFormat("d MMM yyyy");
        } else if (!isDateToday(theDate)) {
            mySimpleHourFormat = new SimpleDateFormat("d MMM HH:mm");
        } else {
            mySimpleHourFormat = new SimpleDateFormat("HH:mm");
        }
        return mySimpleHourFormat.format(theDate);
    }

    public static String formatForCompactDate(Date theDate) {
        SimpleDateFormat mySimpleHourFormat;
        if (theDate == null) return "";

        if (!isDateThisYear(theDate)) {
            mySimpleHourFormat = new SimpleDateFormat("dd/MM/yy");
        } else if (!isDateToday(theDate)) {
            mySimpleHourFormat = new SimpleDateFormat("d MMM");
        } else {
            mySimpleHourFormat = new SimpleDateFormat("HH:mm");
        }
        return mySimpleHourFormat.format(theDate);
    }


    public static String getMessageDate(Date theDate) {
        return new SimpleDateFormat("d MMM yyyy HH:mm").format(theDate);
    }

    public static boolean isDelayLessThan(Date theDate,int timeElapseInSeconds) {
        return getNumberOfSecondsFromNow(theDate) < timeElapseInSeconds;
    }

    public static Boolean isDatePast(Date theDate) {
        Date now = new Date();
        return (theDate.getTime() < now.getTime());
    }

    public static Boolean isDateBeforeToday(Date theDate) {
        Calendar calendar = getCalendarDayDate(theDate);

        Calendar calendarToday = getCalendarTodayDate();

        return !(calendarToday.compareTo(calendar) < 1);
    }

    public static Boolean isDateToday(Date theDate) {
        Calendar calendar = getCalendarDayDate(theDate);

        Calendar calendarToday = getCalendarTodayDate();

        return (calendarToday.compareTo(calendar) == 0);
    }

    public static Boolean isDateYesterday(Date theDate) {
        Calendar calendar = getCalendarDayDate(theDate);

        Calendar calendarYesterday = getCalendarTodayDate();
        calendarYesterday.add(Calendar.DAY_OF_MONTH, -1);

        return (calendarYesterday.compareTo(calendar) == 0);
    }

    public static Boolean isDateThisYear(Date theDate) {
        Calendar calendar = getCalendarDayDate(theDate);

        int todayYear = getCalendarTodayDate().get(Calendar.YEAR);
        int theDateYear = calendar.get(Calendar.YEAR);

        return (todayYear == theDateYear);
    }

    public static Boolean isDateTomorrow(Date theDate) {
        Calendar calendar = getCalendarDayDate(theDate);

        Calendar calendarTomorrow = getCalendarTodayDate();
        calendarTomorrow.add(Calendar.DAY_OF_MONTH, +1);

        return (calendarTomorrow.compareTo(calendar) == 0);
    }

    private static Calendar getCalendarTodayDate() {
        return getCalendarDayDate(null);
    }

    private static Calendar getCalendarDayDate(Date forcingDate) {
        Calendar calendarDayDate = Calendar.getInstance();
        if (null != forcingDate)
            calendarDayDate.setTimeInMillis(forcingDate.getTime());
        // Remove hour, minute and seconds ;
        calendarDayDate.add(Calendar.HOUR_OF_DAY, -calendarDayDate.get(Calendar.HOUR_OF_DAY));
        calendarDayDate.add(Calendar.MINUTE, -calendarDayDate.get(Calendar.MINUTE));
        calendarDayDate.add(Calendar.SECOND, -calendarDayDate.get(Calendar.SECOND));
        calendarDayDate.add(Calendar.MILLISECOND, -calendarDayDate.get(Calendar.MILLISECOND));
        return calendarDayDate;
    }

    public static Boolean isDateThisWeek(Date theDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(theDate.getTime());

        Calendar calendarThisWeek = Calendar.getInstance();
        return calendarThisWeek.get(Calendar.WEEK_OF_YEAR) == calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static Boolean isDateLastWeek(Date theDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(theDate.getTime());

        Calendar calendarLastWeek = Calendar.getInstance();
        return calendarLastWeek.get(Calendar.WEEK_OF_YEAR) - 1 == calendar.get(Calendar.WEEK_OF_YEAR);
    }

    /**
     * Construct a new Date object
     */
    public static Date getDate(int year, int month, int day, int hour, int min, int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, min, sec);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    public static Integer getDateField(Date date, int field) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date);

        return calendar.get(field);
    }

    public static String getDateAndTimeInstance(Date theDate) {
        return SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT).format(theDate);
    }

    public static String getShortTimeInstance(Date theDate) {
        return SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT).format(theDate);
    }

    public static String getShortDateInstanceWithoutYears(Date theDate) {
        if (m_formatForDateWithoutYear == null) {
            m_formatForDateWithoutYear = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
            m_formatForDateWithoutYear.applyPattern(m_formatForDateWithoutYear.toPattern().replaceAll("[^\\p{Alpha}]*y+[^\\p{Alpha}]*", ""));
        }

        return m_formatForDateWithoutYear.format(theDate);
    }

    public static void resetFormatForDateWithoutYear() {
        m_formatForDateWithoutYear = null;
    }

    public static String getSimpleDateStringFromDateInstance(Date theDate) {
        return getShortDateInstanceWithoutYears(theDate) + " " + getShortTimeInstance(theDate);
    }

    public static String getDateInstanceWithTimeZone(Calendar theDate) {
        DateFormat dateInstance = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
        dateInstance.setTimeZone(theDate.getTimeZone());

        return dateInstance.format(new Date(theDate.getTimeInMillis()));
    }

    public static String getShortTimeInstanceWithTimeZone(Calendar theDate) {
        DateFormat timeInstance = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);
        timeInstance.setTimeZone(theDate.getTimeZone());

        return timeInstance.format(new Date(theDate.getTimeInMillis()));
    }

    public static Date getDateFromStringStamp(String stamp) {
        String stampWithoutMs = stamp;
        if (stamp.indexOf('.') > 0)
            stampWithoutMs = stamp.substring(0, stamp.lastIndexOf('.'));
        TimeZone tz = TimeZone.getTimeZone("GMT");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        df.setTimeZone(tz);
        try {
            return df.parse(stampWithoutMs);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Long getStampFromStringDate(String stamp) {
        String pattern = "yyyy-MM-dd HH:mm:ss.SSS";
        String stampFiltered = stamp;
        if( stamp.contains("T")) {
            stampFiltered = stamp.replace('T', ' ');
        }
        if( stamp.length()> pattern.length()) {
            stampFiltered = stampFiltered.substring(0, pattern.length());
        }
        TimeZone tz = TimeZone.getTimeZone("GMT");

        DateFormat df = new SimpleDateFormat(pattern);
        df.setTimeZone(tz);
        try {
            return df.parse(stampFiltered).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date().getTime();
        }
    }

    public static String getStringStampFromDate(Date date) {
        DateFormat stampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        return stampFormat.format(date);
    }

    public static String getDurationFromMs(String durationMs) {
        if (StringsUtil.isNullOrEmpty(durationMs)) {
            return "";
        }

        int duration = Integer.valueOf(durationMs)/1000;
        if( duration > DateTimeUtil.ONE_HOUR_IN_SECONDS ) {
            int nbHour = duration / DateTimeUtil.ONE_HOUR_IN_SECONDS;
            duration = duration - nbHour*DateTimeUtil.ONE_HOUR_IN_SECONDS;
            int nbMinutes = duration / DateTimeUtil.ONE_MINUTE_IN_SECONDS;
            duration = duration - nbMinutes*DateTimeUtil.ONE_MINUTE_IN_SECONDS;
            int nbSeconds = duration;

            return String.format("%dH %02dm %02ds",nbHour, nbMinutes, nbSeconds);
        } else if( duration > DateTimeUtil.ONE_MINUTE_IN_SECONDS ) {
            int nbMinutes = duration / DateTimeUtil.ONE_MINUTE_IN_SECONDS;
            duration = duration - nbMinutes*DateTimeUtil.ONE_MINUTE_IN_SECONDS;
            int nbSeconds = duration;

            return String.format("%dm %02ds", nbMinutes, nbSeconds);
        }
        return String.format("%ds", duration);
    }
}
