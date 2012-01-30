package cakehat.newdatabase;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import java.util.Collection;
import java.util.List;
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
     * Adds the given newly created group to the database.  A ServicesException will be thrown if a group with that name
     * already exists for the corresponding assignment, if any member of the group is already assigned to another group
     * for the assignment, or if a database error occurred.
     * 
     * @param toAdd
     * @return 
     * @throws ServicesException 
     */
    public void addGroup(DbGroup toAdd) throws ServicesException;
    
    /**
     * Adds the given newly created groups to the database.  If a group with the same name as any of those to be added
     * already exists for the corresponding assignment, if a member of any group to be added is already assigned to
     * another group for the assignment, no groups will be added and a ServicesException will be thrown. A
     * ServicesException will also be thrown if a database error occurred.
     * 
     * @param toAdd
     * @throws ServicesException 
     */
    public void addGroups(Set<DbGroup> toAdd) throws ServicesException;
    
    /**
     * Returns the Group for which the given Student is a member for the given Assignment. If the given Assignment is
     * not a group assignment and the Student does not already have a group of one, a group of one will be
     * created, stored in the database, and returned. This method returns {@code null} if no such Group exists.
     *
     * @param asgn
     * @param student
     * @return
     * @throws ServicesException
     */
    public Group getGroup(Assignment asgn, Student student) throws ServicesException;
    
    /**
     * Returns all Groups that have been created for the given Assignment.  A
     * ServicesException will be thrown if an invalid group ID is present in the
     * database or if a database error occurred.  If the given Assignment is not
     * a group assignment, groups of one will be created and stored in the database
     * for each student who does not already have a group of one.  Returns an 
     * empty Set if the given Assignment is a group Assignment and no groups
     * have yet been created for it.
     * 
     * @param asgn
     * @return
     * @throws ServicesException 
     */
    public Set<Group> getGroups(Assignment asgn) throws ServicesException;
    
    /**
     * Removes from the database all groups for the given Assignment.  If no
     * groups had been previously created, this method has no effect.
     * 
     * @param asgn
     * @return
     * @throws ServicesException 
     */
    public void removeGroups(Assignment asgn) throws ServicesException;
    
    /**
     * Returns an immutable ordered list of all assignment.
     */
    public List<Assignment> getAssignments() throws ServicesException;
    
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
