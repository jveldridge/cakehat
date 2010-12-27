package utils.system;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * Java wrappers for native C functions in Linux/Unix.
 * <br/><br/>
 * <strong>Note:</strong> This code will not run properly if the underlying
 * native function library does not exist, so there may be issues of running
 * this code on non-department machines.
 * 
 * @author jak2
 */
class LibCWrapper
{
    /**
     * This interface matches the native functions of the LibC library. To see
     * more information about any of these functions, consult the man pages.
     * For instance to see information on 'getuid' then in a terminal the
     * command would be 'man getuid'.
     */
    private static interface LibC extends Library
    {
        public int chmod(String filepath, int mode) throws LastErrorException;
        public NativeGroup getgrnam(String group) throws LastErrorException;
        public NativeUserInformation getpwnam(String login) throws LastErrorException;
        public NativeUserInformation getpwuid(int uid) throws LastErrorException;
        public int getuid(); //Per man page, this function cannot fail
    }

    private final LibC _libC;

    public LibCWrapper()
    {
        _libC = (LibC) Native.loadLibrary("c", LibC.class);
    }

    /**
     * Changes the permissions for the file or directory specified by the
     * <code>filepath</code>.
     *
     * @param filepath
     * @param mode
     * @throws NativeException if an error occurs in native code
     */
    public void chmod(String filepath, int mode) throws NativeException
    {
        try
        {
            _libC.chmod(filepath, mode);
        }
        catch(LastErrorException e)
        {
            String errorMsg = "Failure to change permissions to: " + mode +
                              " (in octal), for: " + filepath;
            throw new NativeException(e, errorMsg);
        }
    }

    /**
     * Gets group information using the name of the group.
     *
     * @param group name of the group
     * @return group information
     * @throws NativeException thrown if the group does not exist or an error
     * occurs in native code
     */
    public NativeGroup getgrnam(String group) throws NativeException
    {
        String errorMsg = "Unable to obtain information for group: " + group;
        try
        {
            NativeGroup groupInfo = _libC.getgrnam(group);
            if(groupInfo == null)
            {
                throw new NativeException(errorMsg);
            }

            return groupInfo;
        }
        catch(LastErrorException e)
        {
            throw new NativeException(e, errorMsg);
        }
    }

    /**
     * Gets user information using the login name.
     *
     * @param login
     * @return user information
     * @throws NativeException thrown if the login does not exist or an error
     * occurs in native code
     */
    public NativeUserInformation getpwnam(String login) throws NativeException
    {
        String errorMsg = "Unable to obtain information for login: " + login;
        try
        {
            NativeUserInformation userInfo = _libC.getpwnam(login);
            if(userInfo == null)
            {
                throw new NativeException(errorMsg);
            }

            return userInfo;
        }
        catch(LastErrorException e)
        {
            throw new NativeException(e, errorMsg);
        }
    }

    /**
     * Gets user information using the user id.
     *
     * @param uid
     * @return user information
     * @throws NativeException thrown if the uid is not valid or an error
     * occurs in native code
     */
    public NativeUserInformation getpwuid(int uid) throws NativeException
    {
        String errorMsg = "Unable to obtain information for user id: " + uid;
        try
        {
            NativeUserInformation userInfo = _libC.getpwuid(uid);
            if(userInfo == null)
            {
                throw new NativeException(errorMsg);
            }
            
            return userInfo;
        }
        catch(LastErrorException e)
        {
            throw new NativeException(e, errorMsg);
        }
    }

    /**
     * Gets the user id belonging to the user calling this method. The
     * underlying native functionc call is guaranteed to succeed.
     *
     * @return
     */
    public int getuid()
    {
        return _libC.getuid();
    }
}