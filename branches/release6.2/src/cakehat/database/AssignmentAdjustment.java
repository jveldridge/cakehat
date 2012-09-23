package cakehat.database;

import cakehat.assignment.Assignment;
import org.joda.time.DateTime;

/**
 * An adjustment to an {@link Assignment} for a specific {@link Student}. This allows for a TA, likely a HTA, to
 * arbitrarily change the total earned points a student receives for an assignment. This operates on students, not
 * groups, which allows for a TA to give different total grades to different members of the same group.
 * 
 * @author jak2
 */
public class AssignmentAdjustment
{
    private final Assignment _assignment;
    private final Student _student;
    private final TA _ta;
    private final DateTime _dateRecorded;
    private final Double _points;
    private final String _note;
    
    AssignmentAdjustment(Assignment asgn, Student student, TA ta, DateTime dateRecorded, Double points, String note)
    {
        //Validate arguments
        if(asgn == null)
        {
            throw new NullPointerException("asgn may not be null");
        }
        if(student == null)
        {
            throw new NullPointerException("student may not be null");
        }
        if(ta == null)
        {
            throw new NullPointerException("ta may not be null");
        }
        if(dateRecorded == null)
        {
            throw new NullPointerException("dateRecorded may not be null");
        }
        
        _assignment = asgn;
        _student = student;
        _ta = ta;
        _dateRecorded = dateRecorded;
        _points = points;
        _note = note;
    }
    
    /**
     * The assignment this adjustment is for.
     * 
     * @return 
     */
    public Assignment getAssignment()
    {
        return _assignment;
    }
    
    /**
     * The student this adjustment is for.
     * 
     * @return 
     */
    public Student getStudent()
    {
        return _student;
    }
    
    /**
     * The TA who recorded this adjustment.
     * 
     * @return 
     */
    public TA getTA()
    {
        return _ta;
    }
    
    /**
     * The date and time this adjustment was recorded at.
     * 
     * @return 
     */
    public DateTime getDateRecorded()
    {
        return _dateRecorded;
    }
    
    /**
     * The amount of points associated with this adjustment. May be {@code null}.
     * 
     * @return 
     */
    public Double getPoints()
    {
        return _points;
    }
    
    /**
     * The note associated with this adjustment. May be {@code null}.
     * 
     * @return 
     */
    public String getNote()
    {
        return _note;
    }
}