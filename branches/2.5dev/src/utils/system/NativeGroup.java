package utils.system;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.Pointer;
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
 *  struct group {
 *      char   *gr_name;       // group name
 *      char   *gr_passwd;     // group password
 *      gid_t   gr_gid;        // group ID
 *      char  **gr_mem;        // group members
 *  };
 * </pre>
 * This class stores information pertaining to a UNIX group.
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

    private List<String> _members;
    /**
     * List of the group's members' logins.
     *
     * @return
     */
    public List<String> getMembers()
    {
        if(_members == null)
        {
            ImmutableList.Builder<String> builder = ImmutableList.builder();

            Pointer memberPtr;
            for(int i = 0; (memberPtr = gr_mem.getPointer(i)) != null; i += Pointer.SIZE)
            {
                builder.add(memberPtr.getString(0));
            }

            _members = builder.build();
        }

        return _members;
    }
}