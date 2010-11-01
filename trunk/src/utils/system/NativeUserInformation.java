package utils.system;

import java.io.File;

import com.sun.jna.Structure;

/**
 * This class is for <strong>INTERNAL</strong> user only, it should never be
 * accessed from outside of this package. Due to limitations with JNA (Java
 * Native Access) this class must be public and all of its fields must be
 * public.
 * <br/><br/>
 * The fields of this class match the following native struct:
 * <br/><br/>
 * struct passwd {                             <br/>
 *     char   *pw_name;       // username      <br/>
 *     char   *pw_passwd;     // user password <br/>
 *     uid_t   pw_uid;        // user ID       <br/>
 *     gid_t   pw_gid;        // group ID      <br/>
 *     char   *pw_gecos;      // real name     <br/>
 *     char   *pw_dir;        // home directory<br/>
 *     char   *pw_shell;      // shell program <br/>
 * };
 * <br/><br/>
 * This class stores information related to a user, despite its native name
 * which would imply that it primarily dealt with password information.
 * <br/><br/>
 * Do not access the public fields of this class from outside of this class;
 * the fields must be public due to limitations of JNA (Java Native Access).
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
     *
     * @return
     */
    public String getRealName()
    {
    	return pw_gecos;
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