package cakehat.database;

import cakehat.database.DbPropertyValue.DbPropertyKey;
import com.google.common.collect.SetMultimap;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import support.utils.Pair;

/**
 *
 * @author jak2
 * @author jeldridg
 */
public interface Database
{
   
    public <T> DbPropertyValue<T> getPropertyValue(DbPropertyKey<T> key) throws SQLException;
    
    public <T> void putPropertyValue(DbPropertyKey<T> key, DbPropertyValue<T> value) throws SQLException;
    
    /**
     * Returns an immutable set of DbNotifyAddress objects representing rows of the {@code notifyaddresses} table in the
     * database.
     * 
     * @return
     * @throws SQLException 
     */
    public Set<DbNotifyAddress> getNotifyAddresses() throws SQLException;
    
    /**
     * For each DbNotifyAddress in the given Set, adds the address to the database if it does not yet exist, or updates
     * the corresponding entry in the database if it already exists.  Whether or not {@link DbNotifyAddress#getId()}
     * returns {@code null} is used to determine whether or not the address yet exists in the database.  All
     * DbNotifyAddress objects that resulted in the addition of an entry in the database will be updated such that its 
     * ID field matches the auto-generated ID of the corresponding row in the database.
     * 
     * Note that it is permitted to have multiple notify addresses entries in the database that have the same actual
     * address.  If the set contains a DbNotifyAddress with a non-null ID that does not correspond to an entry in the
     * database, however, a SQLException will be thrown.
     * 
     * @param notifyAddresses
     * @throws SQLException 
     */
    public void putNotifyAddresses(Set<DbNotifyAddress> notifyAddresses) throws SQLException;
    
    //need to set ID to null for each DbNotifyAddress
    public void removeNotifyAddresses(Set<DbNotifyAddress> notifyAddresses) throws SQLException;
    
    public Set<DbTA> getTAs() throws SQLException;
    
    public void putTAs(Set<DbTA> tas) throws SQLException;
    
    public Set<DbStudent> getStudents() throws SQLException;
    
    public void putStudents(Set<DbStudent> students) throws SQLException;
    
    //needs to have all nested constituents
    public Set<DbAssignment> getAssignments() throws SQLException;
    
    //only edits assignment table
    public void putAssignments(Set<DbAssignment> assignments) throws SQLException;
    
    //cascades
    public void removeAssignments(Set<DbAssignment> assignments) throws SQLException;
    
    public void putGradableEvents(Set<DbGradableEvent> gradableEvents) throws SQLException;
    
    //cascades
    public void removeGradableEvents(Set<DbGradableEvent> gradableEvents) throws SQLException;
    
    //only edits parts table
    public void putParts(Set<DbPart> parts) throws SQLException;
    
    public void removeParts(Set<DbPart> parts) throws SQLException;
    
    public void putPartActions(Set<DbPartAction> partActions) throws SQLException;
    
    //cascades
    public void removePartActions(Set<DbPartAction> partActions) throws SQLException;
    
    public void putPartActionProperties(Set<DbActionProperty> actionProperties) throws SQLException;
    
    public void removePartActionProperties(Set<DbActionProperty> actionProperties) throws SQLException;
    
    public void putInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException;
    
    public void removeInclusionFilters(Set<DbInclusionFilter> inclusionFilters) throws SQLException;
    
    /**
     * For each student ID in the given Set, adds the corresponding student
     * to the blacklist of the TA with the given ID.  This ensures that the TA
     * will not be distributed a group to grade that includes any of these students.
     * If a student was already blacklisted by this TA, its entry in the blacklist
     * table will not be duplicated.  If any of the IDs in the Set do not
     * correspond to student records in the database, a SQLException will be thrown
     * and no students will have been added to the TA's blacklist.
     *
     * @param studentIDs
     * @param taID
     */
    public void blacklistStudents(Set<Integer> studentIDs, int taID) 
                                                            throws SQLException;
    /**
     * For each student ID in the given Set, removes the corresponding
     * student from the blacklist of the TA with the given ID.  This has no
     * effect for any student IDs that correspond to students not currently
     * blacklisted by the TA or that do not correspond to student records in
     * the database.
     *
     * @param studentIDs
     * @param taID
     */
    public void unBlacklistStudents(Set<Integer> studentIDs, int taID) 
                                                            throws SQLException;

    /**
     * Returns an ImmutableSet containing the IDs of all students who are on 
     * some TA's blacklist.  If no students have been blacklisted, an empty Set 
     * will be returned.
     *
     * @return
     */
    public Set<Integer> getBlacklistedStudents() throws SQLException;

