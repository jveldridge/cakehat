package support.utils.posix;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * Java wrappers for native *NIX POSIX C functions.
 * <br/><br/>
 * <strong>Note:</strong> Effort has been made to have this code behave the same on department Linux and OS X, but there
 * may be cases where the behavior is different. cakehat should be designed to work with the department Linux behavior.
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
        //Documentation: man 2 lstat64 (on OS X)
        public int lstat64(String filepath, OSXFileStat stat) throws LastErrorException;

        //Documentation: man 2 lstat64 (on Linux)
        //The function __lxstat64 is the function called by lstat, lstat is actually a macro.
        //For details on the mapping of the macro to this function, see the stat header file: /usr/include/sys/stat.h
        public int __lxstat64(int version, String filepath, LinuxFileStat stat) throws LastErrorException;
        
        //Per instructions in stat.h, version should always be 3 for this setup
        public static final int LINUX_LSTAT_VERSION = 3;
        
        //Documentation: man 2 chmod
        public int chmod(String filepath, int mode) throws LastErrorException;

        //Documentation: man 2 chown
        public int chown(String filepath, int uid, int gid) throws LastErrorException;

        //Documentation: man 3 getgrent
        //Getting information on a group with many members will generate a native error but the group information it
        //returns is completely accurate, therefore LastErrorException is not to be thrown from this function.
        public NativeGroup getgrnam(String group);

        //Documentation: man 3 getgrent
        //Getting information on a group with many members will generate a native error but the group information it
        //returns is completely accurate, therefore LastErrorException is not to be thrown from this function.
        public NativeGroup getgrgid(int gid);
        
        //Documentation: man 2 getgroups
        public int getgroups(int size, int[] list) throws LastErrorException;
        
        //Documentation: man 2 getgid
        //Per the man page, this function cannot fail. Therefore LastErrorException does not need to be thrown.
        public int getgid();
        
        //Documentation: man 3 getpwent
        public NativeUserInformation getpwnam(String login) throws LastErrorException;

        //Documentation: man 3 getpwent
        public NativeUserInformation getpwuid(int uid) throws LastErrorException;

        //Documentation: man 2 getuid
        //Per the man page, this function cannot fail. Therefore LastErrorException does not need to be thrown.
        public int getuid();

        //The following 3 *utxent methods only operate on Linux, they should not be called on OS X. This will not pose a
        //problem as they are used to determine if a user is remotely connected (such as over ssh), which should never
        //need to be determined when running as a developer on OS X.

        //Documentation: man 3 setutxent
        //(scroll down a bit, first part is about deprecated 32-bit UTMP API)
        //
        //Do not throw an exception as the department machines frequently raise an EACCES (permision denied) error while
        //still properly resetting the pointer to the beginning of the database
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
     * Returns native information about the filepath.
     *
     * @param filepath
     * @return
     * @throws NativeException if an underlying error is encountered, such as the filepath not existing
     */
    public NativeFileStat lstat(String filepath) throws NativeException
    {
        try
        {
            NativeFileStat stat;
            if(Platform.isLinux())
            {
                LinuxFileStat linuxStat = new LinuxFileStat();
                _libC.__lxstat64(LibC.LINUX_LSTAT_VERSION, filepath, linuxStat);
                stat = linuxStat;
            }
            else if(Platform.isMac())
            {
                OSXFileStat osXStat = new OSXFileStat();
                _libC.lstat64(filepath, osXStat);
                stat = osXStat;
            }
            else
            {
                throw new UnsupportedOperationException("Unsupported operating system");
            }
            
            return stat;
        }
        catch(LastErrorException e)
        {
            throw new NativeException(e, "Failure to retrieve file information for: " + filepath);
        }
    }

    /**
     * Changes the permissions for the file or directory specified by the {@code filepath}. The user
     * <strong>must</strong> be the owner of the file or directory specified by {@code filepath}.
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
            throw new NativeException(e, "Failure to change permissions to: " + Integer.toOctalString(mode) +
                    " (printed as octal), for: " + filepath);
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
            throw new NativeException(e, "Failure to change user id to: " + uid + ", and group id to: " + gid +
                    ", for: " + filepath);
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
            throw new NativeException("Unable to obtain information for group: " + group);
        }

        return groupInfo;
    }
    
    /**
     * Gets group information using the gid of the group.
     * 
     * @param gid the id of the group
     * @return group information
     * @throws NativeException 
     */
    public NativeGroup getgrgid(int gid) throws NativeException
    {
        NativeGroup groupInfo = _libC.getgrgid(gid);
        if(groupInfo == null)
        {
            throw new NativeException("Unable to obtain information for gid: " + gid);
        }

        return groupInfo;
    }
    
    /**
     * Fills the passed in array with the group ids for the groups the user is a member of and returns the total number
     * of groups the user is a member of. If {@code size} is 0 no group ids are inserted into {@code list}, but the
     * number of groups the user is a member of will be returned.
     * 
     * @param size the length of {@code list}
     * @param list the array that will be filled with the group ids the user is a member of
     * @return the number of groups the user belongs to
     * @throws NativeException 
     */
    public int getgroups(int size, int[] list) throws NativeException
    {
        try
        {
            return _libC.getgroups(size, list);
        }
        catch(LastErrorException e)
        {
            throw new NativeException(e, "Unable to obtain group ids for user");
        }
    }
    
    /**
     * Gets the group id belonging to the user calling this method. The underlying native function call is guaranteed to
     * succeed.
     *
     * @return
     */
    public int getgid()
    {
        return _libC.getgid();
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
        try
        {
            NativeUserInformation userInfo = _libC.getpwnam(login);
            if(userInfo == null)
            {
                throw new NativeException("Unable to obtain information for login: " + login);
            }

            return userInfo;
        }
        catch(LastErrorException e)
        {
            throw new NativeException(e, "Unable to obtain information for login: " + login);
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
        try
        {
            NativeUserInformation userInfo = _libC.getpwuid(uid);
            if(userInfo == null)
            {
                throw new NativeException("Unable to obtain information for user id: " + uid);
            }
            
            return userInfo;
        }
        catch(LastErrorException e)
        {
            throw new NativeException(e, "Unable to obtain information for user id: " + uid);
        }
    }

    /**
     * Gets the user id belonging to the user calling this method. The underlying native function call is guaranteed to
     * succeed.
     *
     * @return
     */
    public int getuid()
    {
        return _libC.getuid();
    }

    /**
     * Resets the pointer to the beginning of the user accounting database. This should be called before a series of
     * {@link #getutxent()} calls are made.
     * <br/><br/>
     * <strong>Only supported on Linux</strong>
     */
    public void setutxent()
    {
        _libC.setutxent();
    }

    /**
     * Reads an entry from the user accounting database. If there are no more entries in the database then
     * {@code null} will be returned.
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
            throw new NativeException(e, "Unable to retrieve entry from the user accounting database");
        }
    }

    /**
     * Closes the user accounting database. This should be called after a series of series of {@link #getutxent()} calls
     * have been made.
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
            throw new NativeException(e, "Unable to close user accounting database");
        }
    }
}