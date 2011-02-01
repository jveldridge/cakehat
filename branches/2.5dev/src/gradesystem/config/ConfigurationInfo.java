package gradesystem.config;

import gradesystem.handin.DistributablePart;
import java.util.Collection;

/**
 *
 * @author jak2
 */
public interface ConfigurationInfo
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
     * Returns the DistributablePart object representing the DP with the given ID.
     * Returns null if no such DP exists.
     *
     * @param partID
     * @return
     */
    public DistributablePart getDistributablePart(String partID);

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
     *
     * @return
     */
    public Collection<TA> getTAs();

    /**
     * Returns a collection of TAs that are default graders.
     *
     * @return
     */
    public Collection<TA> getDefaultGraders();

    /**
     * Returns a collection of TAs who are not default graders.
     *
     * @return
     */
    public Collection<TA> getNonDefaultGraders();

    /**
     * Returns a collection of TAs that are default graders.
     *
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
     * Returns the addresses that should be notified of actions such as grade
     * submission.
     *
     * @return
     */
    public Collection<String> getNotifyAddresses();
}