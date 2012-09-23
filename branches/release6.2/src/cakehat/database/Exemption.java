package cakehat.database;

import cakehat.assignment.GradableEvent;
import org.joda.time.DateTime;

/**
 * An exemption for a {@link Group} for a {@link GradableEvent}.
 * 
 * @author jak2
 */
public class Exemption
{
    private final GradableEvent _gradableEvent;
    private final Group _group;
    private final TA _ta;
    private final DateTime _dateRecorded;
    private final String _note;
    
    Exemption(GradableEvent gradableEvent, Group group, TA ta, DateTime dateRecorded, String note)
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
        
        _gradableEvent = gradableEvent;
        _group = group;
        _ta = ta;
        _dateRecorded = dateRecorded;
        _note = note;
    }
    
    /**
     * The gradable event this exemption is for.
     * 
     * @return 
     */
    public GradableEvent getGradableEvent()
    {
        return _gradableEvent;
    }
    
    /**
     * The group this exemption is for.
     * 
     * @return 
     */
    public Group getGroup()
    {
        return _group;
    }
    
    /**
     * The TA that granted this exemption.
     * 
     * @return 
     */
    public TA getTA()
    {
        return _ta;
    }
    
    /**
     * The date and time this exemption was granted.
     * 
     * @return 
     */
    public DateTime getDateRecorded()
    {
        return _dateRecorded;
    }

    /**
     * Gets the note associated with this exemption. May be {@code null}.
     * 
     * @return 
     */
    public String getNote()
    {
        return _note;
    }
}