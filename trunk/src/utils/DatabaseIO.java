package utils;

import config.Assignment;
import config.HandinPart;
import config.Part;
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
     * Checks if the assignment part already exists.  If not, creates a new
     * entry in the database
     * 
     * @param part - a Part subclass
     * @return
     */
    public boolean addAssignmentPart(Part part);

    /**
     * Checks if the assignment already exists.  If not, creates a new
     * entry in the database
     *
     * @param asgn - a Assignment subclass
     * @return
     */
    public boolean addAssignment(Assignment asgn);

    /**
     * Checks to see if the TA already exists. If not, creates a new entry
     * in the database.
     * 
     * @param taLogin
     * @return status
     */
    public boolean addTA(String taLogin);

    /**
     * Checks to see if the student already exists. If not, creates new entry
     * in the database.  Newly added students are enabled by default.
     * 
     * @param studentLogin
     * @param studentFirstName
     * @param studentLastName
     * @return status
     */
    public boolean addStudent(String studentLogin, String studentFirstName, String studentLastName);

    /**
     * Marks the student as disabled; use instead of removing if a student has dropped
     * the course.  Disabled students will not be sent grade reports or counted for statistics.
     * 
     * @param studentLogin
     * @return
     */
    public boolean disableStudent(String studentLogin);

    /**
     * "Undo" of disabling a student.  All active students must be enabled to
     * receive grade reports and be counted for statistics.
     * 
     * @param studentLogin
     * @return
     */
    public boolean enableStudent(String studentLogin);
    
    /**
     * Returns true if the student is enabled and false otherwise
     * @param studentLogin
     * @return
     */
    public boolean isStudentEnabled(String studentLogin);

    /**
     * Returns a Map of all students currently in the database
     * Key: String studentLogin
     * Value: String studentName (as "FirstName LastName")
     * 
     * @return
     */
    public Map<String,String> getAllStudents();
    
    /**
     * Returns a Map of all currently enabled student
     * Key: String studentLogin
     * Value: String studentName (as "FirstName LastName")
     * 
     * @return
     */
    public Map<String,String> getEnabledStudents();

    /**
     * Adds student with login studentLogin to the blacklist of TA
     * with login taLogin.  This ensures that this TA will not be 
     * distributed this student to grade.
     * 
     * @param studentLogin
     * @param taLogin
     * @return
     */
    public boolean blacklistStudent(String studentLogin, String taLogin);
    
    /**
     * Removes student with login studentLogin from the blacklist of TA
     * with login taLogin, if the student was on that TA's blacklist.
     * 
     * @param studentLogin
     * @param taLogin
     * @return
     */
    public boolean unBlacklistStudent(String studentLogin, String taLogin);

    /**
     * Indicates whether the HandinPart has a distribution.  Returns true if
     * the HandinPart currently has no distribution, and false if it does.
     * 
     * @param asgn
     * @return
     */
    public boolean isDistEmpty(HandinPart asgn);

    /**
     * Returns the logins of all students who are on some TA's blacklist.
     * 
     * @return
     */
    public Collection<String> getBlacklistedStudents();

    /**
     * Returns the logins of all students who are on this TA's blacklist
     * @param taLogin
     * @return
     */
    public Collection<String> getTABlacklist(String taLogin);

    /**
     * Assigns student with login studentLogin to TA with login taLogin to grade for
     * HandinPart part.  This will enable the TA to open, run, grade, etc. the student's
     * code for the given HandinPart.
     * 
     * NOTE: This method should not be used to create an initial distribution
     *       for a project; it should be used only to reassign grading.
     *       To create an initial distribution, use setAsgnDist(...), below.
     * 
     * @param studentLogin
     * @param assignmentName
     * @param taLogin
     * @return
     */
    public boolean assignStudentToGrader(String studentLogin, HandinPart part, String taLogin);

    /**
     * Unassigns student with login studentLogin to TA with login taLogin to grade for
     * HandinPart part.
     * 
     * @param studentLogin
     * @param assignmentName
     * @param taLogin
     * @return
     */
    public boolean unassignStudentFromGrader(String studentLogin, HandinPart part, String taLogin);

    /**
     * Creates a distribution for the HandinPart part, mapping TA logins (as strings)
     * to Collections of Strings of studentLogins that TA is assigned to grade.
     * 
     * @param assignmentName
     * @param distribution
     * @return
     */
    public boolean setAsgnDist(HandinPart part, Map<String,Collection<String>> distribution);

    /**
     * Returns a Collection of Strings of student logins that the TA with login
     * taLogin is assigned to grade for the HandinPart part.
     * 
     * @param assignmentName
     * @param taLogin
     * @return
     */
    public Collection<String> getStudentsAssigned(HandinPart part, String taLogin);

    //may add: public Map<String,String> getDistribution(String assignmentName);

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
     * @return
     */
    public boolean grantExtension(String studentLogin, Part part, Calendar newDate, String note);
    
    /**
     * Removes a previously granted extension for the given student for the given Part.
     * @param studentLogin
     * @param part
     * @return
     */
    public boolean removeExtension(String studentLogin, Part part);

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
     * @return
     */
    public boolean grantExemption(String studentLogin, Part part, String note);
    
    /**
     * Removes a previously granted exemption for the given student for the given Part.
     * @param studentLogin
     * @param part
     * @return
     */
    public boolean removeExemption(String studentLogin, Part part);

    /**
     * Returns the Calendar representing the date when the Part is due
     * for student with login studentLogin if that student has an extension.  Returns
     * null if the student does not have an extension.
     * 
     * @param studentLogin
     * @param assignmentName
     * @return
     */
    public Calendar getExtension(String studentLogin, Part part);

    /**
     * Returns the Map of student to extensions for a specific Part.  Returns
     * null on error and empty map if no students have extension for the project.
     *
     * @param studentLogin
     * @param assignmentName
     * @return
     */
    public Map<String, Calendar> getExtensions(Part part);

    /**
     * Returns a string containing a message to indicate why the student with login
     * studentLogin has been granted an extension on Part part.  Returns null
     * if the student does not have an extension.
     * 
     * @param studentLogin
     * @param assignmentName
     * @return
     */
    public String getExtensionNote(String studentLogin, Part part);

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
    public String getExemptionNote(String studentLogin, Part part);

    /**
     * Assigns a grade of score to student with login studentLogin on Part part.
     * 
     * @param studentLogin
     * @param assignmentName
     * @param score
     * @return
     */
    public boolean enterGrade(String studentLogin, Part part, double score);

    /**
     * Returns the score of student with login studentLogin on Part part.
     * 
     * @param studentLogin
     * @param assignmentName
     * @param score
     * @param status
     * @return
     */
    public double getStudentScore(String studentLogin, Part part);
    
    /**
     * Returns a map of all scores for the specified students for the 
     * specified Part with students' logins as the keys and their 
     * scores as the values.
     * @param part
     * @return
     */
    public Map<String,Double> getPartScores(Part part, Iterable<String> students);
    
    /**
     * Returns a map of all scores for the specified students for the 
     * specified Assignment with students' logins as the keys and their 
     * scores as the values.
     * @param part
     * @return
     */
    public Map<String,Double> getAssignmentScores(Assignment asgn, Iterable<String> students);

    /**
     * Resets the database: removes all students, scores, graders, distributions,
     * and blacklists; then adds assignment parts as specified in the config file.
     * 
     * @return
     */
    public boolean resetDatabase();
}
