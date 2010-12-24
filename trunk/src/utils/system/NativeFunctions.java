package utils.system;

import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import java.io.File;

/**
 * Java wrappers for native C functions in Linux/Unix.
 * <br/><br/>
 * <strong>Note:</strong> This code will not run properly if the underlying
 * native function library does not exist, so there may be issues of running
 * this code on non-department machines.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class NativeFunctions
{
    /**
     * This interface matches the native functions of the LibC library. To see
     * more information about any of these functions, consult the man pages.
     * For instance to see information on 'getlogin' then in a terminal the
     * command would be 'man getlogin'.
     */
    private static interface LibC extends Library
    {
        public int chmod(String filename, int mode);
        public NativeGroup getgrnam(String which);
        public NativeUserInformation getpwnam(String login);
        public int getuid();
        public NativeUserInformation getpwuid(int uid);
    }

    private final LibC _libC;

    public NativeFunctions()
    {
        _libC = (LibC) Native.loadLibrary("c", LibC.class);
    }

    /**
     * Changes the permissions on a file or directory.
     *
     * @param file the file or directory
     * @param mode the <b>octal</b> form permission, such as 0770 or 0666
     * @return success of performing chmod
     */
    public boolean chmod(File file, int mode)
    {
        int returnVal = _libC.chmod(file.getAbsolutePath(), mode);
        //Returns 0 on success
        boolean success = (returnVal == 0);

        return success;
    }

    /**
     * Returns all of the logins of the members of the group. If the group does
     * not exist then <code>null</code> will be returned.
     *
     * @return
     */
    public List<String> getGroupMembers(String groupName)
    {
        List<String> members = null;
        NativeGroup group = _libC.getgrnam(groupName);

        if(group != null)
        {
            members = group.getMembers();
        }

        return members;
    }

    /**
     * Returns the real name (e.g. Joshua Kaplan) that corresponds with <code>
     * login</code>. If the login does not exist then <code>null</code>
     * will be returned. <code>null</code> may also be returned if the login
     * exists but there is no real name associated with it. This should not
     * occur on Brown CS Department machines, but it may when running cakehat
     * locally.
     *
     * @param login
     * @return
     */
    public String getRealName(String login)
    {
        String name = null;
        NativeUserInformation info = _libC.getpwnam(login);

        if(info != null)
        {
            name = info.getRealName();
        }

        return name;
    }

    /**
     * Returns whether the login exists.
     *
     * @param login
     * @return if login exists
     */
    public boolean isLogin(String login)
    {
        NativeUserInformation info = _libC.getpwnam(login);

        return (info != null);
    }

    /**
     * Returns the login of the user executing this code.
     *
     * @return login
     */
    public String getUserLogin()
    {
        String login = null;
        NativeUserInformation info = _libC.getpwuid(_libC.getuid());

        if(info != null)
        {
            login = info.getUserName();
        }

        return login;
    }
}