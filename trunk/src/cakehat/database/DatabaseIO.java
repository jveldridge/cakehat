package cakehat.database;

import cakehat.config.Assignment;
import cakehat.config.Part;
import cakehat.config.TA;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
import cakehat.rubric.TimeStatus;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface to be implemented by classes providing database interaction; mandates
 * methods such classes must have.
 *
 * @author jeldridg
 */
public interface DatabaseIO {


    /**
     * Checks to see if the student already exists. If not, creates new entry
     * in the database.  Newly added students are enabled by default.
     *
     * @param studentLogin
     * @param studentFirstName
     * @param studentLastName
     */
    public void addStudent(String studentLogin, String studentFirstName, String studentLastName) throws SQLException;

    /**
     * Marks the student as disabled; use instead of removing if a student has dropped
     * the course.  Disabled students will not be sent grade reports or counted for statistics.
     *
     * @param studentLogin
     */
    public void disableStudent(String studentLogin) throws SQLException;

    /**
     * "Undo" of disabling a student.  All active students must be enabled to
     * receive grade reports and be counted for statistics.
     *
     * @param studentLogin
     */
    public void enableStudent(String studentLogin) throws SQLException;

    /**
     * Returns true if the student is enabled and false otherwise
     * @param studentLogin
     * @return
     */
    public boolean isStudentEnabled(String studentLogin) throws SQLException;

    /**
     * Returns a Map of all students currently in the database
     * Key: String studentLogin
     * Value: String studentName (as "FirstName LastName")
     *
     * @return
     */
    public Map<String,String> getAllStudents() throws SQLException;

    /**
     * Returns a Map of all currently enabled student
     * Key: String studentLogin
     * Value: String studentName (as "FirstName LastName")
     *
     * @return
     */
    public Map<String,String> getEnabledStudents() throws SQLException;

    /**
     * Adds all the students in students to the blacklist of the given TA, if
     * not already blacklisted.  This ensures that this TA will not be distributed
     * this student to grade. If the student was already blacklisted it is not added
     * again. If there was an error a SQLException will be thrown.
     *
     * @param students
     * @param taLogin
     */
    public void blacklistStudents(Collection<String> studentLogins, TA ta) throws SQLException;

    /**
     * Removes students in studentLogins from the blacklist of the given TA,
     * if the student was on that TA's blacklist.  If the student was not on the
     * TA's blacklist, this method has no effect.
     *
     * @param studentLogins
     * @param ta
     */
    public void unBlacklistStudents(Collection<String> studentLogins, TA ta) throws SQLException;

    /**
     * Indicates whether the Assignment has a distribution.  Returns true if
     * the Assignment currently has no distribution, and false if it does.
     *
     * @param asgn
     * @return
     */
    public boolean isDistEmpty(Assignment asgn) throws SQLException;

    /**
     * Returns the logins of all students who are on some TA's blacklist.
     *
     * @return
     */
    public Collection<String> getBlacklistedStudents() throws SQLException;

    /**
     * Returns the logins of all students who are on this TA's blacklist
     * @param ta
     * @return
     */
    public Collection<String> getTABlacklist(TA ta) throws SQLException;

    /**
     * Assigns a Group group to the given TA to grade for
     * DistributablePart part, if not assigned already.  This will enable the TA to
     * open, run, grade, etc. the Group's code for the given DistributablePart.
     *
     * NOTE: This method should not be used to create an initial distribution
     *       for a project; it should be used only to reassign grading.
     *       To create an initial distribution, use setAsgnDist(...), below.
     *
     * @param group
     * @param part
     * @param ta
     */
    public void assignGroupToGrader(Group group, DistributablePart part, TA ta) throws SQLException, CakeHatDBIOException;

    /**
     * Unassigns Group group from the given TA for DistributablePart part.
     *
     * @param group
     * @param part
     * @param ta
     */
    public void unassignGroupFromGrader(Group group, DistributablePart part, TA ta) throws SQLException;

    /**
     * Creates a distribution for each DistributablePart part, mapping TAs
     * to Collections of Groups that TA is assigned to grade for each DistributablePart.
     * Any existing distributions will be overwritten.
     *
     * @param distribution
     */
    public void setDistributablePartDist(Map<DistributablePart, Map<TA, Collection<Group>>> distribution) throws SQLException, CakeHatDBIOException;

