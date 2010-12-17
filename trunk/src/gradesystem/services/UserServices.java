package gradesystem.services;

import gradesystem.Allocator;
import gradesystem.config.TA;
import java.util.Collection;
import javax.swing.JOptionPane;

/**
 * Services relating to users. Unlike <code>UserUtilities</code>, these methods
 * are specific to cakehat.
 *
 * @author jak2
 */
public class UserServices
{
    public enum ValidityCheck {BYPASS, CHECK};

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
            String lastName, ValidityCheck checkValidity)
    {

        if (checkValidity == ValidityCheck.CHECK)
        {
            boolean isLoginValid = Allocator.getUserUtilities().isLoginValid(studentLogin);
            boolean isInStudentGroup = this.isInStudentGroup(studentLogin);

            String warningMessage = "";
            if (!isLoginValid)
            {
                warningMessage += String.format("The login %s is not a valid (snoopable) login\n",
                        studentLogin);
            }
            else if (!isInStudentGroup)
            {
                warningMessage += String.format("The login %s is not in the student group",
                        studentLogin);
            }

            if (!isLoginValid || !isInStudentGroup)
            {
                Object[] options = {"Proceed", "Cancel"};
                int shouldContinue = JOptionPane.showOptionDialog(null, warningMessage,
                        "Invalid Student Login",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);

                if (shouldContinue != JOptionPane.OK_OPTION)
                {
                    return;
                }
            }
        }

        Allocator.getDatabaseIO().addStudent(studentLogin, firstName, lastName);
    }

    /**
     * Returns whether or not the current user is a TA for the course as
     * specified by the configuration file.
     *
     * @return whether user is a TA
     */
    public boolean isUserTA()
    {
        String userLogin = Allocator.getUserUtilities().getUserLogin();

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
        String userLogin = Allocator.getUserUtilities().getUserLogin();

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
        String userLogin = Allocator.getUserUtilities().getUserLogin();

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
        return Allocator.getUserUtilities().isMemberOfGroup(studentLogin, Allocator.getCourseInfo().getStudentGroup());
    }

    /**
     * Returns whether or not the user specified by <code>taLogin</code> is a
     * member of the course's TA group.
     *
     * @return true if the user is a member of the course's TA group
     */
    public boolean isInTAGroup(String taLogin)
    {
        return Allocator.getUserUtilities().isMemberOfGroup(taLogin, Allocator.getCourseInfo().getTAGroup());
    }

    /**
     * Returns the logins of all students in the class's student group.
     *
     * @return
     */
    public Collection<String> getStudentLogins()
    {
        return Allocator.getUserUtilities().getMembers(Allocator.getCourseInfo().getStudentGroup());
    }
}