package cakehat.database;

import cakehat.assignment.Assignment;
import cakehat.assignment.GradableEvent;
import cakehat.assignment.Part;
import cakehat.services.ServicesException;
import com.google.common.collect.SetMultimap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;

/**
 *
 * @author hdrosen
 */
public interface DataServices {
        
    /**
     * Returns an immutable Set snapshot containing a {@link Student} object for each student in the database at the
     * time {@link DataServices#updateDataCache()} was called. If the database contained no students, an empty Set is
     * returned.
     * 
     * @return
     */
    public Set<Student> getStudents() throws ServicesException;
    
    /**
     * Returns an immutable Set view containing a {@link Student} object for each enabled student in the database at the
     * time {@link DataServices#updateDataCache()} was called.  If the database contained no enabled students, an
     * empty Set is returned.
     *
     * @return
     */
    public Set<Student> getEnabledStudents() throws ServicesException;    
    
    /**
     * Returns the set of students who have collaboration contracts.
     * 
     * @return
     * @throws ServicesException 
     */
    public Set<Student> getStudentsWithCollaborationContracts() throws ServicesException;
    
    /**
     * Sets for each student in {@code studentsToUpdate} if they have a collaboration contract (mapped to {@code true}
     * or not (mapped to {@code false}).
     * 
     * If any value or key in the map is {@code null}, a {@code NullPointerException} will be thrown.
     * 
     * @param studentsToUpdate
     * @throws ServicesException 
     */
    public void setStudentsHasCollaborationContract(Map<Student, Boolean> studentsToUpdate) throws ServicesException;

    /**
     * For each login in the given Set of student logins, adds the corresponding student to the database.  Each
     * student's name will be determined by (@link UserUtilities#getUserName(java.lang.String)}, and each student's
     * email address will be automatically generated as login@cs.brown.edu.  Each student will be enabled and without
     * collaboration policy.  All of these properties can subsequently be changed in the config manager.
     * 
     * If any login in the set does not correspond to a student in the student group, a ServicesException will be thrown
     * and no students will have been added.
     */
    public void addStudentsByLogin(Set<String> studentLogins) throws ServicesException;
    
    public void addStudents(Set<DbStudent> students) throws ServicesException;

    /**
     * Sets the enabled status of each student in the key set of the given map to the corresponding boolean value.
     * 
     * If any of the values in the map are null, a NullPointerException will be thrown and the enabled status will not
     * have been updated for any students.
     * 
     * @param studentsToUpdate 
     */
    public void setStudentsAreEnabled(Map<Student, Boolean> studentsToUpdate) throws ServicesException;
    
    /**
     * Adds all the students in the given Set to the blacklist of the given TA, if not already blacklisted. This ensures
     * that this TA will not be distributed any of these students to grade. If a student was already blacklisted, that 
     * student will not be added again.
     *
     * @param students
     * @param ta
     * @throws ServicesException
     */
    public void blacklistStudents(Set<Student> students, TA ta) throws ServicesException;
    
    /**
     * For each Student in the given Set, removes the student from the given TA's blacklist if the student was
     * previously blacklisted.  This method has no effect for students who were not previously on the TA's blacklist.
     *
     * @param students
     * @param ta
     * @throws ServicesException
     */
    public void unBlacklistStudents(Set<Student> students, TA ta) throws ServicesException;
    
    /**
     * Returns a Set containing the Student object representing each student who has been blacklisted by some TA. If no
     * students have been blacklisted, an empty Set will be returned.
     * 
     * @return
     * @throws ServicesException 
     */
    public Set<Student> getBlacklistedStudents() throws ServicesException;
    
    /**
     * Returns a Set containing the Student object representing each student who has been blacklisted by the given TA.
     * If the TA has not blacklisted any students, an empty Set will be returned.
     * 
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Set<Student> getBlacklist(TA ta) throws ServicesException;
    
    /**
     * Indicates whether the gradable event has a distribution. Returns true if the gradable event currently has no
     * distribution, and false if it does.
     *
     * @param asgn
     * @return
     * @throws ServicesException 
     */
    public boolean isDistEmpty(GradableEvent ge) throws ServicesException;
    
    /**
     * Returns a map that maps a TA to a Set of Groups that TA has been assigned to grade for the given Part. There will
     * be an entry in the map for each TA; if a TA has not been assigned any groups to grade, the value for that TA's
     * entry in the map will be an empty Set.
     * 
     * @param part
     * @return
     * @throws ServicesException 
     */
    public Map<TA, Set<Group>> getDistribution(Part part) throws ServicesException;
    
