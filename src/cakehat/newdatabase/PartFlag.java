package cakehat.newdatabase;

import cakehat.assignment.Part;
import org.joda.time.DateTime;

/**
 * Flags a {@link Group}'s {@link Part}. A flag is set by TA to indicate that this part should be reviewed. A note may
 * be recorded with the flag.
 *
 * @author jak2
 */
public class PartFlag
{
    private final Part _part;
    private final Group _group;
    private final TA _ta;
    private final DateTime _dateRecorded;
    private final String _note;
    
    PartFlag(Part part, Group group, TA ta, DateTime dateRecorded, String note)
    {
        //Validate arguments
        if(part == null)
        {
            throw new NullPointerException("part may not be null");
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
        
        _part = part;
        _group = group;
        _ta = ta;
        _dateRecorded = dateRecorded;
        _note = note;
    }
    
    /**
     * The part flagged.
     * 
     * @return 
     */
    public Part getPart()
    {
        return _part;
    }
    
    /**
     * The group flagged.
     * 
     * @return 
     */
    public Group getGroup()
    {
        return _group;
    }
    
    /**
     * The TA that flagged this group for this assignment.
     * 
     * @return 
     */
    public TA getTA()
    {
        return _ta;
    }
    
    /**
     * The date and time this flag was recorded.
     * 
     * @return 
     */
    public DateTime getDateRecorded()
    {
        return _dateRecorded;
    }

    /**
     * Gets the note associated with this flag. May be {@code null}.
     * 
     * @return 
     */
    public String getNote()
    {
        return _note;
    }
}