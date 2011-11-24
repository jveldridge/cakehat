package support.utils.posix;

import com.sun.jna.Platform;
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
     * Changes the permissions on a file or directory. The user
     * <strong>must</strong> be the owner of the file or directory.
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
     * Changes the group of the file or directory to be the group passed in. In
     * order to change the group, the user invoking this method must be the
     * owner of the file.
     *
     * @param file
     * @param group
     *
     * @throws NativeException thrown if unable to change the file to the
     * specified group. Can occur for reasons including: file does not exist,
     * group does not exist, or group is not owned by user.
     */
    public void changeGroup(File file, String group) throws NativeException
    {
        int gid = _wrapper.getgrnam(group).getGID();

        //Per the chown(2) documentation:
        //One of the owner or group id's may be left unchanged by specifying it as -1.
        int uid = -1;

        _wrapper.chown(file.getAbsolutePath(), uid, gid);
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

    /**
     * Returns whether the user executing this code is remotely connected to
     * the computer running cakehat.
     *
     * @return
     * @throws NativeException
     */
    public boolean isUserRemotelyConnected() throws NativeException
    {
        //If the code is not running on Linux, just assume the user is running
        //locally because determining this information is not supported on
        //non-Linux platforms
        if(!Platform.isLinux())
        {
            return false;
        }

        String userLogin = this.getUserLogin();
        boolean isRemote = false;

        //Open user account database
        _wrapper.setutxent();

        //Just in case something fails, always close the user account database
        try
        {
            NativeUTMPX utmpx = _wrapper.getutxent();
            while(utmpx != null)
            {
                //If the entry is for this user and the IP address is remote
                if(userLogin.equals(utmpx.getUser()) && utmpx.isRemoteIP())
                {
                    isRemote = true;
                    break;
                }

                utmpx = _wrapper.getutxent();
            }
        }
        finally
        {
            //Close user account database
            _wrapper.endutxent();
        }

        return isRemote;
    }
}