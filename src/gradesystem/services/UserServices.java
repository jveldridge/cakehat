package gradesystem.services;

import java.util.Collection;

/**
 * Services relating to users. Unlike <code>UserUtilities</code>, these methods
 * are specific to cakehat.
 *
 * @author jak2
 */
public interface UserServices
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
            String lastName, ValidityCheck checkValidity);

    /**
     * Returns whether or not the current user is a TA for the course as
     * specified by the configuration file.
     *
     * @return whether user is a TA
     */
    public boolean isUserTA();

    /**
     * Returns whether or not the current user is an admin for the course as
     * specified by the configuration file.
     *
     * @return whether the user is an Admin
     */
    public boolean isUserAdmin();

    /**
     * Returns whether or not the current user is an HTA for the course as
     * specified by the configuration file.
     *
     * @return whether user is a HTA
     */
    public boolean isUserHTA();

    /**
     * Returns whether or not the student with given login studentLogin is a
     * member of the course's student group.
     *
     * @return true if the student with login studentLogin is a member of the
     *         course's student group; false otherwise
     */
    public boolean isInStudentGroup(String studentLogin);

    /**
     * Returns whether or not the user specified by <code>taLogin</code> is a
     * member of the course's TA group.
     *
     * @return true if the user is a member of the course's TA group
     */
    public boolean isInTAGroup(String taLogin);

    /**
     * Returns the logins of all students in the class's student group.
     *
     * @return
     */
    public Collection<String> getStudentLogins();
}