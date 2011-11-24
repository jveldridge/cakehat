package support.utils.posix;

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
     */
    private static interface LibC extends Library
    {
        //Documentation: man 2 chmod
        public int chmod(String filepath, int mode) throws LastErrorException;

        //Documentation: man 2 chown
        public int chown(String filepath, int uid, int gid) throws LastErrorException;

        //Documentation: man 3 getgrent
        //Getting information on a group with many members will generate a native
        //error but the group information it returns is completely accurate,
        //therefore LastErrorException is not to be thrown from this function.
        public NativeGroup getgrnam(String group);

        //Documentation: man 3 getpwent
        public NativeUserInformation getpwnam(String login) throws LastErrorException;

        //Documentation: man 3 getpwent
        public NativeUserInformation getpwuid(int uid) throws LastErrorException;

        //Documentation: man 2 getuid
        //Per the man page, this function cannot fail. Therefore
        //LastErrorException does not need to be thrown.
        public int getuid();

        //The following 3 *utxent methods only operate on Linux, they should not
        //be called on OS X. This will not pose a problem as they are used to
        //determine if a user is remotely connected (such as over ssh), which
        //should never need to be determined when running as a developer on OS X.

        //Documentation: man 3 setutxent
        //(scroll down a bit, first part is about deprecated 32-bit UTMP API)
        //
        //Do not throw an exception as the department machines frequently raise
        //an EACCES (permision denied) error while still properly resetting
        //the pointer to the beginning of the database
        public void setutxent();

        //Documentation: man 3 getutxent
        //(scroll down a bit, first part is about deprecated 32-bit UTMP API)
        public NativeUTMPX getutxent() throws LastErrorException;

        //Documentation: man 3 endutxent
        //(scroll down a bit, first part is about deprecated 32-bit UTMP API)
        public void endutxent() throws LastErrorException;
    }

    private final LibC _libC;

    public LibCWrapper()
    {
        _libC = (LibC) Native.loadLibrary("c", LibC.class);
    }

    /**
     * Changes the permissions for the file or directory specified by the
     * <code>filepath</code>. The user <strong>must</strong> be the owner of
     * the file or directory specified by <code>filepath</code>.
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
            String errorMsg = "Failure to change permissions to: " +
                               Integer.toOctalString(mode) + " " +
                               "(printed as octal), for: " + filepath;
            throw new NativeException(e, errorMsg);
        }
    }

    /**
     * Changes the user owner and group owner of the specified file. For this
     * to succeed the file or directory specified by <code>filepath</code> must
     * be owned by the user.
     *
     * @param filepath
     * @param uid user id; to remain unchanged use -1
     * @param gid group id; to remain unchanged use -1
     * @throws NativeException
     */
    public void chown(String filepath, int uid, int gid) throws NativeException
    {
        try
        {
            _libC.chown(filepath, uid, gid);
        }
        catch(LastErrorException e)
        {
            String errorMsg = "Failure to change user id to: " + uid +
                              ", and group id to: " + gid + ", for: " + filepath;
            throw new NativeException(e, errorMsg);
        }
    }

    /**
     * Gets group information using the name of the group.
     *
     * @param group name of the group
     * @return group information
     * @throws NativeException thrown if the group does not exist
     */
    public NativeGroup getgrnam(String group) throws NativeException
    {
        NativeGroup groupInfo = _libC.getgrnam(group);
        if(groupInfo == null)
        {
            String errorMsg = "Unable to obtain information for group: " + group;
            throw new NativeException(errorMsg);
        }

        return groupInfo;
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

    /**
     * Resets the pointer to the beginning of the user accounting database. This
     * should be called before a series of {@link #getutxent()} calls are made.
     * <br/><br/>
     * <strong>Only supported on Linux</strong>
     */
    public void setutxent()
    {
        _libC.setutxent();
    }

    /**
     * Reads an entry from the user accounting database. If there are no more
     * entries in the database then <code>null</code> will be returned.
     * <br/><br/>
     * <strong>Only supported on Linux</strong>
     *
     * @see #setutxent()
     * @see #endutxent()
     *
     * @return
     * @throws NativeException
     */
    public NativeUTMPX getutxent() throws NativeException
    {
        try
        {
            return _libC.getutxent();
        }
        catch(LastErrorException e)
        {
            String errorMsg = "Unable to retrieve entry from the user " +
                    "accounting database";
            throw new NativeException(e, errorMsg);
        }
    }

    /**
     * Closes the user accounting database. This should be called after a
     * series of series of {@link #getutxent()} calls have been made.
     * <br/><br/>
     * <strong>Only supported on Linux</strong>
     *
     * @throws NativeException
     */
    public void endutxent() throws NativeException
    {
        try
        {
            _libC.endutxent();
        }
        catch(LastErrorException e)
        {
            String errorMsg = "Unable to close user accounting database";
            throw new NativeException(e, errorMsg);
        }
    }
}