    /**
     * Assigns Groups for each TA to grade for each Part. Any existing distributions will be overwritten.
     *
     * @param distribution
     * @throws ServicesException 
     */
    public void setDistribution(Map<Part, Map<TA, Set<Group>>> distribution) throws ServicesException;

    /**
     * Returns a Set of Groups that the given TA is assigned to grade for the given Part part. Returns an empty Set if
     * no groups are assigned to the TA for the given Part or if there is no distribution for the Part in the database.
     *
     * @param part
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Set<Group> getAssignedGroups(Part part, TA ta) throws ServicesException;

    /**
     * Returns a Set of Groups that have been assigned to any TA to grade for the given Part. This can be used to find
     * students who have not been assigned to any TA to grade.  If no distribution exists yet, an empty Set will be
     * returned.
     *
     * @param part
     * @return
     * @throws ServicesException 
     */
    public Set<Group> getAssignedGroups(Part part) throws ServicesException;
    
    /**
     * Returns a Set of Parts for which the given TA has been assigned at least one Group to grade.
     *
     * @param ta
     * @return
     * @throws ServicesException 
     */
    public Set<Part> getPartsWithAssignedGroups(TA ta) throws ServicesException;
    
    /**
     * Returns the TA who has been assigned to grade the given Group for the given Part. If no such TA exists,
     * {@code null} will be returned.
     * 
     * @param part
     * @param group
     * @return
     * @throws ServicesException
     */
    @Deprecated
    public TA getGrader(Part part, Group group) throws ServicesException;

    /**
     * Sets the TA who has been assigned to grade the given Group and  Part. Pass {@code ta} as {@code null} to
     * have the given group and part be unassigned. This overwrites any existing assignment for the group for the part.
     * <br/><br/>
     * For efficiency reasons this method should not be used create an initial automated distribution. For that purpose,
     * use {@link #setDistribution(java.util.Map)}.
     * 
     * @param part
     * @param group
     * @param ta
     * @throws ServicesException
     */
    @Deprecated
    public void setGrader(Part part, Group group, TA ta) throws ServicesException;

    /**
     * Sets the same extension for each group in {@code groups} for the {@code gradableEvent}. The groups must belong
     * to the same assignment that the gradable event belongs to, an exception will be thrown if not.
     * 
     * @param gradableEvent
     * @param groups
     * @param ontime
     * @param shiftDates
     * @param note may be {@code null}
     * @throws ServicesException if thrown no changes to extensions will have occurred
     */
    public void setExtensions(GradableEvent gradableEvent, Set<Group> groups, DateTime ontime, boolean shiftDates,
            String note) throws ServicesException;
    
    /**
     * Returns a mapping of group to the extensions for that group in {@code groups}. The groups must belong to the
     * same assignment that {@code gradableEvent} belongs to, an exception will be thrown if not. If a group does not
     * have an extension it will not be in the mapping.
     * 
     * @param gradableEvent
     * @param groups
     * @return
     * @throws ServicesException 
     */
    public Map<Group, Extension> getExtensions(GradableEvent gradableEvent, Set<Group> groups) throws ServicesException;

    /**
     * Deletes extensions for each group in {@code groups} for the {@code gradableEvent}. The groups must belong to the
     * same assignment that the gradable event belongs to, an exception will be thrown if not.
     * 
     * @param gradableEvent
     * @param groups
     * @throws ServicesException if thrown no changes to extensions will have occurred
     */
    public void deleteExtensions(GradableEvent gradableEvent, Set<Group> groups) throws ServicesException;
        
    public GroupGradingSheet getGroupGradingSheet(Part part, Group group) throws ServicesException;
    
    public Map<Part, Map<Group, GroupGradingSheet>> getGroupGradingSheets(SetMultimap<Part, Group> toRetrieve) throws ServicesException;
    
    public void saveGroupGradingSheet(GroupGradingSheet groupGradingSheet) throws ServicesException;
    
    /**
     * Marks the given group grading sheets as submitted or unsubmitted based on the given boolean.  If marking as
     * submitted, all of the group grading sheets will be saved first.
     * 
     * @param groupGradingSheets
     * @param submitted
     * @throws ServicesException 
     */
    public void setGroupGradingSheetsSubmitted(Set<GroupGradingSheet> groupGradingSheets,
                                               boolean submitted) throws ServicesException;
    
