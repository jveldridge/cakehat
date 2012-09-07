package cakehat.database;

import cakehat.assignment.Part;
import org.joda.time.DateTime;

/**
 * Grade information for a group and part.
 *
 * @author jak2
 */
public class PartGrade
{
    private final Part _part;
    private final Group _group;
    private final TA _ta;
    private final DateTime _dateRecorded;
    private final Double _earned;
    private final boolean _submitted;
    
    PartGrade(Part part, Group group, TA ta, DateTime dateRecorded, Double earned, boolean submitted)
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
        _earned = earned;
        _submitted = submitted;
    }
    
    /**
     * The part this grade is for.
     * 
     * @return 
     */
    public Part getPart()
    {
        return _part;
    }
    
    /**
     * The group this grade is for.
     * 
     * @return 
     */
    public Group getGroup()
    {
        return _group;
    }
    
    /**
     * The TA which recorded this grade.
     * 
     * @return 
     */
    public TA getTA()
    {
        return _ta;
    }
    
    /**
     * The date and time this grade was recorded at.
     * 
     * @return 
     */
    public DateTime getDateRecorded()
    {
        return _dateRecorded;
    }
    
    /**
     * The amount of points earned by the group for the part. Will be {@code null} if no grade has yet been recorded
     * for this group and part. If {@link #isSubmitted()} returns {@code false} then this value is not the final earned
     * value for the group and part.
     * 
     * @return 
     */
    public Double getEarned()
    {
        return _earned;
    }
    
    /**
     * Whether this grade has been submitted.
     * 
     * @return 
     */
    public boolean isSubmitted()
    {
       return _submitted; 
    }
}