    /**
     * Returns an ImmutableSet containing the IDs of all students who are on 
     * the blacklist of the TA with the given ID. If the TA has not blacklisted 
     * any students, an empty Set will be returned.
     * 
     * @param taID
     * @return
     */
    public Set<Integer> getBlacklist(int taID) throws SQLException;
    
    /**
     * Returns an ImmutableSet containing a GroupRecord object for each group in
     * the database. If the database contains no groups, an empty Set is returned.
     *
     * @return
     */
    public Set<DbGroup> getGroups() throws SQLException;
    
    /**
     * Updates the groups tables in the database to match the given Set of DbGroup objects.  Any DbGroup that has a null
     * ID will be added, and any DbGroup that has a non-null ID will be updated, which includes updating its list of
     * students.  Any removals of students from groups will be done before any additions of students to groups.  A
     * SQLException will be thrown if a student to be added to a group is still a member of some group in the database
     * for the same assignment after all removals have been performed.  A SQLException will also be thrown if any group
     * to be added has the  same name as a group that already exists for that assignment.  This means that a single
     * putGroups(...) call cannot be used to swap the names of groups.  In either case, no changes will have been made
     * to the database.
     * 
     * @param groups
     */
    public void putGroups(Set<DbGroup> groups) throws SQLException;

    /**
     * Returns an ImmutableSet containing the DbGroup object for each group that has been 
     * created for the assignment with the given ID.  If no groups have yet been
     * created for the assignment or there is no assignment with the given 
     * asgnID, an empty Set will be returned.
     * 
     * @param asgnID
     * @return
     */
    public Set<DbGroup> getGroups(int asgnID) throws SQLException;
    
    /**
     * Removes the given DbGroups from the database.  If any of the given DbGroups does not correspond to a group in the
     * database, it will be ignored.
     * 
     * @param groups
     * @throws SQLException 
     */
    public void removeGroups(Set<DbGroup> groups) throws SQLException;
    
    /**
     * Indicates whether all Parts corresponding to the part IDs in the given
     * Set have no distribution.  This is intended to be used to determine
     * if an distribution has been set for any Parts of an Assignment; however,
     * this does not need to be the case.
     * 
     * Returns true if the distribution is empty for all of the parts, and false
     * if it is non-empty for at least one of them.
     *
     * @param partIDs
     * @return
     */
    public boolean isDistEmpty(Set<Integer> partIDs) throws SQLException;
    
    /**
     * Returns the distribution for the Part with the given ID.  The SetMultimap 
     * returned maps a TA's ID to a Set of group IDs representing the
     * groups that TA has been assigned to grade for the Part. The SetMultiap returned
     * contains entries only for those TAs who have groups assigned to them.
     * If no distribution has yet been set, an empty SetMultimap is returned.
     * 
     * @param partID
     * @return
     */
    public SetMultimap<Integer, Integer> getDistribution(int partID) throws SQLException;
    
    /**
     * Stores a distribution in the database.  The given Map maps the ID of a 
     * Part to a Map that maps a TA's ID to a Set of group IDs representing
     * groups that TA has been assigned to grade for that Part.
     * Any existing distribution for the Part will be overwritten. The
     * distribution will either be set in its entirety or not at all; if the
     * new distribution is not successfully set in its entirety, the database
     * table will be in whatever state it was in before this method was called.
     *
     * @param distribution
     */
    public void setDistribution(Map<Integer, Map<Integer, Set<Integer>>> distribution) 
                                                        throws SQLException;

    /**
     * Assigns the group corresponding to the given group ID to the TA with the
     * given ID to grade for the Part corresponding to the given partID.
     * If the group is already assigned to the TA for the Part, this method 
     * has no effect.  The group will be unassigned from any TA to which it was
     * previously assigned for this Part. If the groupID is invalid, a 
     * SQLException will be thrown.
     *
     * NOTE: This method should not be used to create an initial distribution
     *       for a project; it should be used only to reassign grading.
     *       To create an initial distribution, use setDistribution(...), above.
     *
     * @param groupID
     * @param partID
     * @param taID
     */
    public void assignGroup(int groupID, int partID, int taID) throws SQLException;

    /**
     * Unassigns the group with the given ID from the TA with the given ID
     * on the Part with the given part ID.
     *
     * @param groupID
     * @param partID
     * @param taID
     */
    public void unassignGroup(int groupID, int partID) throws SQLException;

    /**
     * Returns an ImmutableSet of IDs for the groups that the given TA has been 
     * assigned to grade for the Part with the given ID.  Returns an empty Set 
     * if no groups are assigned to the TA for the Part or if there is no 
     * distribution for the Part in the database. 
     *
     * @param partID
     * @param taID
     * @return
     */
    public Set<Integer> getAssignedGroups(int partID, int taID) 
                                                        throws SQLException;

