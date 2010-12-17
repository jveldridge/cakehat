package utils;

import java.util.HashMap;
import java.util.List;
import utils.system.NativeFunctions;

/**
 * Utilities that are login-related.
 */
public class UserUtilities
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();
    private static final HashMap<String, List<String>> GROUP_MEMBERS = new HashMap<String, List<String>>();
    private static final String USER_LOGIN = NATIVE_FUNCTIONS.getUserLogin();

    /**
     * Returns all logins of a given group. If the group does not exist, <code>
     * null</code> is returned.
     *
     * @param group
     * @return
     */
    public List<String> getMembers(String group)
    {
        List<String> members = GROUP_MEMBERS.get(group);

        if(!GROUP_MEMBERS.containsKey(group))
        {
            members = NATIVE_FUNCTIONS.getGroupMembers(group);
            GROUP_MEMBERS.put(group, members);
        }

        return members;
    }

    /**
     * Returns the user login.
     *
     * @return user login
     */
    public String getUserLogin()
    {
        return USER_LOGIN;
    }

    /**
     * Returns a user's real name. If the login does not exist, <code>null
     * </code> is returned.
     *
     * @param login the user's login
     * @return user's name
     */
    public String getUserName(String login)
    {
        return NATIVE_FUNCTIONS.getRealName(login);
    }

    /**
     * Returns if a login is valid.
     *
     * @param login the user's login
     * @return true if the login exists
     */
    public boolean isLoginValid(String login)
    {
        return NATIVE_FUNCTIONS.isLogin(login);
    }

    /**
     * Returns whether or not the user specified by <code>login</code> is a
     * member of the group specified by <code>group</code>. If the user is a
     * member of the group true is returned. If the group does not exist or the
     * user is not a member false is returned.
     *
     * @return if a member of the group
     */
    public boolean isMemberOfGroup(String login, String group)
    {
        boolean isMember = false;
        List<String> logins = this.getMembers(group);

        if(logins != null)
        {
            isMember = logins.contains(login);
        }

        return isMember;
    }
}