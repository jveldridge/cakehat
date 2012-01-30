package cakehat.newdatabase;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;


/**
 *
 * @author hdrosen
 */
public interface DataServicesV5 {
    
    public enum ValidityCheck {BYPASS, CHECK};
    
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
     * Sets the given Student's enabled status.  Students are enabled by default,
     * but students who have dropped the course should be disabled.  Disabled students
     * will not be sent grade reports.
     *
     * @param student
     * @param enabled
     * @throws ServicesException
     */
    public void setStudentEnabled(Student student, boolean enabled) throws ServicesException;
    
    /**
     * Adds all the students in the given Set to the blacklist of the given TA, 
     * if not already blacklisted. This ensures that this TA will not be distributed
     * any of these students to grade. If a student was already blacklisted, that 
     * student will not be added again.
     *
     * @param students
     * @param ta
     * @throws ServicesException
     */
    public void blacklistStudents(Set<Student> students, TA ta) throws ServicesException;
    
    /**
     * For each Student in the given Set, removes the student from the given
     * TA's blacklist if the student was previously blacklisted.  This method has
     * no effect for students who were not previously on the TA's blacklist.
     *
     * @param students
     * @param ta
     * @throws ServicesException
     */
    public void unBlacklistStudents(Set<Student> students, TA ta) throws ServicesException;
    
    /**
     * Returns a Collection containing the Student object representing each student
     * who has been blacklisted by some TA.  If no students have been blacklisted,
     * an empty Collection will be returned.
     * 
     * @return
     * @throws ServicesException 
     */
    public Collection<Student> getBlacklistedStudents() throws ServicesException;
    
    /**
     * Returns a Collection containing the Student object representing each student
     * who has been blacklisted by the given TA.  If the TA has not blacklisted any
     * students, an empty Collection will be returned.
     * 
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Collection<Student> getBlacklist(TA ta) throws ServicesException;
    
    /**
     * Indicates whether the Assignment has a distribution.  Returns true if
     * the Assignment currently has no distribution, and false if it does.
     *
     * @param asgn
     * @return
     * @throws ServicesException 
     */
    public boolean isDistEmpty(Assignment asgn) throws ServicesException;
    
    /**
     * Returns a map that maps a TA to a Collection of Groups that TA has been
     * assigned to grade for the given DistributablePart.  There will be an entry
     * in the map for each TA; if a TA has not been assigned any groups to grade,
     * the value for that TA's entry in the map will be an empty Collection.
     * 
     * @param dp
     * @return
     * @throws ServicesException 
     */
    public Map<TA, Collection<Group>> getDistribution(Part dp) throws ServicesException;
    
    /**
     * Assigns Groups for each TA to grade for each Part.  Any existing
     * distributions will be overwritten.
     *
     * @param distribution
     * @throws ServicesException 
     */
    public void setDistribution(Map<Part, Map<TA, Collection<Group>>> distribution) throws ServicesException;
    
    /**
     * Assigns the given Group to the given TA to grade for the given Part.  
     * This will enable the TA to open, run, grade, etc. the Group's code for 
     * the given Part.  If the group is already assigned to the TA for the Part, 
     * this method has no effect. The Group will first be unassigned from any TA 
     * to which it has previously been assigned for the Part.
     *
     * NOTE: This method should not be used to create an initial automated distribution
     *       for a project; it should be used only to assign grading manually.
     *       To create an initial distribution, use {@link DataServices#setDistribution(java.util.Map)}.
     *
     * @param group
     * @param part
     * @param ta
     * @throws ServicesException
     */
    public void assignGroup(Group group, Part part, TA ta) throws ServicesException;

    /**
     * Unassigns Group group from the given TA for Part part.  If the Group was not
     * previously assigned to the TA to grade for the Part, this method has no effect.
     *
     * @param group
     * @param part
     * @param ta
     * @throws ServicesException 
     */
    public void unassignGroup(Group group, Part part, TA ta) throws ServicesException;

    /**
     * Returns a Collection of Groups that the given TA is assigned to grade for
     * the given Part part.  Returns an empty Collection if no groups are assigned 
     * to the TA for the given Part or if there is no distribution for the Part 
     * in the database.
     *
     * @param part
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Collection<Group> getAssignedGroups(Part part, TA ta) throws ServicesException;

    /**
     * Returns a Collection of Groups that have been assigned to any TA
     * to grade for the given Part.  This can be used to find students who
     * have not been assigned to any TA to grade.  If no distribution exists
     * yet, an empty Collection will be returned.
     *
     * @param part
     * @return
     * @throws ServicesException 
     */
    public Collection<Group> getAssignedGroups(Part part) throws ServicesException;
    
    /**
     * Returns a Collection of Parts for which the given TA has been
     * assigned at least one Group to grade.
     *
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Set<Part> getDPsWithAssignedGroups(TA ta) throws ServicesException;
    
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
    
    public TA getTA(int taId) throws ServicesException;
    
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
     * For each entry in the given Map, stores the corresponding HandinStatus in
     * the database for the corresponding Group and given Handin.  Any existing
     * handin statuses for Groups with entries in the Map will be overwritten.
     *
     * @param handin
     * @param statuses
     */
    public void setHandinTimes(GradableEvent gradableEvent, Map<Group, DateTime> statuses) throws ServicesException;
    
    /**
     * Loads Student and Group objects into memory for all students and groups in the database.
     *
     * @throws ServicesException
     */
    public void updateDataCache() throws ServicesException;
    
    /**
     * Returns the Student object corresponding to the given studentLogin.
     * If no such student exists in the database, <code>null</code> will be
     * returned.
     *
     * @param studentLogin
     * @return
     */
    public Student getStudentFromLogin(String studentLogin) throws ServicesException;

    /**
     * Returns whether or not the given student login corresponds to
     * a valid Student object.
     * 
     * @param studentLogin
     * @return
     */
    public boolean isStudentLoginInDatabase(String studentLogin) throws ServicesException;
    
    /**
     * Removes all data from database tables and rebuilds the tables. If no DB
     * file exists or is empty then it will be set to the initial configuration.
     */
    public void resetDatabase() throws ServicesException;
}