    /**
     * Returns an ImmutableSet of IDs for groups that have been assigned to any 
     * TA to grade for the given Part.  This can be used to find students who 
     * have not been assigned to any TA to grade. If no distribution exists yet,
     * an empty Set will be returned.
     *
     * @param partID
     * @return
     */
    public Set<Integer> getAssignedGroups(int partID) throws SQLException;

    /**
     * Returns an ImmutableSet of part IDs representing Parts for which at least
     * one group has been assigned to the TA with the given ID for grading. If
     * the TA has not been assigned any groups to grade, an empty Set will be
     * returned.
     *
     * @param taID
     * @return
     */
    public Set<Integer> getPartsWithAssignedGroups(int taID) throws SQLException;
    
    /**
     * Returns the ID of the TA who has been assigned to grade the group with
     * the given ID for the Part with the given ID.  If no such TA exists, or 
     * the groupID is invalid, <code>null</code> will be returned.
     * 
     * @param partID
     * @param groupID
     * @return
     * @throws SQLException
     */
    public Integer getGrader(Integer partID, int groupID) throws SQLException;

    /**
     * Sets the same extension for the groups specified by {@code groupIds} for gradable event {@code geId}. The groups
     * should to belong to the assignment that the gradable event belongs to; this is not enforced by the database.
     * 
     * @param geId
     * @param ontime
     * @param shiftDates
     * @param note
     * @param dateRecorded
     * @param taId
     * @param groupIds
     * @throws SQLException if thrown no changes to extensions will have occurred
     */
    public void setExtensions(int geId, String ontime, boolean shiftDates, String note, String dateRecorded,
            int taId, Set<Integer> groupIds) throws SQLException;
    
    /**
     * Deletes extensions for the groups specified by {@code groupIds} for gradable event {@code geId}. The groups
     * should belong to the assignment that the gradable event belongs to; this is not enforced by the database.
     * 
     * @param geId
     * @param groupIds
     * @throws SQLException if thrown no changes to extensions will have occurred
     */
    public void deleteExtensions(int geId, Set<Integer> groupIds) throws SQLException;
    
    /**
     * Retrieves a mapping of group id to extension record for the groups specified by {@code groupIds} for gradable
     * event {@code geId}. The groups should to belong to the assignment that the gradable event belongs to; this is
     * not enforced by the database. If no extension exists for a group it will not be in the mapping.
     * 
     * @param geId
     * @param groupIds
     * @return
     * @throws SQLException 
     */
    public Map<Integer, ExtensionRecord> getExtensions(int geId, Set<Integer> groupIds) throws SQLException;
    
    /**
     * Assigns a grade of score to the group with the given group ID on the part
     * with the given part ID. If the group ID is invalid then a SQLException is thrown.
     *
     * @param groupID
     * @param partID
     * @param taID
     * @param dateRecorded
     * @param earned
     * @param submitted
     * @param dateRecorded
     * 
     */
    public void setEarned(int groupID, int partID, int taID, Double earned,
                    boolean submitted, String dateRecorded) throws SQLException;
    
    public void setEarned(int partId, int taId, String dateRecorded, Map<Integer, Pair<Double, Boolean>> earned)
            throws SQLException;
    
    public void setEarnedSubmitted(int partId, int taId, String dateRecorded, Map<Integer, Boolean> submitted)
            throws SQLException;

    /**
     * Returns a GradeRecord object containing the grade information of the 
     * group with the given group ID on the part with the given part ID, or 
     * null if no such grade information is stored in the database.
     *
     * @param groupID
     * @param partID
     * @return
     */
    public GradeRecord getEarned(int groupID, int partID) throws SQLException;

    /**
     * Returns a Map that maps a group ID to the GradeRecord object representing 
     * the grade information for that group on the part corresponding to the 
     * given partID for each group ID in the given Set. Any Groups with no score
     * stored in the database will not have an entry in the returned Map. 
     * Invalid groups will also not have an entry in the returned Map.
     *
     * @param partID
     * @param groupIDs
     * @return
     */
    public Map<Integer, GradeRecord> getEarned(int partID, Set<Integer> groupIDs) 
                                                        throws SQLException;

    public Map<Integer, GradableEventOccurrenceRecord> getGradableEventOccurrences(int geId, Set<Integer> groupIds)
            throws SQLException;

    public void setGradableEventOccurrences(int geid, Map<Integer, String> groupsToTime, int tid, String dateRecorded)
            throws SQLException;
    
    
    public void deleteGradableEventOccurrences(int geId, Set<Integer> groupIds) throws SQLException;
    
    /**
     * Null will be returned if no such geid exists in database
     * @param geid
     * @return 
     */
    public DbGradableEvent getDbGradableEvent(int geid) throws SQLException;


    public void resetDatabase() throws SQLException;
}