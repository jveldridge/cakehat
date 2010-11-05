package config;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import utils.Allocator;

/**
 * Represents the time information a CodePart has.
 *
 * @author jak2
 */
public class TimeInformation
{
    private LatePolicy _latePolicy;
    private GradeUnits _units;
    private Calendar _early, _ontime, _late;
    private int _earlyValue, _ontimeValue, _lateValue;
    private boolean _affectAll, _ecIfLate;

    TimeInformation(LatePolicy policy, GradeUnits units, boolean affectAll, boolean ecIfLate)
    {
        _latePolicy = policy;
        _units = units;
        _affectAll = affectAll;
        _ecIfLate = ecIfLate;
    }

    /**
     * Determines if the dates are reasonable. Reasonable is determined
     * as being in the same calendar year.
     *
     * @param writer to write error messages to
     * @param part in order to give helpful error messages
     * @return whether dates are reasonable
     */
    boolean areDatesReasonable(StringWriter writer, HandinPart part)
    {
        String msgBeginning = part.getAssignment().getName() + " - " + part.getName() +
                              "'s";

        Calendar thisYear = GregorianCalendar.getInstance();
        thisYear.set(Calendar.YEAR, Allocator.getCalendarUtilities().getCurrentYear());
        thisYear.set(Calendar.MONTH, 0);
        thisYear.set(Calendar.DAY_OF_MONTH, 1);
        thisYear.set(Calendar.HOUR_OF_DAY, 0);
        thisYear.set(Calendar.MINUTE, 0);
        thisYear.set(Calendar.SECOND, 0);
        thisYear.set(Calendar.MILLISECOND, 0);

        boolean valid = true;

        if(_early != null && _early.before(thisYear))
        {
            valid = false;

            writer.append(msgBeginning + " EARLY date is likely incorrect." +
                          " Date specified: " + Allocator.getCalendarUtilities().getCalendarAsString(_early) + "\n");
        }
        if(_ontime != null && _ontime.before(thisYear))
        {
            valid = false;

            writer.append(msgBeginning + " ONTIME date is likely incorrect." +
                          " Date specified: " + Allocator.getCalendarUtilities().getCalendarAsString(_ontime) + "\n");
        }
        if(_late != null && _late.before(thisYear))
        {
            valid = false;

            writer.append(msgBeginning + " LATE date is likely incorrect." +
                          " Date specified: " + Allocator.getCalendarUtilities().getCalendarAsString(_late) + "\n");
        }

        return valid;
    }

    public boolean ecIfLate()
    {
        return _ecIfLate;
    }

    public boolean affectsAll()
    {
        return _affectAll;
    }

    public LatePolicy getLatePolicy()
    {
        return _latePolicy;
    }

    public GradeUnits getGradeUnits()
    {
        return _units;
    }

    // EARLY

    void setEarly(Calendar cal, int value)
    {
        _early = cal;
        _earlyValue = value;
    }

    public int getEarlyValue()
    {
        return _earlyValue;
    }

    public Calendar getEarlyDate()
    {
        return _early;
    }

    // ONTIME

    void setOntime(Calendar cal, int value)
    {
        _ontime = cal;
        _ontimeValue = value;
    }

    public int getOntimeValue()
    {
        return _ontimeValue;
    }

    public Calendar getOntimeDate()
    {
        return _ontime;
    }

    // LATE

    void setLate(Calendar cal, int value)
    {
        _late = cal;
        _lateValue = value;
    }

    public Calendar getLateDate()
    {
        return _late;
    }
    public int getLateValue()
    {
        return _lateValue;
    }
    
}