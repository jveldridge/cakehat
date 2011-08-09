package cakehat.database;

import cakehat.config.Assignment;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface to be implemented by classes providing database interaction; mandates
 * methods such classes must have.
 * 
 * These methods should be called <strong>only</strong> by implementations of
 * the {@link DataServices} interface.
 *
 * @author jeldridg
 */
public interface Database {

    /**
     * Adds a student record with the given login and names to the database if
     * no record with the given login already exists.  The newly added student
     * is enabled by default.  The return value is the auto-generated ID
     * corresponding to the newly added row in the database table. This will be
     * 0 if a student with that login is already in the database.
     *
     * @param studentLogin
     * @param studentFirstName
     * @param studentLastName
     */
    public int addStudent(String studentLogin, String studentFirstName, String studentLastName) throws SQLException;

    /**
     * Sets the "enabled" field of the database student table to 0 for the student
     * record corresponding to the given student ID.  This denotes that the
     * student has been disabled and will not be distributed for grading, sent
     * grade reports, or counted for statistics.
     * 
     * If the given student ID does not correspond to a student record in the
     * database, this method has no effect.
     *
     * @param studentID
     */
    public void disableStudent(int studentID) throws SQLException;

    /**
     * Sets the "enabled" field of the database student table to 1 for the student
     * record corresponding to the given student ID.  This denotes that the
     * student is enabled and can be distributed for grading, sent grade reports,
     * and counted for statistics.
     * 
     * If the given student ID does not correspond to a student record in the
     * database, this method has no effect.
     *
     * @param studentID
     */
    public void enableStudent(int studentID) throws SQLException;

    /**
     * Returns an immutable Collection containing a StudentRecord object for each student
     * in the database.  If the database contains no students, an empty Collection
     * is returned.
     *
     * @return
     */
    public Collection<StudentRecord> getAllStudents() throws SQLException;

    /**
     * For each student ID in the given Collection, adds the corresponding student
     * to the blacklist of the TA with the given login.  This ensures that the TA
     * will not be distributed a group to grade that includes any of these students.
     * If a student was already blacklisted by this TA, its entry in the blacklist
     * table will not be duplicated.  If any of the IDs in the Collection do not
     * correspond to student records in the database, a SQLException will be thrown
     * and no students will have been added to the TA's blacklist.
     *
     * @param studentIDs
     * @param taLogin
     */
    public void blacklistStudents(Collection<Integer> studentIDs, String taLogin) throws SQLException;

    /**
     * For each student ID in the given Collection, removes the corresponding
     * student from the blacklist of the TA with the given login.  This has no
     * effect for any student IDs that correspond to students not currently
     * blacklisted by the TA or that do not correspond to student records in
     * the database.
     *
     * @param studentIDs
     * @param taLogin
     */
    public void unBlacklistStudents(Collection<Integer> studentIDs, String taLogin) throws SQLException;

    /**
     * Returns the IDs of all students who are on some TA's blacklist.  If no
     * students have been blacklisted, an empty Collection will be returned.
     *
     * @return
     */
    public Collection<Integer> getBlacklistedStudents() throws SQLException;

    /**
     * Returns the IDs of all students who are on the blacklist of the TA with
     * the given login.  If the TA has not blacklisted any students, an empty
     * Collection will be returned.
     * 
     * @param taLogin
     * @return
     */
    public Collection<Integer> getBlacklist(String taLogin) throws SQLException;
    
    /**
     * Returns an immutable Collection containing a GroupRecord object for each group
     * in the database.  If the database contains no groups, an empty Collection
     * is returned.
     *
     * @return
     */
    public Collection<GroupRecord> getAllGroups() throws SQLException;
    
    /** 
     * Adds a group to the database.  A SQLException will be thrown if the group
     * to be added has the same name as a group that already exists for that
     * assignment.  A CakehatDBIOException will be thrown if any member of the
     * group is already in a group for that assignment.
     * 
     * @param group
     */
    public GroupRecord addGroup(NewGroup group) throws SQLException, CakeHatDBIOException;
    
