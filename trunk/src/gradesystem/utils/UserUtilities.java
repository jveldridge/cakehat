package gradesystem.utils;

import gradesystem.Allocator;
import gradesystem.config.TA;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import gradesystem.utils.system.NativeFunctions;

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
     * Returns the logins of all students in the class's student group.
     *
     * @return
     */
    public Collection<String> getStudentLogins()
    {
        return this.getMembers(Allocator.getCourseInfo().getStudentGroup());
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
     * Returns whether or not the current user is a TA for the course as
     * specified by the configuration file.
     *
     * @return whether user is a TA
     */
    public boolean isUserTA()
    {
        String userLogin = this.getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getTAs())
        {
            if(ta.getLogin().equals(userLogin))
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Returns whether or not the current user is an admin for the course as
     * specified by the configuration file.
     *
     * @return whether the user is an Admin
     */
    public boolean isUserAdmin()
    {
        String userLogin = this.getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getTAs())
        {
            if(ta.getLogin().equals(userLogin) && ta.isAdmin())
            {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Returns whether or not the current user is an HTA for the course as
     * specified by the configuration file.
     *
     * @return whether user is a HTA
     */
    public boolean isUserHTA()
    {
        String userLogin = this.getUserLogin();

        for(TA ta : Allocator.getCourseInfo().getHTAs())
        {
            if(ta.getLogin().equals(userLogin))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether or not the student with given login studentLogin is a
     * member of the course's student group.
     * 
     * @return true if the student with login studentLogin is a member of the
     *         course's student group; false otherwise
     */
    public boolean isInStudentGroup(String studentLogin)
    {
        return this.isMemberOfGroup(studentLogin, Allocator.getCourseInfo().getStudentGroup());
    }

    /**
     * Returns whether or not the user specified by <code>taLogin</code> is a
     * member of the course's TA group.
     * 
     * @return true if the user is a member of the course's TA group
     */
    public boolean isInTAGroup(String taLogin)
    {
        return this.isMemberOfGroup(taLogin, Allocator.getCourseInfo().getTAGroup());
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