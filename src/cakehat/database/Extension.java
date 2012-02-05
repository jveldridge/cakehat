package cakehat.database;

import cakehat.database.assignment.GradableEvent;
import org.joda.time.DateTime;

/**
 * An extension granted to a {@link Group} for a {@link GradableEvent}.
 *
 * @author jak2
 */
public class Extension
{
    private final GradableEvent _gradableEvent;
    private final Group _group;
    private final TA _ta;
    private final DateTime _dateRecorded;
    private final DateTime _onTime;
    private final boolean _shiftDates;
    private final String _note;
    
    Extension(GradableEvent gradableEvent, Group group, TA ta, DateTime dateRecorded, DateTime onTime,
              boolean shiftDates, String note)
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
        if(onTime == null)
        {
            throw new NullPointerException("onTime may not be null");
        }
        
        _gradableEvent = gradableEvent;
        _group = group;
        _ta = ta;
        _dateRecorded = dateRecorded;
        _onTime = onTime;
        _shiftDates = shiftDates;
        _note = note;
    }
    
    /**
     * The gradable event this extension is for.
     * 
     * @return 
     */
    public GradableEvent getGradableEvent()
    {
        return _gradableEvent;
    }
    
    /**
     * The group this extension is for.
     * 
     * @return 
     */
    public Group getGroup()
    {
        return _group;
    }
    
    /**
     * The TA that granted this extension.
     * 
     * @return 
     */
    public TA getTA()
    {
        return _ta;
    }
    
    /**
     * The date and time this extension was granted.
     * 
     * @return 
     */
    public DateTime getDateRecorded()
    {
        return _dateRecorded;
    }
    
    /**
     * The new on time date the group was given for this gradable event.
     * 
     * @return 
     */
    public DateTime getNewOnTime()
    {
        return _onTime;
    }

    /**
     * Whether any other deadline dates should be shifted relative to the new on time deadline. If {@code true} they
     * will be shifted, if {@code false} the gradable event will be handled as if it only has an on time deadline.
     * 
     * @return 
     */
    public boolean getShiftDates()
    {
        return _shiftDates;
    }

    /**
     * Gets the note associated with this extension. May be {@code null}.
     * 
     * @return 
     */
    public String getNote()
    {
        return _note;
    }
}