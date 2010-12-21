package gradesystem.services;

import gradesystem.config.Assignment;
import gradesystem.config.HandinPart;
import gradesystem.config.LabPart;
import gradesystem.config.TA;
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
    public void importLabGrades(LabPart part);

    /**
     * Makes the user's grading directory as specified by {@link #getUserGradingDirectory()}.
     *
     * @return success of making directory
     */
    public boolean makeUserGradingDirectory();

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
     * Returns whether or not some member of the given student's group for the given
     * project is on the given TA's blacklist.
     *
     * @param studentLogin
     * @param ta
     * @return true if a group member is on the TA's blacklist; false otherwise
     */
    public boolean groupMemberOnTAsBlacklist(String studentLogin, HandinPart part, TA ta);

    /**
     * present the user with a dialog warning them that some of the handins are for students
     * who are not in the database or who are not enabled. allow the user to choose a method
     * for resolution. either add/enable them or ignore the handin.
     *
     * @param asgn
     * @return what are the remaining bad logins (null if the user clicked Cancel)
     */
    public Collection<String> resolveMissingStudents(Assignment asgn);

    /**
     * updates the touched file that goes represents the student's lab grade
     *
     * @param labPart
     * @param score
     * @param student
     * @author aunger
     */
    public void updateLabGradeFile(LabPart labPart, double score, String student);
}