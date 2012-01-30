package cakehat.newdatabase;

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
    
    private final SubmissionStatus _status;
    
    PartGrade(Part part, Group group, TA ta, DateTime dateRecorded, Double earned, boolean inSync)
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
        
        _status = determineSubmissionStatus(earned, inSync, group, part);
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
     * for this group and part.
     * 
     * @return 
     */
    public Double getEarned()
    {
        return _earned;
    }
    
    /**
     * Gets the status of this grade in the database relative to the grading sheet.
     * 
     * @return 
     */
    public SubmissionStatus getSubmissionStatus()
    {
        return _status;
    }
    
    private static SubmissionStatus determineSubmissionStatus(Double earned, boolean inSync, Group group, Part part)
    {
        SubmissionStatus status = null;
        if(earned == null)
        {
            if(inSync)
            {
                throw new IllegalStateException("database has no recorded grade earned, but database has recorded " +
                        "insync as true\n"+
                        "Group: " + group.getName() + " [" + group.getId() + "]\n" +
                        "Part: " + part.getFullDisplayName() + " [" + part.getId() + "]");
            }
            else
            {
                status = SubmissionStatus.NOT_SUBMITTED_NOT_MATCHING;
            }
        }
        else
        {
            if(inSync)
            {
                status = SubmissionStatus.SUBMITTED_MATCHING;
            }
            else
            {
                status = SubmissionStatus.SUBMITTED_NOT_MATCHING;
            }
        }
        
        return status;
    }
    
    public static enum SubmissionStatus
    {   
        /**
         * The grading sheet has been modified and has not been submitted.
         * <br/><br/>
         * Not applicable when not using a GML file.
         */
        NOT_SUBMITTED_NOT_MATCHING,
        
        /**
         * The grading sheet has been submitted and has not been modified since.
         * <br/><br/>
         * OR
         * <br/><br/>
         * There is no grading sheet and a grade has been submitted.
         */
        SUBMITTED_MATCHING,
        
        /**
         * The grading sheet has been submitted and has been modified since.
         * <br/><br/>
         * Not applicable when not using a GML file.
         */
        SUBMITTED_NOT_MATCHING
    }
}