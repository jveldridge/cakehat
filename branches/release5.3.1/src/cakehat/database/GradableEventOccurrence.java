package cakehat.database;

import cakehat.database.assignment.GradableEvent;
import org.joda.time.DateTime;

/**
 * Stores the occurrence of a gradable event manually entered by a TA for a group's gradable event that does not have a
 * digital handin.
 *
 * @author jak2
 */
public class GradableEventOccurrence
{
    private final GradableEvent _gradableEvent;
    private final Group _group;
    private final TA _ta;
    private final DateTime _dateRecorded;
    private final DateTime _occurrenceDate;
    
    GradableEventOccurrence(GradableEvent gradableEvent, Group group, TA ta, DateTime dateRecorded,
            DateTime occurrenceDate)
    {
        //Validate arguments
        if(gradableEvent == null)
        {
            throw new NullPointerException("gradableEvent may not be null");
        }
        if(group == null)
        {
            throw new NullPointerException("group may not be null");
        }
        if(ta == null)
        {
            throw new NullPointerException("ta may not be null");
        }
        if(dateRecorded == null)
        {
            throw new NullPointerException("dateRecorded may not be null");
        }
        if(occurrenceDate == null)
        {
            throw new NullPointerException("occurrenceDate may not be null");
        }
        
        _gradableEvent = gradableEvent;
        _group = group;
        _ta = ta;
        _dateRecorded = dateRecorded;
        _occurrenceDate = occurrenceDate;
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

    public DateTime getOccurrenceDate()
    {
        return _occurrenceDate;
    }
}