    /**
     * Adds a set of groups to the database.  A SQLException will be thrown if any
     * group to be added has the same name as a group that already exists for that
     * assignment.  A CakehatDBIOException will be thrown if any member of any
     * group to be added is already in a group for that assignment.  In either case,
     * no groups will have been added to the database.
     * 
     * @param groups
     */
    public Collection<GroupRecord> addGroups(Collection<NewGroup> groups) throws SQLException, CakeHatDBIOException;

    /**
     * Returns the ID of the group for which the student with the given ID is a
     * member for the assignment with the given ID. If no such group exists then
     * <code>0</code> is returned.
     *
     * @param asgnID
     * @param studentID
     * @return
     */
    public int getGroup(String asgnID, int studentID) throws SQLException;

    /**
     * Returns the IDs of all groups that have been created for the assignment
     * with the given ID.  If no groups have yet been created for the assignment,
     * an empty Collection will be returned.
     * 
     * @param asgnID
     * @return
     */
    public Collection<Integer> getGroups(String asgnID) throws SQLException;

    /**
     * Removes from the database the group with the given ID.  If no such group
     * exists, this method has no effect.
     * 
     * @param groupID
     */
    public void removeGroup(int groupID) throws SQLException;

    /**
     * Removes from the database all groups that have been created for the assignment
     * with the given ID.  If no groups have yet been created for the assignment,
     * this method has no effect.
     * 
     * @param asgnID
     */
    public void removeGroups(String asgnID) throws SQLException;
    
    /**
     * Indicates whether all Parts corresponding to the part IDs in the given
     * Iterable have no distribution.  This is intended to be used to determine
     * if an distribution has been set for any Parts of an Assignment; however,
     * this does not need to be the case.
     * 
     * Returns true if the distribution is empty for all of the parts, and false
     * if it is non-empty for at least one of them.
     *
     * @param partIDs
     * @return
     */
    public boolean isDistEmpty(Iterable<String> partIDs) throws SQLException;
    
    /**
     * Returns the distribution for the DistributablePart with the given ID.  The
     * Map returned maps a TA's login to a Collection of group IDs representing the
     * groups that TA has been assigned to grade for the DistributablePart. The 
     * Map returned contains entries only for those TAs who have groups assigned to
     * them.  If no distribution has yet been set, an empty Map is returned.
     * 
     * @param dpID
     * @return
     */
    public Map<String, Collection<Integer>> getDistribution(String dpID) throws SQLException;
    
    /**
     * Stores a distribution in the database.  The given Map maps the ID of a 
     * DistributablePart to a Map that maps a TA's login to a Collection of group
     * IDs representing groups that TA has been assigned to grade for that
     * DistributablePart. Any existing distributions will be overwritten.  The
     * distribution will either be set in its entirety or not at all; if the new
     * distribution is not successfully set in its entirety, the database table
     * will be in whatever state it was in before this method was called.
     *
     * @param distribution
     */
    public void setDistribution(Map<String, Map<String, Collection<Integer>>> distribution) throws SQLException;

    /**
     * Assigns the group corresponding to the given group ID to the TA with the
     * given login to grade for the DistributablePart corresponding to the given
     * part ID.  If the group is already assigned to the TA for the DistributablePart,
     * this method has no effect.  The group will be unassigned from any TA to which
     * it was previously assigned for this DistributablePart.
     *
     * NOTE: This method should not be used to create an initial distribution
     *       for a project; it should be used only to reassign grading.
     *       To create an initial distribution, use setAsgnDist(...), below.
     *
     * @param groupID
     * @param partID
     * @param taLogin
     */
    public void assignGroup(int groupID, String partID, String taLogin) throws SQLException;

    /**
     * Unassigns the group with the given ID from the TA with the given login
     * on the DistributablePart with the given part ID.
     *
     * @param groupID
     * @param partID
     * @param taLogin
     */
    public void unassignGroup(int groupID, String partID, String taLogin) throws SQLException;

