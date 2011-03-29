package cakehat.config;

import java.util.Calendar;

/**
 * Represents the time information a {@link gradesystem.handin.Handin} has.
 *
 * @author jak2
 */
public class TimeInformation
{
    private final LatePolicy _latePolicy;
    private final GradeUnits _units;
    private final boolean _affectAll, _ecIfLate;
    private Calendar _early, _ontime, _late;
    private int _earlyValue, _ontimeValue, _lateValue;

    TimeInformation(LatePolicy policy, GradeUnits units, boolean affectAll, boolean ecIfLate)
    {
        _latePolicy = policy;
        _units = units;
        _affectAll = affectAll;
        _ecIfLate = ecIfLate;
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