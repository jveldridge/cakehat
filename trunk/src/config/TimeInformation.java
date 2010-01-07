package config;

import java.util.Calendar;

/**
 *
 * @author jak2
 */
public class TimeInformation
{
    private LatePolicy _latePolicy;
    private GradeUnits _units;
    private Calendar _early, _ontime, _late;
    private int _earlyValue, _ontimeValue, _lateValue;

    public LatePolicy getLatePolicy()
    {
        return _latePolicy;
    }

    void setLatePolicy(LatePolicy policy)
    {
        _latePolicy = policy;
    }

    public GradeUnits getGradeUnits()
    {
        return _units;
    }

    void setGradeUnits(GradeUnits units)
    {
        _units = units;
    }

    public int getEarlyValue()
    {
        return _earlyValue;
    }

    void setEarlyValue(int value)
    {
        _earlyValue = value;
    }

    public Calendar getEarlyDate()
    {
        return _early;
    }

    void setEarlyDate(Calendar date)
    {
        _early = date;
    }

    public int getOntimeValue()
    {
        return _ontimeValue;
    }

    void setOntimeValue(int value)
    {
        _ontimeValue = value;
    }

    public Calendar getOntimeDate()
    {
        return _ontime;
    }

    void setOntimeDate(Calendar date)
    {
        _ontime = date;
    }

    public int getLateValue()
    {
        return _lateValue;
    }

    void setLateValue(int value)
    {
        _lateValue = value;
    }

    public Calendar getLateDate()
    {
        return _late;
    }

    void setLateDate(Calendar date)
    {
        _late = date;
    }
    
}