    /**
     * Returns a Collection of Groups that the given TA
     * is assigned to grade for the DistributablePart part.  Returns an empty Collection
     * if no groups are assigned to the TA for the given DistributablePart or if there is no
     * distribution for the DistributablePart in the database.
     *
     * @param part
     * @param ta
     * @return
     */
    public Collection<Group> getGroupsAssigned(DistributablePart part, TA ta) throws SQLException, CakeHatDBIOException;

    /**
     * Returns a Collection of Groups containing the groups who
     * have been assigned to any TA to grade for the given DistributablePart.  This can be
     * used to find students who have not been assigned to any TA to grade.  If no
     * distribution exists yet, an empty Collection will be returned.
     *
     * @param part
     * @return
     */
    public Collection<Group> getAllAssignedGroups(DistributablePart part) throws SQLException, CakeHatDBIOException;

    /**
     * Returns a Collection of DistributableParts for which the TA has someone
     * assigned to them.
     *
     * @param ta
     * @return
     */
    public Set<DistributablePart> getDPsWithAssignedStudents(TA ta) throws SQLException;

    //may add: public Map<TA, Collection<String>> getDistribution(String assignmentName);

    /**
     * Grants an extension for the Group group for the Handin
     * handin.  The group's handin will now be due at the date/time represented by
     * the calendar newDate.  String note can be used to store a message explaining why
     * the extension was granted.
     *
     * @param group
     * @param handin
     * @param newDate
     * @param note
     */
    public void grantExtension(Group group, Handin handin, Calendar newDate, String note) throws SQLException;

    /**
     * Removes a previously granted extension for the given group for the given Handin.
     * If the group did not previously have an extension, this method has no effect.
     *
     * @param group
     * @param handin
     */
    public void removeExtension(Group group, Handin handin) throws SQLException;

    /**
     * Grants an exemption for the Group group for the Part
     * part.  The group's emailed grade report will indicate that the student
     * has been exempted for this Part instead of showing a 0.  Additionally,
     * this Part will not be taken into account in the calculation of final grades
     * (if support for final grade calculation is added).
     *
     * @param group
     * @param part
     * @param note
     */
    public void grantExemption(Group group, Part part, String note) throws SQLException;

    /**
     * Removes a previously granted exemption for the given group for the given Part.
     * If the group did not previously have an exemption, this method has no effect.
     *
     * @param group
     * @param part
     */
    public void removeExemption(Group group, Part part) throws SQLException;

    /**
     * Returns the Calendar representing the date when the Handin is due
     * for Group group if that group has an extension.  Returns
     * null if the group does not have an extension.
     *
     * @param group
     * @param handin
     * @return
     */
    public Calendar getExtension(Group group, Handin handin) throws SQLException;

    /**
     * Returns the Map of Groups to extensions for a specific Handin.
     *
     * @param handin
     * @return
     */
    public Map<Group, Calendar> getAllExtensions(Handin handin) throws SQLException;

    /**
     * Returns a string containing a message to indicate why the Group group
     * has been granted an extension on Handin handin.  Returns null
     * if the group does not have an extension.
     *
     * @param group
     * @param handin
     * @return
     */
    public String getExtensionNote(Group group, Handin handin) throws SQLException;


    /**
     * Returns the Map of Part to groups for a specific Assignment a.
     *
     * @return a map of exemptions given for the given assignment
     * @throws SQLException
     */
    public Map<Part, Collection<Group>> getAllExemptions(Assignment a) throws SQLException;

    /**
     * Returns a string containing a message to indicate why the Group group
     * has been granted an exemption on Part part.
     * Returns null if the group does not have an exemption.
     *
     * @param group
     * @param part
     * @return
     */
    public String getExemptionNote(Group group, Part part) throws SQLException;

    /**
     * Assigns a grade of score to Group group on Part part.
     *
     * @param group
     * @param part
     * @param score
     */
    public void enterGrade(Group group, Part part, double score) throws SQLException;

