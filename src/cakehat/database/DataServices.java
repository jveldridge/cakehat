package cakehat.database;

import cakehat.config.Assignment;
import cakehat.config.Part;
import cakehat.config.TA;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
import cakehat.services.ServicesException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Services methods relating to fundamental data.  These methods are ultimately
 * backed by the database (or other data store) and the methods of {@link Database},
 * but are expected to implement caching.  In addition, these methods may perform
 * additional logic, including calls to other Services classes, to perform data validation
 * and to ensure synchronization with the backing data source.
 * <br/><br/>
 * <strong>All client code should use the methods of this interface rather than
 * those of <code>Database</code>.</strong>
 * 
 * @author jeldridg
 */
public interface DataServices {

    public enum ValidityCheck {BYPASS, CHECK};

    /**
     * Returns an immutable Collection containing a Student object for each student
     * in the database at the time {@link DataServices#updateDataCache()} was called.
     * If the database contained no students, an empty Collection is returned.  Note
     * that the returned Collection is like a Set in that all elements are unique.
     * 
     * @param slcl
     * @return
     */
    public Collection<Student> getAllStudents();

    /**
     * Returns an immutable Collection containing a Student object for each enabled
     * student in the database at the time {@link DataServices#updateDataCache()} 
     * was called.  If the database contained no enabled students, an empty
     * Collection is returned.  Note that the returned Collection is like a Set
     * in that all elements are unique.
     *
     * @return
     */
    public Collection<Student> getEnabledStudents();

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login or is not in the course's
     * student group; the user will then have the option of adding the student
     * anyway or cancelling the operation.  The students's first and last name
     * will be looked up.  If a student with the given login is already in the
     * database, this method has no effect.
     *
     * @param studentLogin
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     */
    public Student addStudent(String studentLogin, ValidityCheck checkValidity) throws ServicesException;

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login or is not in the course's
     * student group; the user will then have the option of adding the student
     * anyway or canceling the operation.  The students's first and last name
     * will be set to the firstName and lastName parameters, respectively.  If a
     * student with the given login is already in the database, this method has
     * no effect.
     *
     * @param studentLogin
     * @param firstName
     * @param lastName
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     * @return 
     */
    public Student addStudent(String studentLogin, String firstName, String lastName,
                           ValidityCheck checkValidity) throws ServicesException;

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
     * Adds all the students in the given Collection to the blacklist of the given
     * TA, if not already blacklisted. This ensures that this TA will not be distributed
     * any of these students to grade. If a student was already blacklisted, that student
     * will not be added again.
     *
     * @param students
     * @param ta
     * @throws ServicesException
     */
    public void blacklistStudents(Collection<Student> students, TA ta) throws ServicesException;
    
    /**
     * For each Student in the given Collection, removes the student from the given
     * TA's blacklist if the student was previously blacklisted.  This method has
     * no effect for students who were not previously on the TA's blacklist.
     *
     * @param students
     * @param ta
     * @throws ServicesException
     */
    public void unBlacklistStudents(Collection<Student> students, TA ta) throws ServicesException;
    
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
     * Adds the given newly created group to the database and returns the 
     * corresponding Group object.  A ServicesException will be thrown if a
     * group with that name already exists, if any member of the group is
     * already assigned to another group for the corresponding assignment, or 
     * if a database error occurred.
     * 
     * @param toAdd
     * @return 
     * @throws ServicesException 
     */
    public Group addGroup(NewGroup toAdd) throws ServicesException;
    
    /**
     * Adds the given newly created groups to the database.  If a group with the
     * same name as any of those to be added already exists in the database or if
     * a member of any group to be added is already assigned to another group for
     * the corresponding assignment, no groups will be added and a ServicesException
     * will be thrown. A ServicesException will also be thrown if a database 
     * error occurred.
     * 
     * @param toAdd
     * @throws ServicesException 
     */
    public Collection<Group> addGroups(Collection<NewGroup> toAdd) throws ServicesException;
    
    /**
     * Returns the Group for which the given Student is a member for the
     * given Assignment. A ServicesException will be thrown if no such Group
     * exists, or if a database error occurred.
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
     * for each student who does not already have a group of one.
     * 
     * @param asgn
     * @return
     * @throws ServicesException 
     */
    public Collection<Group> getGroups(Assignment asgn) throws ServicesException;
    
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
    public Map<TA, Collection<Group>> getDistribution(DistributablePart dp) throws ServicesException;
    
