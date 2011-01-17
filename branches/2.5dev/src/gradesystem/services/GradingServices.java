package gradesystem.services;

import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.LabPart;
import gradesystem.config.TA;
import gradesystem.database.Group;
import gradesystem.handin.DistributablePart;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

/**
 * This class provides grading specific utility functions.
 *
 * @author jeldridg
 * @author jak2
 */
public interface GradingServices
{
    /**
     * Import grades for a lab part into the database.
     *
     * @param part
     */
    public void importLabGrades(LabPart part) throws ServicesException;

    /**
     * Makes the user's grading directory as specified by {@link #getUserGradingDirectory()}.
     */
    public void makeUserGradingDirectory() throws ServicesException;

    /**
     * Removes the user's grading directory as specified by {@link #getUserGradingDirectory()}.
     *
     * @return success of removing directory
     */
    public boolean removeUserGradingDirectory();

    /**
     * Gets the path to the temporary directory that the user uses while running
     * cakehat.
     * <br><br>
     * Path is: /course/<course>/.cakehat/.<talogin>/
     * <br><br>
     * This directory <b>should</b> be deleted when cakehat is closed.
     *
     * @return path to a TA's temporary grading directory
     */
    public String getUserGradingDirectory();

    /**
     * The directory the handin is unarchived into.
     *
     * /course/<course>/.cakehat/.<ta login>/<assignment name>/<distributable part name>/<group name>/
     *
     * @return
     */
    public File getUnarchiveHandinDirectory(DistributablePart part, Group group);

    /**
     * The directory containing all of the handins for an assignment.
     *
     * /course/<course>/handin/<assignment name>/<current year>/
     *
     * @param assignment
     * @return
     */
    public File getHandinDirectory(Assignment assignment);

    /**
     * The absolute path to a student's GRD file for a given handin part.
     *
     * @param part
     * @param studentLogin
     * @return
     */
    public String getStudentGRDPath(HandinPart part, String studentLogin);

    /**
     * Opens a new EmailView so that user TA can inform students that their assignment
     * has been graded.  Default settings:
     *  FROM:    user TA
     *  TO:      user TA
     *  CC:      grades TA & grades HTA
     *  BCC:     students the user TA is assigned to grade for this assignment, as selected
     *  SUBJECT: "[<course code>] <project> Graded"
     *  MESSAGE: "<project> has been graded and is available for pickup in the handback bin."
     *
     * @param project
     * @param students
     */
    public void notifyStudents(HandinPart part, Vector<String> students, boolean emailRubrics);

    /**
     * Prints .GRD files for each student the user TA has graded
     * Calls getPrinter(...) and then prints using lpr
     *
     * @param assignment assignment for which .GRD files should be printed
     */
    public void printGRDFiles(HandinPart part, Iterable<String> studentLogins);

    /**
     * Prints GRD files for the handin parts and student logins specified. The
     * GRD files must already exist in order for this to work.
     * 
     * @param toPrint
     */
    public void printGRDFiles(Map<HandinPart, Iterable<String>> toPrint);

    /**
     * Prompts the user to a select a printer.
     *
     * @return printer selected
     */
    public String getPrinter();

    /**
     * Print dialogue for selecting printer.  Message passed in will be displayed as instructions to the user
     * 
     * @param message
     * @return the name of the printer selected
     */
    public String getPrinter(String message);

    /**
     * Returns whether or not it is OK to distribute the student with the given
     * login to the given TA for the given assignment.  It is always OK to distribute
     * the student if no member of the student's group is on the TA's blacklist.
     * If a group member is on the TA's blacklist, a dialog will be shown asking
     * the user whether or not to continue.  If the user selects the continue option,
     * this method returns true; otherwise, it will return false.
     * 
     * @param asgn
     * @param student
     * @param ta
     * @return true if it is OK to distribute the student's handin to the TA
     */
    public boolean isOkToDistribute(Assignment asgn, String student, TA ta) throws ServicesException;

    /**
     * Returns whether or not some member of the given student's group for the given
     * project is on the given TA's blacklist.
     *
     * @param studentLogin
     * @param ta
     * @return true if a group member is on the TA's blacklist; false otherwise
     */
    public boolean groupMemberOnTAsBlacklist(String studentLogin, HandinPart part, TA ta) throws ServicesException;

    /**
     * present the user with a dialog warning them that some of the handins are for students
     * who are not in the database or who are not enabled. allow the user to choose a method
     * for resolution. either add/enable them or ignore the handin.
     *
     * @param asgn
     * @return what are the remaining bad logins (null if the user clicked Cancel)
     */
    public Collection<String> resolveMissingStudents(Assignment asgn) throws ServicesException;

    /**
     * Updates the student's lab grade by deleting the file that previously
     * represented the lab grade and creating a new one.
     *
     * @param labPart
     * @param score
     * @param student the student's login
     * @author aunger, jak2
     */
    public void updateLabGradeFile(LabPart labPart, double score, String student);

    /**
     * Gets the extensions for each student in studentLogins. Takes into account
     * the group the student is in; using the latest date of all group members.
     * If there is no extension for any member of the group, null is assigned.
     *
     *
     * @param part
     * @param studentLogins
     * @return
     */
    public Map<String, Calendar> getExtensions(HandinPart part, Iterable<String> studentLogins) throws ServicesException;
}