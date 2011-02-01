package gradesystem.config;

import gradesystem.handin.DistributablePart;
import java.io.File;
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
     * Retrieves the course code, e.g. cs000. This is done by examining the
     * location of the running code. If it is determined that the code is
     * running from the cakehat jar as it would be during normal operation, the
     * course code is extracted from the path. If the code is instead believed
     * to be running in development mode, a hard coded test value is used.
     *
     * @return
     */
    public String getCourse();

    /**
     * The course's test account.
     *
     * <pre>
     * {@code
     * <course>000
     * }
     * </pre>
     *
     * @return
     */
    public String getTestAccount();

    /**
     * The course's student group.
     *
     * <pre>
     * {@code
     * <course>student
     * }
     * </pre>
     *
     * @return
     */
    public String getStudentGroup();

    /**
     * The course's TA group.
     *
     * <pre>
     * {@code
     * <course>ta
     * }
     * </pre>
     *
     * @return
     */
    public String getTAGroup();

    /**
     * Builds the path to the configuration file.
     *
     * <pre>
     * {@code
     * /course/<course code>/.cakehat/<current year>/config/config.xml
     * }
     * </pre>
     *
     * @return
     */
    public File getConfigurationFile();

    /**
     * Course directory.
     *
     * <pre>
     * {@code
     * /course/<course>/
     * }
     * </pre>
     *
     * @return
     */
    public String getCourseDir();

    /**
     * Handin directory.
     *
     * <pre>
     * {@code
     * /course/<course>/handin/
     * }
     * </pre>
     *
     * @return
     */
    public String getHandinDir();

    /**
     * The directory where cakehat exists.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/
     * }
     * </pre>
     *
     * @return
     */
    public String getGradingDir();

    /**
     * The top level directory that stores all of the GML rubric files.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/rubrics/
     * }
     * </pre>
     *
     * @return
     */
    public String getRubricDir();

    /**
     * The path to the database file.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/database/database.db
     * }
     * </pre>
     *
     * @return
     */
    public String getDatabaseFilePath();

    /**
     * The directory that backups of the database are put in.
     *
     * <pre>
     * {@code
     * /course/<course>/.cakehat/<current year>/database/backups/
     * }
     * </pre>
     *
     * @return
     */
    public String getDatabaseBackupDir();

    /**
     * The email domain of student and TA logins: cs.brown.edu
     * <br/><br/>
     * (Technically this isn't course info, but this is a convenient place to
     * put it.)
     *
     * @return
     */
    public String getEmailDomain();

    /**
     * This is the cakehat email address.
     * <br/><br/>
     * (Technically this isn't course info, but this is a convenient place to
     * put it.)
     *
     * @return
     */
    public String getCakehatEmailAddress();










    /**
     * Returns a set of the assignments where there is a choice of which one
     * students can do. (cs15 fnl prjs)
     *
     * @return Set of assignment numbers
     *
     * @deprecated Assignment numbers must now be unique, but part numbers do
     * not need to be. Part numbers that are equal are considered mututally
     * exclusive, in this manner cs015 final projects and similar setups are
     * supported.
     */
    public Set<Integer> getAssignmentsWithChoices();



    // Deprecated methods, these methods all forward to ConfigurationInfo
    // They are here for the moment to reduce merge conflicts


    /**
     * Returns a collection of all assignments.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<Assignment> getAssignments();

    /**
     * Returns the email account that emails can be sent from.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public EmailAccount getEmailAccount();

    /**
     * Returns the DistributablePart object representing the DP with the given ID.
     * Returns null if no such DP exists.
     *
     * @deprecated see ConfigurationInfo
     * @param partID
     * @return
     */
    public DistributablePart getDistributablePart(String partID);

        /**
     * Minutes of leniency when determing if an assignment meets a deadline.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public int getMinutesOfLeniency();

    /**
     * Default options for how to submit a graded assignment.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public SubmitOptions getSubmitOptions();

    /**
     * Returns a collection of all of the TAs.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<TA> getTAs();

    /**
     * Returns a collection of TAs that are default graders.
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<TA> getDefaultGraders();

    /**
     * Returns a collection of TAs who are not default graders.
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<TA> getNonDefaultGraders();

    /**
     * Returns a collection of TAs that are default graders.
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<TA> getAdmins();

    /**
     * Returns the TA object representing the TA with the given login.
     * Returns null if no such TA exists.
     *
     * @deprecated see ConfigurationInfo
     * @return the TA object representing the TA with the given login.
     */
    public TA getTA(String taLogin);

    /**
     * Returns a collection of all assignments that have a handin.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<Assignment> getHandinAssignments();

    /**
     * Returns a collection of all assignment that have a nonhandin part.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<Assignment> getNonHandinAssignments();

    /**
     * Returns a collection of all assignments that have a lab part.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<Assignment> getLabAssignments();

    /**
     * Returns the addresses that should be notified of actions such as grade
     * submission.
     *
     * @deprecated see ConfigurationInfo
     * @return
     */
    public Collection<String> getNotifyAddresses();
}