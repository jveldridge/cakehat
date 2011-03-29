package cakehat.services;

/**
 * Constants that exist throughout cakehat.
 *
 * @author jak2
 */
public interface Constants
{
    /**
     * The email domain of student and TA logins
     * <br/><br/>
     * <pre>
     * {@code
     * cs.brown.edu
     * }
     * </pre>
     *
     * @return
     */
    public String getEmailDomain();

    /**
     * The cakehat email address.
     * <br/><br/>
     * <pre>
     * {@code
     * cakehat@cs.brown.edu
     * }
     * </pre>
     *
     * @return
     */
    public String getCakehatEmailAddress();
}
