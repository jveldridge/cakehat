package cakehat.config;

import cakehat.handin.DistributablePart;
import java.util.List;

/**
 * Information that either comes from the configuration file or is built
 * directly from it.
 *
 * @author jak2
 */
public interface ConfigurationInfo
{
    /**
     * Returns an immutable list of all assignments.
     *
     * @return
     */
    public List<Assignment> getAssignments();

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
     * Returns an immutable list of all of the TAs.
     *
     * @return
     */
    public List<TA> getTAs();

    /**
     * Returns an immutable list of TAs that are default graders.
     *
     * @return
     */
    public List<TA> getDefaultGraders();

    /**
     * Returns an immutable list of TAs who are not default graders.
     *
     * @return
     */
    public List<TA> getNonDefaultGraders();

    /**
     * Returns an immutable list of TAs that are default graders.
     *
     * @return
     */
    public List<TA> getAdmins();

    /**
     * Returns the TA object representing the TA with the given login.
     * Returns null if no such TA exists.
     *
     * @param the login of the TA
     * @return
     */
    public TA getTA(String taLogin);

    /**
     * Returns an immutable list of all assignments that have a handin.
     *
     * @return
     */
    public List<Assignment> getHandinAssignments();

    /**
     * Returns an immutable list of all assignment that have a nonhandin part.
     *
     * @return
     */
    public List<Assignment> getNonHandinAssignments();

    /**
     * Returns an immutable list of all assignments that have a lab part.
     *
     * @return
     */
    public List<Assignment> getLabAssignments();

    /**
     * Returns an immutable list of the addresses that should be notified of
     * actions such as grade submission.
     *
     * @return
     */
    public List<String> getNotifyAddresses();
}