    /**
     * Assigns Groups for each TA to grade for each DistributablePart.  Any existing
     * distributions will be overwritten.
     *
     * @param distribution
     * @throws ServicesException 
     */
    public void setDistribution(Map<DistributablePart, Map<TA, Collection<Group>>> distribution) throws ServicesException;
    
    /**
     * Assigns the given Group to the given TA to grade for the given
     * DistributablePart.  This will enable the TA to open, run, grade, etc.
     * the Group's code for the given DistributablePart.  If the group is already
     * assigned to the TA for the DistributablePart, this method has no effect.
     * The Group will first be unassigned from any TA to which it has previously
     * been assigned for the DistributablePart.
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
    public void assignGroup(Group group, DistributablePart part, TA ta) throws ServicesException;

    /**
     * Unassigns Group group from the given TA for DistributablePart part.  If
     * the Group was not previously assigned to the TA to grade for the DistributablePart,
     * this method has no effect.
     *
     * @param group
     * @param part
     * @param ta
     * @throws ServicesException 
     */
    public void unassignGroup(Group group, DistributablePart part, TA ta) throws ServicesException;

    /**
     * Returns a Collection of Groups that the given TA is assigned to grade for
     * the given DistributablePart part.  Returns an empty Collection
     * if no groups are assigned to the TA for the given DistributablePart or if there is no
     * distribution for the DistributablePart in the database.
     *
     * @param part
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Collection<Group> getAssignedGroups(DistributablePart part, TA ta) throws ServicesException;

    /**
     * Returns a Collection of Groups that have been assigned to any TA
     * to grade for the given DistributablePart.  This can be used to find students
     * who have not been assigned to any TA to grade.  If no distribution exists
     * yet, an empty Collection will be returned.
     *
     * @param part
     * @return
     * @throws ServicesException 
     */
    public Collection<Group> getAssignedGroups(DistributablePart part) throws ServicesException;
    
    /**
     * Returns a Collection of DistributableParts for which the given TA has been
     * assigned at least one Group to grade.
     *
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Set<DistributablePart> getDPsWithAssignedGroups(TA ta) throws ServicesException;
    
    /**
     * Returns the TA who has been assigned to grade the given Group for the given
     * DistributablePart.  If no such TA exists, <code>null</code> will be returned.
     * 
     * @param part
     * @param group
     * @return
     * @throws ServicesException
     */
    public TA getGrader(DistributablePart part, Group group) throws ServicesException;
    
    /**
     * Grants an extension for the given Group on the given Handin.  The group's
     * handin will now be due at the date/time represented by the given Calendar.
     * Any previously granted extension will be overwritten. The given String can
     * be used to store a message explaining why the extension was granted;
     * however, a null value is permitted.
     *
     * @param group
     * @param handin
     * @param newDate
     * @param note
     */
    public void grantExtension(Group group, Calendar newDate, String note) throws ServicesException;

    /**
     * Removes a previously granted extension for the given group for the given Handin.
     * If the group did not previously have an extension, this method has no effect.
     *
     * @param group
     * @param handin
     */
    public void removeExtension(Group group) throws ServicesException;
    
    /**
     * Returns the Calendar representing the date when the Handin is due
     * for Group group if that group has an extension.  Returns
     * null if the group does not have an extension.
     *
     * @param group
     * @param handin
     * @return
     */
    public Calendar getExtension(Group group) throws ServicesException;
    
    /**
     * Returns a Map that maps a Group to a Calendar representing the extended
     * on-time date for that Group on the given Handin. A Group will only have an
     * entry in the map if an extension has been granted for that Group on the
     * given Handin.
     *
     * @param handin
     * @return
     * @throws ServicesException 
     */
    public Map<Group, Calendar> getExtensions(Handin handin) throws ServicesException;
    
    /**
     * Returns a string containing a message to indicate why the Group group
     * has been granted an extension on Handin handin.  Returns null
     * if the group does not have an extension, or if no note was stored to
     * explain this extension.
     *
     * @param group
     * @param handin
     * @return
     */
    public String getExtensionNote(Group group) throws ServicesException;

