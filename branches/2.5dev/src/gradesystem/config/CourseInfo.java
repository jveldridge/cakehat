package gradesystem.config;

import gradesystem.handin.DistributablePart;
import java.util.Collection;
import java.util.Set;

/**
 * Constants used throughout the program.
 *
 * @author jak2
 */
public interface CourseInfo
{
    /**
     * Returns a collection of all assignments.
     *
     * @return
     */
    public Collection<Assignment> getAssignments();

    /**
     * Returns the email account that emails can be sent from.
     * 
     * @return
     */
    public EmailAccount getEmailAccount();

    /**
     * Returns the addresses that should be notified of actions such as grade
     * submission.
     *
     * @return
     */
    public Collection<String> getNotifyAddresses();

    /**
     * Returns the course name (that matches its course directory), e.g. cs015.
     *
     * @return
     */
    public String getCourse();

    /**
     * Minutes of leniency when determing if an assignment meets a deadline.
     *
     * @return
     */
    public int getMinutesOfLeniency();

    /**
     * Default options for how to submit a graded assignment.
     *
     * @return
     */
    public SubmitOptions getSubmitOptions();

    /**
     * Returns a collection of all of the TAs.
     * @return
     */
    public Collection<TA> getTAs();

    /**
     * Returns a collection of TAs that are default graders.
     * @return
     */
    public Collection<TA> getDefaultGraders();

    /**
     * Returns a collection of TAs who are not default graders.
     * @return
     */
    public Collection<TA> getNonDefaultGraders();

    /**
     * Returns a collection of TAs that are default graders.
     * @return
     */
    public Collection<TA> getAdmins();

    /**
     * Returns the TA object representing the TA with the given login.
     * Returns null if no such TA exists.
     *
     * @return the TA object representing the TA with the given login.
     */
    public TA getTA(String taLogin);

    /**
     * Returns the DistributablePart object representing the DP with the given ID.
     * Returns null if no such DP exists.
     * 
     * @param partID
     * @return
     */
    public DistributablePart getDistributablePart(String partID);

    /**
     * Returns a collection of all assignments that have a handin.
     * 
     * @return
     */
    public Collection<Assignment> getHandinAssignments();

    /**
     * Returns a collection of all assignment that have a nonhandin part.
     *
     * @return
     */
    public Collection<Assignment> getNonHandinAssignments();

    /**
     * Returns a collection of all assignments that have a lab part.
     *
     * @return
     */
    public Collection<Assignment> getLabAssignments();

    /**
     * The course's test account.
     *
     * <course>000
     *
     * @return
     */
    public String getTestAccount();

    /**
     * The course's student group.
     *
     * <course>student
     *
     * @return
     */
    public String getStudentGroup();

    /**
     * The course's TA group.
     *
     * <course>ta
     *
     * @return
     */
    public String getTAGroup();

    /**
     * Course directory.
     *
     * /course/<course>/
     *
     * @return
     */
    public String getCourseDir();

    /**
     * Handin directory.
     *
     * /course/<course>/handin/
     *
     * @return
     */
    public String getHandinDir();

    /**
     * The directory where cakehat exists.
     *
     * /course/<course>/.cakehat/
     *
     * @return
     */
    public String getGradingDir();

    /**
     * The directory where the lab check off data is stored.
     *
     * /course/<course>/.cakehat/<current year>/labs/
     *
     * @return
     */
    public String getLabsDir();

    /**
     * The top level directory that stores all of the GML rubric files.
     *
     * /course/<course>/.cakehat/<current year>/rubrics/
     *
     * @return
     */
    public String getRubricDir();

    /**
     * The path to the database file.
     *
     * /course/<course>/.cakehat/<current year>/database/database.db
     *
     * @return
     */
    public String getDatabaseFilePath();

    /**
     * The directory that backups of the database are put in.
     *
     * /course/<course>/.cakehat/<current year>/database/backups/
     *
     * @return
     */
    public String getDatabaseBackupDir();

    /**
     * The email domain of student and TA logins: cs.brown.edu
     *
     * (Technically this isn't course info, but this is a convenient place to
     * put it.)
     *
     * @return
     */
    public String getEmailDomain();

    /**
     * This is the cakehat email address.
     *
     * (Technically this isn't course info, but this is a convenient place to
     * put it.)
     *
     * @return
     */
    public String getCakehatEmailAddress();

    /**
     * Returns a set of the assignments where there is a choice of which one students can do. (cs15 fnl prjs)
     * @return Set of assignment numbers
     *
     * @deprecated Assignment numbers must now be unique, but part numbers do
     * not need to be
     */
    public Set<Integer> getAssignmentsWithChoices();
}