    /**
     * Returns a Collection of IDs for the groups that the given TA has been
     * assigned to grade for the DistributablePart with the given ID.  Returns
     * an empty Collection if no groups are assigned to the TA for the
     * DistributablePart or if there is no distribution for the DistributablePart
     * in the database.
     *
     * @param partID
     * @param taLogin
     * @return
     */
    public Collection<Integer> getAssignedGroups(String partID, String taLogin) throws SQLException;

    /**
     * Returns a Collection of IDs for groups that have been assigned to any TA
     * to grade for the given DistributablePart.  This can be used to find students
     * who have not been assigned to any TA to grade.  If no distribution exists
     * yet, an empty Collection will be returned.
     *
     * @param partID
     * @return
     */
    public Collection<Integer> getAssignedGroups(String partID) throws SQLException;

    /**
     * Returns a Collection of part IDs representing DistributableParts for which
     * at least one group has been assigned to the TA with the given login for
     * grading.  If the TA has not been assigned any groups to grade, an empty Set
     * will be returned.
     *
     * @param taLogin
     * @return
     */
    public Set<String> getDPsWithAssignedGroups(String taLogin) throws SQLException;
    
    /**
     * Returns the login of the TA who has been assigned to grade the group with
     * the given ID for the DistributablePart with the given ID.  If no such TA
     * exists, <code>null</code> will be returned.
     * 
     * @param partID
     * @param groupID
     * @return
     * @throws SQLException
     * @throws CakeHatDBIOException 
     */
    public String getGrader(String partID, int groupID) throws SQLException;

    /**
     * Grants an extension for the group with the given ID on the handin for the
     * assignment with the given ID.  The group's handin will now be due at the
     * date/time represented by the given calendar.  The given note String can be
     * used to store a message explaining why the extension was granted; however,
     * <code>null</code> is also permitted.  Any existing extension will be overwritten.
   
     * @param groupID
     * @param asgnID
     * @param newDate
     * @param note
     */
    public void grantExtension(int groupID, String asgnID, Calendar newDate, String note) throws SQLException;

    /**
     * Removes a previously granted extension for the group with the given ID.
     * If the group did not previously have an extension, this method has no effect.
     *
     * @param groupID
     */
    public void removeExtension(int groupID) throws SQLException;
    
    /**
     * Returns a Calendar representing the date when the handin of the group with 
     * the given group ID is due for the corresponding assignment, if an extension
     * has been granted for that group.  Returns null if the group does not have
     * an extension.
     *
     * @param groupID
     * @return
     */
    public Calendar getExtension(int groupID) throws SQLException;
    
    /**
     * Returns a Map that maps a group ID to a Calendar representing a granted
     * extension for that group on the handin for the assignment with the given
     * ID.  Only groups for which an extension has been granted will have an
     * entry in the returned Map.  If no extensions have been granted for the
     * assignment, an empty Map will be returned.
     *
     * @param asgnID
     * @return
     */
    public Map<Integer, Calendar> getExtensions(String asgnID) throws SQLException;

    /**
     * Returns a string containing a message to indicate why the group with the
     * given ID has been granted an extension on the handin for the corresponding
     * assignment.  Returns null if the group does not have an extension, or if
     * no note explaining the extension was stored in the database.
     *
     * @param groupID
     * @return
     */
    public String getExtensionNote(int groupID) throws SQLException;

    /**
     * Grants an exemption for the group with the given group ID for the Part
     * with the given part ID.  The given note String can be used to store a
     * message explaining why the exemption was granted; however, <code>null</code>
     * is also permitted.  If the group had an exemption previously, any exiting
     * exemption note will be overwritten.
     *
     * @param groupID
     * @param partID
     * @param note
     */
    public void grantExemption(int groupID, String partID, String note) throws SQLException;

    /**
     * Removes a previously granted exemption for the group with the given group ID
     * on the part with the given part ID. If the group did not previously have an
     * exemption for the part, this method has no effect.
     *
     * @param groupID
     */
    public void removeExemption(int groupID, String partID) throws SQLException;
    
    /**
     * Returns the group IDs of all groups that have an exemption for the part
     * with the given ID.
     * 
     * @param partID
     * @return
     * @throws SQLException 
     */
    public Set<Integer> getExemptions(String partID) throws SQLException;

