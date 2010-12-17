package gradesystem.utils.system;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * This class is for <strong>INTERNAL</strong> user only, it should never be
 * accessed from outside of this package. Due to limitations with JNA (Java
 * Native Access) this class must be public and all of its fields must be
 * public.
 * <br/><br/>
 * The fields of this class match the following native struct:
 * <br/><br/>
 *  struct group {                              <br/>
 *      char   *gr_name;       // group name    <br/>
 *      char   *gr_passwd;     // group password<br/>
 *      gid_t   gr_gid;        // group ID      <br/>
 *      char  **gr_mem;        // group members <br/>
 *  };
 * <br/><br/>
 * This class stores information pertaining to a UNIX group.
 * <br/><br/>
 * Do not access the public fields of this class from outside of this class;
 * the fields must be public due to limitations of JNA (Java Native Access).
 *
 * @author jak2 (Joshua Kaplan)
 */
public class NativeGroup extends Structure
{
    /**
     * Name of the group.
     */
    public String gr_name;

    /**
     * The group's password (encrypted).
     */
    public String gr_passwd;

    /**
     * The unique id for the group.
     */
    public int gr_gid;

    /**
     * Pointer into native memory of the list of members.
     */
    public Pointer gr_mem;

    /**
     * The name of the group.
     *
     * @return
     */
    public String getName()
    {
        return gr_name;
    }

    /**
     * The group's password (encrypted).
     *
     * @return
     */
    public String getPassword()
    {
        return gr_passwd;
    }

    /**
     * Unique identifier for the group.
     *
     * @return
     */
    public int getGID()
    {
        return gr_gid;
    }

    /**
     * List of the group's members' logins.
     *
     * @return
     */
    public List<String> getMembers()
    {
        ArrayList<String> members = new ArrayList<String>();

        Pointer memberPtr;
        for(int i = 0; (memberPtr = gr_mem.getPointer(i)) != null; i += Pointer.SIZE)
        {
        	members.add(memberPtr.getString(0));
        }

        return members;
    }
}