    /**
     * Returns the points earned for the given Group and Part. If no such value is stored in the database, {@code null}
     * will be returned.
     *
     * @param group
     * @param part
     * @return
     * @throws ServicesException
     */
    @Deprecated
    public PartGrade getEarned(Group group, Part part) throws ServicesException;
    
    /**
     * Returns a mapping from group to part grade for each group that has a value stored in the database. If no value
     * is stored in the database for a given group, that group will not be the key set.
     * 
     * @param groups
     * @param part
     * @return
     * @throws ServicesException 
     */
    @Deprecated
    public Map<Group, PartGrade> getEarned(Set<Group> groups, Part part) throws ServicesException;
    
    /**
     * Returns an immutable set of all TAs.
     * 
     * @return
     */
    public Set<TA> getTAs();
    
    /**
     * Returns an immutable set of the TAs who are default graders.
     * 
     * @return
     */
    public Set<TA> getDefaultGraders();
    
    /**
     * Returns the TA with the corresponding id, {@code null} if there is no such TA.
     * 
     * @param taId
     * @return 
     */
    public TA getTA(int taId);
    
    /**
     * Returns the Group for which the given Student is a member for the given Assignment. If the given Assignment is
     * not a group assignment and the Student does not already have a group of one, a group of one will be created,
     * stored in the database, and returned. This method returns {@code null} if no such Group exists.
     *
     * @param asgn
     * @param student
     * @return
     * @throws ServicesException
     */
    public Group getGroup(Assignment asgn, Student student) throws ServicesException;
    
    /**
     * Returns all Groups that have been created for the given Assignment. A ServicesException will be thrown if an
     * invalid group ID is present in the database or if a database error occurred.  If the given Assignment is not
     * a group assignment, groups of one will be created and stored in the database for each student who does not
     * already have a group of one. Returns an empty Set if the given Assignment is a group Assignment and no groups
     * have yet been created for it.
     * 
     * @param asgn
     * @return
     * @throws ServicesException 
     */
    public Set<Group> getGroups(Assignment asgn) throws ServicesException;
    
    /**
     * Returns the Group with the corresponding id, {@null} if no such group exists.
     * 
     * @param groupId
     * @return 
     */
    public Group getGroup(int groupId) throws ServicesException;
    
    /**
     * Returns an immutable ordered list of all assignment.
     * 
     * @return
     */
    public List<Assignment> getAssignments();
    
    /**
     * Returns the deadline info for the given gradable event.
     * 
     * @param gradableEvent
     * @return
     * @throws ServicesException 
     */
    public DeadlineInfo getDeadlineInfo(GradableEvent gradableEvent) throws ServicesException;
    
    /**
     * Returns the gradable event occurrences for the given gradable events and groups. If a group does not have a
     * recorded gradable event occurrence it will not be in the map.
     * 
     * @param gradableEvent
     * @param groups
     * @return
     * @throws ServicesException 
     */
    public Map<Group, GradableEventOccurrence> getGradableEventOccurrences(GradableEvent gradableEvent,
            Set<Group> groups) throws ServicesException;

    /**
     * For each entry in the given map, stores the corresponding gradable event occurrence in the database for the
     * corresponding Group and given GradableEvent. Any existing gradable event occurrences for groups with entries in
     * the map will be overwritten.
     *
     * @param gradableEvent 
     * @param statuses
     * @throws ServicesException
     */
    public void setGradableEventOccurrences(GradableEvent gradableEvent, Map<Group, DateTime> statuses)
            throws ServicesException;
    
    /**
     * Deletes gradable event occurrences for the given gradable event for each group. If there is currently no gradable
     * event for a given group there will be no effect for that group and an exception will not be thrown.
     * 
     * @param gradableEvent
     * @param groups
     * @throws ServicesException 
     */
    public void deleteGradableEventOccurrences(GradableEvent gradableEvent, Set<Group> groups) throws ServicesException;
    
    /**
     * Loads Student and Group objects into memory for all students and groups in the database.
     *
     * @throws ServicesException
     */
    public void updateDataCache() throws ServicesException;
    
    /**
     * Returns the Student object corresponding to the given studentLogin. If no such student exists in the database,
     * {@code null} will be returned.
     *
     * @param studentLogin
     * @return
     * @throws ServicesException
     */
    public Student getStudentFromLogin(String studentLogin) throws ServicesException;

    /**
     * Returns whether or not the given student login corresponds to a valid Student object.
     * 
     * @param studentLogin
     * @return
     * @throws ServicesException
     */
    public boolean isStudentLoginInDatabase(String studentLogin) throws ServicesException;
}