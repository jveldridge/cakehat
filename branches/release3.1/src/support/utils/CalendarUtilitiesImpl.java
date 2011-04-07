package support.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CalendarUtilitiesImpl implements CalendarUtilities
{
    public int getCurrentYear()
    {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public String getCalendarAsString(Calendar entry)
    {
        if (entry == null)
        {
            return "";
        }

        return this.getDateAsString(entry) + " " + this.getTimeAsString(entry);
     }

    public String getCalendarAsHandinTime(Calendar entry)
    {
         if (entry == null)
         {
             return "";
         }

         return entry.get(Calendar.HOUR_OF_DAY)
                 + ":" + ensureLeadingZero(entry.get(Calendar.MINUTE))
                 + " " + (entry.get(Calendar.MONTH) + 1)
                 + "-" + entry.get(Calendar.DAY_OF_MONTH)
                 + "-" + entry.get(Calendar.YEAR);
     }

    public String getDateAsString(Calendar entry)
    {
        if (entry == null)
        {
            return "";
        }
        return entry.get(Calendar.YEAR)
                + "-" + ensureLeadingZero(entry.get(Calendar.MONTH) + 1)
                + "-" + ensureLeadingZero(entry.get(Calendar.DAY_OF_MONTH));
    }

    public String getTimeAsString(Calendar entry)
    {
        if (entry == null)
        {
            return "";
        }

        return ensureLeadingZero(entry.get(Calendar.HOUR_OF_DAY))
                + ":" + ensureLeadingZero(entry.get(Calendar.MINUTE))
                + ":" + ensureLeadingZero(entry.get(Calendar.SECOND));
     }

    /**
     * Helper method for getCalendarAsString(...) to ensure that a 1 digit
     * number is returned with a leading zero when turned into a String.
     *
     * @param number
     * @return
     */
    private String ensureLeadingZero(int number)
    {
        String numberS = number + "";

        if (numberS.length() != 2)
        {
            return "0" + numberS;
        }

        return numberS;
    }

    public Calendar getCalendar(String year, String month, String day, String time)
    {
        Calendar cal = new GregorianCalendar();

        //Try to convert all of the entries
        int yearI = 0, monthI = 0, dayI = 0, hourI = 0, minuteI = 0, secondI = 0;
        try
        {
            if (year != null && year.length() != 0)
            {
                yearI = Integer.valueOf(year);
            }
            if (month != null && month.length() != 0)
            {
                monthI = Integer.valueOf(month);
            }
            if (day != null && day.length() != 0)
            {
                dayI = Integer.valueOf(day);
            }

            if (time != null)
            {
                String[] timeParts = time.split(":");
                if (timeParts.length == 3)
                {
                    hourI = Integer.valueOf(timeParts[0]);
                    minuteI = Integer.valueOf(timeParts[1]);
                    secondI = Integer.valueOf(timeParts[2]);
                }
            }
        }
        catch (Exception e) { }

        //Set fields
        monthI--; //Because months are zero indexed
        cal.set(yearI, monthI, dayI, hourI, minuteI, secondI);

        return cal;
    }

    public boolean isBeforeDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency)
    {
        deadline = ((Calendar) deadline.clone());
        deadline.add(Calendar.MINUTE, minutesOfLeniency);

        return toCheck.before(deadline);
    }

    public int daysAfterDeadline(Calendar toCheck, Calendar deadline, int minutesOfLeniency)
    {
        deadline = ((Calendar) deadline.clone());
        deadline.add(Calendar.MINUTE, minutesOfLeniency);

        //If to check is before the deadline
        if(toCheck.before(deadline))
        {
            return 0;
        }

        int daysLate = 0;

        // Look ahead 1000 days, to prevent infinite looping if really far apart days are passed in
        for(int i = 0; i < 1000; i++)
        {
            if(toCheck.after(deadline))
            {
                daysLate++;
                deadline.add(Calendar.HOUR, 24);
            }
            else
            {
                break;
            }
        }

        return daysLate;
    }
}