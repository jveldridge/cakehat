package support.utils;

import java.util.Set;
import support.utils.posix.NativeException;

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
    public Set<String> getMembers(String group) throws NativeException;

    /**
     * Returns the POSIX user id that corresponds with {@code login}. If the login does not exist then an exception will
     * be thrown.
     * 
     * @param login
     * @return user id
     * @throws NativeException 
     */
    public int getUserId(String login) throws NativeException;
    
    /**
     * Returns the POSIX user id of the user executing this code.
     * 
     * @return user id
     */
    public int getUserId();
    
    /**
     * Returns the user login.
     *
     * @return user login
     */
    public String getUserLogin();
    
    
    /**
     * Returns the login corresponding to the POSIX user id {@code uid}. If the {@code uid} does not exist then an
     * exception will be thrown.
     * 
     * @param userId
     * @return
     * @throws NativeException 
     */
    public String getUserLogin(int userId) throws NativeException;

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
     * Returns whether or not the user specified by {@code login} is a member of the group specified by {@code group}.
     * If the user is a member of the group true is returned.
     *
     * @return if a member of the group
     *
     * @throws NativeException thrown if the group does not exist
     */
    public boolean isMemberOfGroup(String login, String group) throws NativeException;

    /**
     * Returns whether or not the user is remotely connected (such as over ssh) to the computer running cakehat.
     *
     * @return
     * @throws NativeException
     */
    public boolean isUserRemotelyConnected() throws NativeException;
}