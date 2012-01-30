package cakehat.services;

import cakehat.newdatabase.TA;
import java.util.List;
import support.utils.posix.NativeException;

/**
 * Services relating to users. Unlike <code>UserUtilities</code>, these methods
 * are specific to cakehat.
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
    public TA getUser() throws ServicesException;

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
     * Returns the logins of all memberes in the course's student group.
     *
     * @return
     * @throws NativeException thrown if the student group does not exist
     */
    public List<String> getStudentLogins() throws NativeException;
    
    /**
     * Returns the logins of all members in the course's TA group.
     *
     * @return
     * @throws NativeException thrown if the TA group does not exist
     */
    public List<String> getTALogins() throws NativeException;
    
    /**
     * Returns the logins of all members in the course's HTA group.
     *
     * @return 
     * @throws NativeException thrown if the HTA group does not exist
     */
    public List<String> getHTALogins() throws NativeException;

}