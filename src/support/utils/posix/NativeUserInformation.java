package support.utils.posix;

import java.io.File;
import com.sun.jna.Structure;

/**
 * This class is for <strong>INTERNAL</strong> use only, it should never be
 * accessed from outside of this package. Due to limitations with JNA (Java
 * Native Access) this class must be public and all of its fields must be
 * public. Do not access the public fields of this class from outside of this
 * class.
 * <br/><br/>
 * The fields of this class match the following native struct:
 * <pre>
 * struct passwd {
 *     char   *pw_name;       // username
 *     char   *pw_passwd;     // user password
 *     uid_t   pw_uid;        // user ID
 *     gid_t   pw_gid;        // group ID
 *     char   *pw_gecos;      // real name
 *     char   *pw_dir;        // home directory
 *     char   *pw_shell;      // shell program
 * };
 * </pre>
 * This class stores information related to a user, despite its native name
 * which would imply that it primarily dealt with password information.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class NativeUserInformation extends Structure
{
    /**
     * User name
     */
    public String pw_name;

    /**
     * User's password (encrypted)
     */
    public String pw_passwd;

    /**
     * Unique id for the user
     */
    public int pw_uid;

    /**
     * Unique group id of the user
     */
    public int pw_gid;

    /**
     * Real name of the user
     */
    public String pw_gecos;

    /**
     * Home directory of the user
     */
    public String pw_dir;

    /**
     * The default shell of the user
     */
    public String pw_shell;

    /**
     * The user's login.
     *
     * @return
     */
    public String getUserName()
    {
    	return pw_name;
    }

    /**
     * The user's encrypted password.
     *
     * @return
     */
    public String getEncryptedPassword()
    {
    	return pw_passwd;
    }

    /**
     * The user's unique id.
     *
     * @return
     */
    public int getUserId()
    {
    	return pw_uid;
    }

    /**
     * The user's group's unique id.
     *
     * @return
     */
    public int getGroupId()
    {
    	return pw_gid;
    }

    /**
     * The user's real name.
     * <br/><br/>
     * Note: pw_gecos should contain the name; however, on OS X this information
     * is actually stored in the pw_shell variable.
     *
     * @return
     */
    public String getRealName()
    {
        String name = pw_gecos;
        if(name == null)
        {
            name = pw_shell;
        }

    	return name;
    }

    /**
     * The user's home directory.
     *
     * @return
     */
    public File getHomeDirectory()
    {
    	return new File(pw_dir);
    }

    /**
     * The user's default shell.
     *
     * @return
     */
    public String getDefaultShell()
    {
    	return pw_shell;
    }
}