    /**
     * Returns a String containing a message to indicate why the group with the
     * given group ID has been granted an exemption on the part with the given 
     * part ID.  Returns null if the group does not have an exemption, or if
     * no note explaining the exemption was stored in the database.
     *
     * @param groupID
     * @param partID
     * @return
     */
    public String getExemptionNote(int groupID, String partID) throws SQLException;

    /**
     * Assigns a grade of score to the group with the given group ID on the part
     * with the given part ID.
     *
     * @param groupID
     * @param partID
     * @param score
     */
    public void enterGrade(int groupID, String partID, double score) throws SQLException;

    /**
     * Returns the score of the group with the given group ID on the part
     * with the given part ID, or null if no such score is stored in the database.
     *
     * @param groupID
     * @param partID
     * @return
     */
    public Double getPartScore(int groupID, String partID) throws SQLException;

    /**
     * Returns the sum of scores for the group corresponding to the given groupIDs
     * on the parts corresponding to the given part IDs.  The given Iterable of
     * part IDs is intended to correspond to the part IDs that make up an Assignment;
     * however, this is not enforced.  Any parts for which the group does not have
     * a score will contribute nothing to the returned sum.  (This will be the case
     * for all parts that do not belong to the assignment corresponding to the group).
     * 
     * Note that the returned score does not take into account any handin status points.
     *
     * @param groupID
     * @param partIDs
     * @return
     */
    public Double getScore(int groupID, Iterable<String> partIDs) throws SQLException;

    /**
     * Returns a Map that maps a group ID to the score for that group on the
     * part corresponding to the given part ID for each group ID in the given
     * Iterable. Any Groups with no score stored in the database will not have
     * an entry in the returned Map.
     *
     * @param partID
     * @param groupIDs
     * @return
     */
    public Map<Integer, Double> getPartScores(String partID, Iterable<Integer> groupIDs) throws SQLException;

    /**
     * Returns a Map that maps a groupID to the the sum of scores for the group
     * on the parts corresponding to the given part IDs, for each groupID in the 
     * given Iterable of groupIDs.  The given Iterable of part IDs is intended
     * to correspond to the part IDs that make up an Assignment; however, this
     * is not enforced.  Any parts for which the group does not have a score will
     * contribute nothing to the returned sum.  (This will be the case for all parts
     * that do not belong to the assignment corresponding to the group).
     * 
     * Any Groups with no score stored in the database for any of the Parts of
     * the Assignment will not have an entry in the returned Map.  Note also 
     * that the scores in the Map do not take into account any handin status points.
     *
     * @param partIDs
     * @param groupIDs
     * @return
     */
    public Map<Integer, Double> getScores(Iterable<String> partIDs, Iterable<Integer> groupIDs) throws SQLException;

    /**
     * Stores the given HandinStatus in the database for the group with the
     * given group ID.  Any existing handin status for the group will be overwritten.
     *
     * @param groupID
     * @param status
     */
    public void setHandinStatus(int groupID, HandinStatus status) throws SQLException;

    /**
     * For each group ID key in the given Map, stores the corresponding HandinStatus
     * in the database for that group.  Any existing handin statuses for the groups
     * will be overwritten.
     *
     * @param statuses
     */
    public void setHandinStatuses(Map<Integer, HandinStatus> statuses) throws SQLException;

    /**
     * Returns the HandinStatus for the group with the given group ID.  If no 
     * record of the group's handin status exists in the database, null will be
     * returned.
     *
     * @param groupID
     * @return
     */
    public HandinStatus getHandinStatus(int groupID) throws SQLException;

    /**
     * Returns whether or not at least one group for the handin corresponding to
     * the Assignment with the given ID has had a handin status set.  Returns
     * true if so, and false if not.
     * 
     * @param asgnID
     * @return
     */
    public boolean areHandinStatusesSet(String asgnID) throws SQLException, CakeHatDBIOException;

    /**
     * Removes all data from database tables and rebuilds the tables. If no DB
     * file exists or is empty then it will be set to the initial configuration.
     */
    public void resetDatabase() throws SQLException;

}
