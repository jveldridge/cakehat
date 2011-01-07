package utils;

import java.util.List;
import utils.system.NativeException;

/**
 * Utilities that are login-related.
 */
public interface UserUtilities
{
    /**
     * Returns all logins of a given group.
     *
     * @param group
     * @return
     *
     * @throws NativeException thrown if the group does not exist
     */
    public List<String> getMembers(String group) throws NativeException;

    /**
     * Returns the user login.
     *
     * @return user login
     */
    public String getUserLogin();

    /**
     * Returns a user's real name.
     *
     * @param login the user's login
     * @return user's name
     *
     * @throws NativeException thrown if the login is not valid (does not exist)
     */
    public String getUserName(String login) throws NativeException;

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
     * member of the group true is returned.
     *
     * @return if a member of the group
     *
     * @throws NativeException thrown if the group does not exist
     */
    public boolean isMemberOfGroup(String login, String group) throws NativeException;
}