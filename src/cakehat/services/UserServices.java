package cakehat.services;

import cakehat.database.TA;
import java.util.Set;
import support.utils.posix.NativeException;

/**
 * Services relating to users. Unlike {@code UserUtilities}, these methods are specific to cakehat.
 *
 * @author jak2
 */
public interface UserServices
{
    /**
     * Returns the TA object representing the current user.
     *
     * @return the TA object representing the current user.
     */
    public TA getUser();

    /**
     * Returns whether or not the student with given login {@code studentLogin} is a member of the course's student
     * group.
     *
     * @return {@code true} if the student with login {@code studentLogin} is a member of the course's student group;
     * {@code false} otherwise
     *
     * @throws NativeException thrown if the student group does not exist
     */
    public boolean isInStudentGroup(String studentLogin) throws NativeException;

    /**
     * Returns the logins of all members in the course's student group.
     *
     * @return
     * @throws NativeException thrown if the student group does not exist
     */
    public Set<String> getStudentLogins() throws NativeException;

    /**
     * Returns the logins of all members in the course's TA group.
     *
     * @return
     * @throws NativeException thrown if the TA group does not exist
     */
    public Set<String> getTALogins() throws NativeException;

    /**
     * Returns the logins of all members in the course's HTA group.
     *
     * @return 
     * @throws NativeException thrown if the HTA group does not exist
     */
    public Set<String> getHTALogins() throws NativeException;
}