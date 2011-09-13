package support.utils;

import java.util.Calendar;

/**
 * Utility methods for dealing with <code>Calendar</code>s.
 */
public interface CalendarUtilities
{
    /**
     * Returns the current year.
     *
     * @return
     */
    public int getCurrentYear();

    /**
     * Turns a calendar into a String. Returned in the format
     * YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     *
     * @param entry
     * @return date and time formatted as YEAR-MONTH-DAY HOUR:MINUTE:SECOND
     */
    public String getCalendarAsString(Calendar entry);

     /**
      * Turns a calendar into a String.  Returned in the format
      * HOUR:MINUTE MONTH-DAY-YEAR
      *
      * @param entry
      * @return date and time formatted as HOUR:MINUTE MONTH-DAY-YEAR
      */
    public String getCalendarAsHandinTime(Calendar entry);

    /**
     * Turns a calendar into a String.  Returned in format YEAR-MONTH-DAY
     *
     * @param entry
     * @return date formatted as YEAR-MONTH-DAY
     */
    public String getDateAsString(Calendar entry);

    /**
     * Turns the time from a Calendar into a String.
     * Returned in format HOUR:MINUTE:SECOND
     *
     * @param entry
     * @return time formatted as HOUR:MINUTE:SECOND
     */
    public String getTimeAsString(Calendar entry);

    /**
     * Returns a Calendar from the Strings passed in.
     *
     * @param year
     * @param month
     * @param day
     * @param time formated as HOUR:MINUTE:SECOND
     * @return
     */
    public Calendar getCalendar(String year, String month, String day, String time);

    /**
     * Determines if a calendar, given a certain amount of leniency, is
     * before the deadline.
     *
     * @param toCheck the calendar to check if it is before the deadline
     * @param deadline the deadline
     * @param minutesOfLeniency the amount of leniency in minutes to be granted after the deadline
     * @return
     */
    public boolean isBeforeDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency);

    /**
     * Number of days, given a certain amount of leniency, that is after
     * the deadline.
     *
     * @param toCheck the calendar to check how many days after the deadline
     * @param deadline the deadline
     * @param minutesOfLeniency the amount of leniency in minutes to be granted after the deadline
     * @return number of days
     */
    public int daysAfterDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency);
}