    /**
     * Returns the score of Group group on Part part.
     *
     * @param group
     * @param part - part getting score for
     * @return
     */
    public Double getGroupScore(Group group, Part part) throws SQLException;

    /**
     * Returns the score of Group group on an Assignment asgn.
     *
     * @param group
     * @param asgn - assignment getting score for
     * @return
     */
    public Double getGroupAsgnScore(Group group, Assignment asgn) throws SQLException;

    /**
     * Returns a map of all scores for the specified Groups for the
     * specified Part with Groups as the keys and their scores as the values.
     * Any Groups with no score stored in the database will not have an entry
     * in the returned Map.
     *
     * @param part
     * @param groups
     * @return
     */
    public Map<Group,Double> getPartScoresForGroups(Part part, Iterable<Group> groups) throws SQLException;

    /**
     * Returns a map of all scores for the specified Groups for the
     * specified Assignment with Groups as the keys and their
     * scores as the values.
     * @param asgn
     * @param groups
     * @return
     */
    public Map<Group,Double> getAssignmentScoresForGroups(Assignment asgn, Iterable<Group> groups) throws SQLException;

    /**
     * pulls the distribution for a DistributablePart from the DB
     * @param handin
     * @return
     */
    public Map<TA, Collection<Group>> getDistribution(DistributablePart handin) throws SQLException, CakeHatDBIOException;

    /**
     * adds a set of groups for an Assignment
     * @param asgn
     * @param groupings
     */
    public void setGroups(Assignment asgn, Collection<Group> groups) throws SQLException;

    /**
     * adds a group for an Assignment
     * @param asgn
     * @param group
     */
    public void setGroup(Assignment asgn, Group group) throws SQLException;

    /**
     * Gets the group for a student for an Assignment. If no group exists then
     * <code>null</code> is returned.
     *
     * @param asgn
     * @param student
     * @return
     */
    public Group getStudentsGroup(Assignment asgn, String student) throws SQLException;

    /**
     * return all the groups that have been created for an assignment
     * @param handin
     * @return
     */
    public Collection<Group> getGroupsForAssignment(Assignment asgn) throws SQLException;

    /**
     * removes a group for a specific Assignment
     * @param asgn
     * @param group
     * @return
     */
    public void removeGroup(Assignment asgn, Group group) throws SQLException;

    /**
     * removes all groups for a specific Assignment
     * @param asgn
     * @return
     */
    public void removeGroupsForAssignment(Assignment asgn) throws SQLException;

    public TA getGraderForGroup(DistributablePart part, Group group) throws SQLException, CakeHatDBIOException;

    /**
     * returns all the graders for a specific student
     * @param studentlogin
     * @return map of asgn to ta
     */
    public Map<DistributablePart, TA> getGradersForStudent(String studentLogin) throws SQLException, CakeHatDBIOException;

    /**
     * For the given Handin and Group the HandinStatus is stored in the DB.
     * In there is an existing record for that Handin and Group the currently stored value is removed.
     *
     * @param handin
     * @param group
     * @param status
     */
    public void setHandinStatus(Handin handin, Group group, HandinStatus status) throws SQLException;

    /**
     * For the given Handin and Groups the TimeStatus is stored in the DB.
     * Statuses maps Groups to their associated HandinStatus.
     * If there is an existing record for that Handin and Group then currently stored value is removed.
     *
     * @param handin
     * @param statuses
     */
    public void setHandinStatuses(Handin handin, Map<Group, HandinStatus> statuses) throws SQLException;

    /**
     * The HandinStatus for the Handin and Group is returned. If no record in the DB exists,
     * null will be returned
     *
     * @param handin
     * @param group
     * @return
     */
    public HandinStatus getHandinStatus(Handin handin, Group group) throws SQLException;

    /**
     * Returns whether or not at least one group for the given Handin has had a
     * handin status set.
     * 
     * @param handin
     * @return
     * @throws SQLException
     * @throws CakeHatDBIOException
     */
    public boolean areHandinStatusesSet(Handin handin) throws SQLException, CakeHatDBIOException;

    /**
     * Removes all data from database tables and rebuilds the tables. If no DB
     * file exists or is empty then it will be set to the initial configuration.
     */
    public void resetDatabase() throws SQLException;

}