    /**
     * Grants an exemption for the Group group for the Part
     * part.  The group's emailed grade report will indicate that the student
     * has been exempted for this Part instead of showing a 0.  Additionally,
     * this Part will not be taken into account in the calculation of final grades
     * (if support for final grade calculation is added).  The given note String can be
     * used to store a message explaining why the extension was granted; however,
     * <code>null</code> is also permitted.  Any existing extension will be overwritten.
     *
     * @param group
     * @param part
     * @param note
     */
    public void grantExemption(Group group, Part part, String note) throws ServicesException;

    /**
     * Removes a previously granted exemption for the given group for the given Part.
     * If the group did not previously have an exemption, this method has no effect.
     *
     * @param group
     * @param part
     */
    public void removeExemption(Group group, Part part) throws ServicesException;
    
    /**
     * Returns the Groups that have an exemption for the given Part.
     * 
     * @param part
     * @return
     * @throws ServicesException 
     */
    public Set<Group> getExemptions(Part part) throws ServicesException;
    
    /**
     * Returns a string containing a message to indicate why the Group group
     * has been granted an exemption on Part part.
     * Returns null if the group does not have an exemption, or if no note was
     * stored to explain this exemption.
     *
     * @param group
     * @param part
     * @return
     */
    public String getExemptionNote(Group group, Part part) throws ServicesException;
    
    /**
     * Assigns a grade of score to Group group on Part part.
     *
     * @param group
     * @param part
     * @param score
     */
    public void enterGrade(Group group, Part part, double score) throws ServicesException;
    
     /**
     * Returns the score of the given Group on the given Part. If no such score is
     * stored in the database, null will be returned.
     *
     * @param group
     * @param part
     * @return
     */
    public Double getScore(Group group, Part part) throws ServicesException;

    /**
     * Returns the score of the given Group for the corresponding Assignment.
     * The returned score is simply the sum of the scores for the Group on each
     * Part of the Assignment; it does not include any handin status points.  If
     * no part  scores for the given Assignment are stored in the database for
     * the given Group, null will be returned.
     *
     * @param group
     * @return
     */
    public Double getScore(Group group) throws ServicesException;
    
    /**
     * Returns a map of all scores for the specified Groups for the
     * specified Part with Groups as the keys and their scores as the values.
     * Any Groups with no score stored in the database will not have an entry
     * in the returned Map.
     *
     * @param part
     * @param groups
     * @return
     * @throws ServicesException 
     */
    public Map<Group, Double> getScores(Part part, Collection<Group> groups) throws ServicesException;

    /**
     * Returns a map of all scores for the specified Groups for the
     * specified Assignment with Groups as the keys and scores as the values.
     * For each group, the score in the Map is simply the sum of the scores for the 
     * group on each Part of the Assignment; it does not include any handin
     * status points.
     * 
     * @param asgn
     * @param groups
     * @return
     * @throws ServicesException 
     */
    public Map<Group, Double> getScores(Assignment asgn, Collection<Group> groups) throws ServicesException;
    
    /**
     * Stores the given HandinStatus in the database for the given Handin and Group.
     * Any existing handin status for the Handin and Group will be overwritten.
     *
     * @param handin
     * @param group
     * @param status
     */
    public void setHandinStatus(Group group, HandinStatus status) throws ServicesException;

    /**
     * For each entry in the given Map, stores the corresponding HandinStatus in
     * the database for the corresponding Group and given Handin.  Any existing
     * handin statuses for Groups with entries in the Map will be overwritten.
     *
     * @param handin
     * @param statuses
     */
    public void setHandinStatuses(Map<Group, HandinStatus> statuses) throws ServicesException;

    /**
     * Returns the HandinStatus for the given Handin and Group. If no such record
     * exists in the database, null will be returned.
     *
     * @param handin
     * @param group
     * @return
     */
    public HandinStatus getHandinStatus(Group group) throws ServicesException;

    /**
     * Returns whether or not at least one group for the given Handin has had a
     * handin status set.
     * 
     * @param handin
     * @return
     * @throws SQLException
     * @throws CakeHatDBIOException
     */
    public boolean areHandinStatusesSet(Handin handin) throws ServicesException;

    /**
     * Removes all data from database tables and rebuilds the tables. If no DB
     * file exists or is empty then it will be set to the initial configuration.
     */
    public void resetDatabase() throws ServicesException;
    
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
     * Loads Student objects into memory for all students in the database.
     *
     * @throws ServicesException
     */
    public void updateDataCache() throws ServicesException;

}
