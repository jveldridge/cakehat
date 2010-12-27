package utils.system;

import java.util.List;
import java.io.File;

/**
 * Methods that build directly upon native functions.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class NativeFunctions
{
    private final LibCWrapper _wrapper;

    public NativeFunctions()
    {
        _wrapper = new LibCWrapper();
    }

    /**
     * Changes the permissions on a file or directory.
     *
     * @param file the file or directory
     * @param mode the <b>octal</b> form permission, such as 0770 or 0666
     *
     * @throws NativeException thrown if the file/directory does not exist or
     * the permissions cannot be changed
     */
    public void chmod(File file, int mode) throws NativeException
    {
        _wrapper.chmod(file.getAbsolutePath(), mode);
    }

    /**
     * Returns all of the logins of the members of the group.
     *
     * @return
     *
     * @throws NativeException thrown if the group does not exist
     */
    public List<String> getGroupMembers(String groupName) throws NativeException
    {
        return _wrapper.getgrnam(groupName).getMembers();
    }

    /**
     * Returns the real name (e.g. Joshua Kaplan) that corresponds with <code>
     * login</code>. If the login does not exist then an exception will be
     * thrown.
     * <br/><br/>
     * <code>null</code> will be returned if the login exists but there is no
     * real name associated with it. This should not occur on Brown CS
     * Department machines, but it may when running cakehat locally.
     *
     * @param login
     * @return the real name of the user specified by login
     *
     * @throws NativeException thrown if login does not exist
     */
    public String getRealName(String login) throws NativeException
    {
        return _wrapper.getpwnam(login).getRealName();
    }

    /**
     * Returns whether the login exists.
     *
     * @param login
     * @return if login exists
     */
    public boolean isLogin(String login)
    {
        boolean exists = false;
        try
        {
            _wrapper.getpwnam(login);
            exists = true;
        }
        catch(NativeException e) { }

        return exists;
    }

    /**
     * Returns the login of the user executing this code.
     *
     * @return login
     */
    public String getUserLogin()
    {
        //getuid is guaranteed to never fail
        //getpwuid should never fail for a valid uid
        //Therefore, if an exception occurs, something is seriously wrong with
        //native system calls, and there is no hope of handling it
        try
        {

            return _wrapper.getpwuid(_wrapper.getuid()).getUserName();
        }
        catch (NativeException ex)
        {
            throw new RuntimeException("Unable to retrieve user's login. " +
                    "This should never happen, something is very wrong with " +
                    "native functionality.", ex);
        }
    }
}