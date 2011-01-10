package gradesystem.database;

import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.Part;
import gradesystem.config.TA;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

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
     * @return status true if the student was added; false if the student was
     *         already in the database and thus not added
     */
    public boolean addStudent(String studentLogin, String studentFirstName, String studentLastName) throws SQLException;

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
     * Adds student with login studentLogin to the blacklist of the given TA, if
     * not already blacklisted.  This ensures that this TA will not be distributed
     * this student to grade.  The return value indicates whether or not the student
     * was in fact added to the TA's blacklist.
     *
     * @param studentLogin
     * @param taLogin
     * @return true if the student was added to the TA's blacklist; false if the
     *         student was already on the TA's blacklist and was thus not added
     *         again
     */
    public boolean blacklistStudent(String studentLogin, TA ta) throws SQLException;

    /**
     * Removes student with login studentLogin from the blacklist of the given TA,
     * if the student was on that TA's blacklist.  If the student was not on the
     * TA's blacklist, this method has no effect.
     *
     * @param studentLogin
     * @param ta
     */
    public void unBlacklistStudent(String studentLogin, TA ta) throws SQLException;

    /**
     * Indicates whether the HandinPart has a distribution.  Returns true if
     * the HandinPart currently has no distribution, and false if it does.
     *
     * @param asgn
     * @return
     */
    public boolean isDistEmpty(HandinPart asgn) throws SQLException;

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
     * Assigns student with login studentLogin to the given TA to grade for
     * HandinPart part, if not assigned already.  This will enable the TA to
     * open, run, grade, etc. the student's code for the given HandinPart.
     * The return value indicates whether or not the student was added.
     *
     * NOTE: This method should not be used to create an initial distribution
     *       for a project; it should be used only to reassign grading.
     *       To create an initial distribution, use setAsgnDist(...), below.
     *
     * @param studentLogin
     * @param assignmentName
     * @param ta
     */
    public void assignStudentToGrader(String studentLogin, HandinPart part, TA ta) throws SQLException, CakeHatDBIOException;

    /**
     * Unassigns student with login studentLogin to the given TA to grade for
     * HandinPart part.
     *
     * @param studentLogin
     * @param assignmentName
     * @param ta
     * @return
     */
    public void unassignStudentFromGrader(String studentLogin, HandinPart part, TA ta) throws SQLException;

    /**
     * Creates a distribution for the HandinPart part, mapping TAs
     * to Collections of Strings of studentLogins that TA is assigned to grade.
     * Any existing distribution will be overwritten.
     *
     * @param assignmentName
     * @param distribution
     */
    public void setAsgnDist(HandinPart part, Map<TA, Collection<String>> distribution) throws SQLException;

    /**
     * Returns a Collection of Strings of student logins that the given TA
     * is assigned to grade for the HandinPart part.  Returns an empty Collection
     * if no students are assigned to the TA for the given Part or if there is no
     * distributionfor the Part in the database.
     *
     * @param assignmentName
     * @param taLogin
     * @return
     */
    public Collection<String> getStudentsAssigned(HandinPart part, TA ta) throws SQLException;

    /**
     * Returns a Collection of Strings containing the logisn of all students who
     * have been assinged to a TA to grade for the given HandinPart.  This can be
     * used to find students who have not been assigned to any TA to grade.  If no
     * distribution exists yet, an empty Collection will be returned.
     *
     * @param part
     * @return
     */
    public Collection<String> getAllAssignedStudents(HandinPart part) throws SQLException;

    //may add: public Map<TA, Collection<String>> getDistribution(String assignmentName);

    /**
     * Grants an extension for the student with login studentLogin for the HandinPart
     * part.  The student's handin will now be due at the date/time represented by
     * the calendar newDate.  String note can be used to store a message explaining why
     * the extension was granted.
     *
     * @param studentLogin
     * @param assignmentName
     * @param newDate
     * @param note
     */
    public void grantExtension(String studentLogin, Part part, Calendar newDate, String note) throws SQLException;

    /**
     * Removes a previously granted extension for the given student for the given Part.
     * If the student did not previously have an extension, this method has no effect.
     *
     * @param studentLogin
     * @param part
     */
    public void removeExtension(String studentLogin, Part part) throws SQLException;

    /**
     * Grants an extension for the student with login studentLogin for the HandinPart
     * part.  The student's emailed grade report will indicate that the student
     * has been exempted for theis assignment instead of showing a 0.  Additionally,
     * this assignment will not be taken into account in the calculation of final grades
     * (if support for final grade calculation is added).
     *
     * @param studentLogin
     * @param assignmentName
     * @param note
     */
    public void grantExemption(String studentLogin, Part part, String note) throws SQLException;

    /**
     * Removes a previously granted exemption for the given student for the given Part.
     * If the student did not previously have an exemption, this method has no effect.
     * 
     * @param studentLogin
     * @param part
     */
    public void removeExemption(String studentLogin, Part part) throws SQLException;

    /**
     * Returns the Calendar representing the date when the Part is due
     * for student with login studentLogin if that student has an extension.  Returns
     * null if the student does not have an extension.
     *
     * @param studentLogin
     * @param assignmentName
     * @return
     */
    public Calendar getExtension(String studentLogin, Part part) throws SQLException;

    /**
     * Returns the Map of student to extensions for a specific Part.  Returns
     * null on error and empty map if no students have extension for the project.
     *
     * @param studentLogin
     * @param assignmentName
     * @return
     */
    public Map<String, Calendar> getExtensions(Part part) throws SQLException;

    /**
     * Returns a string containing a message to indicate why the student with login
     * studentLogin has been granted an extension on Part part.  Returns null
     * if the student does not have an extension.
     *
     * @param studentLogin
     * @param assignmentName
     * @return
     */
    public String getExtensionNote(String studentLogin, Part part) throws SQLException;

    /**
     * Returns a string containing a message to indicate why the student with login
     * studentLogin has been granted an exemption on Part part.  This takes a Part
     * rather than a HandinPart because exemptions can also be granted on NON_HANDIN
     * and LAB parts.  Returns null if the student does not have an exemption.
     *
     * @param studentLogin
     * @param assignmentName
     * @return
     */
    public String getExemptionNote(String studentLogin, Part part) throws SQLException;

    /**
     * Assigns a grade of score to student with login studentLogin on Part part.
     *
     * @param studentLogin
     * @param assignmentName
     * @param score
     */
    public void enterGrade(String studentLogin, Part part, double score) throws SQLException;

    /**
     * Returns the score of student with login studentLogin on Part part.
     *
     * @param studentLogin
     * @param part - part getting score for
     * @return
     */
    public double getStudentScore(String studentLogin, Part part) throws SQLException;

    /**
     * Returns the score of student with login studentLogin on an Assignment asgn.
     *
     * @param studentLogin
     * @param asgn - assignment getting score for
     * @return
     */
    public double getStudentAsgnScore(String studentLogin, Assignment asgn) throws SQLException;

    /**
     * Returns a map of all scores for the specified students for the
     * specified Part with students' logins as the keys and their
     * scores as the values.
     * @param part
     * @return
     */
    public Map<String,Double> getPartScores(Part part, Iterable<String> students) throws SQLException;

    /**
     * Returns a map of all scores for the specified students for the
     * specified Assignment with students' logins as the keys and their
     * scores as the values.
     * @param part
     * @return
     */
    public Map<String,Double> getAssignmentScores(Assignment asgn, Iterable<String> students) throws SQLException;

    /**
     * pulls the distribution for a handin part from the DB
     * @param handin
     * @return
     */
    public Map<TA, Collection<String>> getDistribution(HandinPart handin) throws SQLException;

    /**
     * adds a set of groups for a handin part
     * @param handin
     * @param groupings
     * @return
     */
    public boolean setGroups(HandinPart handin, Map<String, Collection<String>> groupings) throws SQLException;

    /**
     * adds a group for a handin part
     * @param handin
     * @param group
     * @return
     */
    public boolean setGroup(HandinPart handin, String groupName, Collection<String> group) throws SQLException;

    /**
     * get the group for a student for a handin
     * @param handin
     * @param student
     * @return
     */
    public Collection<String> getGroup(HandinPart handin, String student) throws SQLException;

    /**
     * gets the whole map of students to their group members
     * @param handin
     * @return
     */
    public Map<String, Collection<String>> getGroups(HandinPart handin) throws SQLException;

    /**
     * removes a group for a specfic handin part
     * @param handin
     * @param group
     * @return
     */
    public void removeGroup(HandinPart handin, Collection<String> group) throws SQLException;

    /**
     * removes all groups for a specfic handin part
     * @param handin
     * @param group
     * @return
     */
    public void removeGroups(HandinPart handin) throws SQLException;

    /**
     * returns all the graders for a specific student
     * @param studentlogin
     * @return map of asgn to ta
     */
    public Map<Assignment, TA> getAllGradersForStudent(String studentLogin) throws SQLException, CakeHatDBIOException;

    /**
     * Removes all data from database tables and rebuilds the tables. If no DB
     * file exists or is empty then it will be set to the initial configuration.
     */
    public void resetDatabase() throws SQLException;
}
