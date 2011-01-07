package gradesystem.services;

import gradesystem.config.TA;
import java.util.List;
import utils.system.NativeException;

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
     * if the given studentLogin is not a valid login or is not in the course's
     * student group; the user will then have the option of adding the student
     * anyway or cancelling the operation.  The students's first and last name
     * will be set to the firstName and lastName parameters, respectively.
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
    public void addStudent(String studentLogin, String firstName, String lastName,
            ValidityCheck checkValidity) throws ServicesException;

    /**
     * Adds the given studentLogin to the database.  A warning will be shown
     * if the given studentLogin is not a valid login or is not in the course's
     * student group; the user will then have the option of adding the student
     * anyway or cancelling the operation.  The students's first and last name
     * will be looked up.
     *
     * @param studentLogin
     * @param checkValidity parameter that indicates whether the student should be
     *                    added to the database without checking that the login is
     *                    valid and that the the student is in the course student
     *                    group.  This should be passed as BYPASS when both of
     *                    these conditions are known to be true (for example, when
     *                    adding all members of the course group)
     */
    public void addStudent(String studentLogin, ValidityCheck checkValidity) throws ServicesException;

    /**
     * Returns the TA object representing the current user.
     *
     * @return the TA object representing the current user.
     */
    public TA getUser();

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
     *
     * @throws NativeException thrown if the student group does not exist
     */
    public boolean isInStudentGroup(String studentLogin) throws NativeException;

    /**
     * Returns whether or not the user specified by <code>taLogin</code> is a
     * member of the course's TA group.
     *
     * @return true if the user is a member of the course's TA group
     *
     * @throws NativeException thrown if the TA group does not exist
     */
    public boolean isInTAGroup(String taLogin) throws NativeException;

    /**
     * Returns the logins of all students in the class's student group.
     *
     * @return logins of the student in the class's student group
     *
     * @throws NativeException thrown if the student group does not exist
     */
    public List<String> getStudentLogins() throws NativeException;

    /**
     * Returns the login of the given TA, or, if the given TA object is null,
     * "UNASSIGNED"
     * 
     * @param ta
     * @return
     */
    public String getSanitizedTALogin(TA ta);

    /**
     * Returns the name of the given TA, or, if the given TA object is null,
     * "UNASSIGNED"
     *
     * @param ta
     * @return
     */
    public String getSanitizedTAName(TA ta);
}