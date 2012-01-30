package cakehat.database;

import cakehat.assignment.GradableEvent;
import org.joda.time.DateTime;

/**
 * Stores the handin time manually entered by a TA for a gradable event that does not have a digital handin directory.
 *
 * @author jak2
 */
public class HandinTime
{
    private final GradableEvent _gradableEvent;
    private final Group _group;
    private final TA _ta;
    private final DateTime _dateRecorded;
    private final DateTime _handinTime;
    
    HandinTime(GradableEvent gradableEvent, Group group, TA ta, DateTime dateRecorded, DateTime handinTime)
    {
        _gradableEvent = gradableEvent;
        _group = group;
        _ta = ta;
        _dateRecorded = dateRecorded;
        _handinTime = handinTime;
    }

    public GradableEvent getGradableEvent()
    {
        return _gradableEvent;
    }

    public Group getGroup()
    {
        return _group;
    }

    public TA getTA()
    {
        return _ta;
    }

    public DateTime getDateRecorded()
    {
        return _dateRecorded;
    }

    public DateTime getHandinTime()
    {
        return _handinTime;
    }
}