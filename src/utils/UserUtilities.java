package utils;

import config.TA;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import utils.system.NativeFunctions;

/**
 * Utilities that are login-related.
 */
public class UserUtilities
{
    public enum ValidityCheck {BYPASS, CHECK};

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

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login (i.e., is not snoopable)
     * or is not in the course's student group; the user will then have the option
     * of adding the student anyway or cancelling the operation.  The user's
     * first and last name will be set to the firstName and lastName parameters,
     * respectively.
     *
     * @param studentLogin
     * @param firstName
     * @param lastName
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     */
    public void addStudent(String studentLogin, String firstName,
            String lastName, ValidityCheck checkValidity) {

        if (checkValidity == ValidityCheck.CHECK) {
            boolean isLoginValid = this.isLoginValid(studentLogin);
            boolean isInStudentGroup = this.isInStudentGroup(studentLogin);

            String warningMessage = "";
            if (!isLoginValid) {
                warningMessage += String.format("The login %s is not a valid (snoopable) login\n",
                        studentLogin);
            }
            else if (!isInStudentGroup) {
                warningMessage += String.format("The login %s is not in the student group",
                        studentLogin);
            }

            if (!isLoginValid || !isInStudentGroup) {
                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(null, warningMessage,
                        "Invalid Student Login",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);

                if (shouldContinue != JOptionPane.OK_OPTION) {
                    return;
                }
            }
        }

        Allocator.getDatabaseIO().addStudent(studentLogin, firstName, lastName);
    }

}
