package utils;

import java.util.List;

/**
 * Utilities that are login-related.
 */
public interface UserUtilities
{
    /**
     * Returns all logins of a given group. If the group does not exist, <code>
     * null</code> is returned.
     *
     * @param group
     * @return
     */
    public List<String> getMembers(String group);

    /**
     * Returns the user login.
     *
     * @return user login
     */
    public String getUserLogin();

    /**
     * Returns a user's real name. If the login does not exist, <code>null
     * </code> is returned.
     *
     * @param login the user's login
     * @return user's name
     */
    public String getUserName(String login);

    /**
     * Returns if a login is valid.
     *
     * @param login the user's login
     * @return true if the login exists
     */
    public boolean isLoginValid(String login);

    /**
     * Returns whether or not the user specified by <code>login</code> is a
     * member of the group specified by <code>group</code>. If the user is a
     * member of the group true is returned. If the group does not exist or the
     * user is not a member false is returned.
     *
     * @return if a member of the group
     */
    public boolean isMemberOfGroup(String login, String group);
}