package cakehat.newdatabase;

import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import java.util.Set;
import org.joda.time.DateTime;

/**
 *
 * @author hdrosen
 */
public interface DataServicesV5 {
    
    /**
     * Returns an immutable Set snapshot containing a {@link Student} object for each student in the database at the
     * time {@link DataServicesV5#updateDataCache()} was called. If the database contained no students, an empty Set is
     * returned.
     * 
     * @return
     */
    public Set<Student> getStudents() throws ServicesException;
    
    /**
     * Returns an immutable Set view containing a {@link Student} object for each enabled student in the database at the
     * time {@link DataServicesV5#updateDataCache()} was called.  If the database contained no enabled students, an
     * empty Set is returned.
     *
     * @return
     */
    public Set<Student> getEnabledStudents() throws ServicesException;
    
    /**
     * Returns the TA who has been assigned to grade the given Group for the given Part. If no such TA exists,
     * {@code null} will be returned.
     * 
     * @param part
     * @param group
     * @return
     * @throws ServicesException
     */
    public TA getGrader(Part part, Group group) throws ServicesException;
 
    /**
     * Sets the TA who has been assigned to grade the given Group and  Part. Pass {@code ta} as {@code null} to
     * have the given group and part be unassigned.
     * 
     * @param part
     * @param group
     * @param ta
     * @throws ServicesException
     */
    public void setGrader(Part part, Group group, TA ta) throws ServicesException;
    
    /**
     * Returns the points earned for the given Group and Part. If no such value is stored in the database, {@code null}
     * will be returned.
     *
     * @param group
     * @param part
     * @return
     * @throws ServicesException
     */
    public PartGrade getEarned(Group group, Part part) throws ServicesException;
    
    /**
     * Sets the points earned and matches gml for the given Group and Part.
     * 
     * @param group
     * @param part
     * @param earned
     * @param matchesGml
     * @return
     * @throws ServicesException 
     */
    public void setEarned(Group group, Part part, Double earned, boolean matchesGml) throws ServicesException;
    
    /**
     * Returns an immutable set of all TAs.
     * 
     * @return
     */
    public Set<TA> getTAs() throws ServicesException;
    
    /**
     * Returns the deadline info for the given gradable event.
     * 
     * @param gradableEvent
     * @return
     * @throws ServicesException 
     */
    public DeadlineInfo getDeadlineInfo(GradableEvent gradableEvent) throws ServicesException;
    
    /**
     * Returns the handin time for the given gradable event and group.
     * 
     * @param gradableEvent
     * @param part
     * @return
     * @throws ServicesException 
     */
    public HandinTime getHandinTime(GradableEvent gradableEvent, Group group) throws ServicesException;
    
    
    /**
     * Sets the handin time for the given gradable event and group.
     * 
     * @param gradableEvent
     * @param part
     * @param handinTime
     * @throws ServicesException 
     */
    public void setHandinTime(GradableEvent gradableEvent, Group group, DateTime handinTime) throws ServicesException;
    
    /**
     * Loads Student and Group objects into memory for all students and groups in the database.
     *
     * @throws ServicesException
     */
    public void updateDataCache() throws ServicesException;
}
