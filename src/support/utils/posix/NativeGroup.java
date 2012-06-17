package support.utils.posix;

import com.google.common.collect.ImmutableSet;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Set;

/**
 * This class is for <strong>INTERNAL</strong> use only, it should never be accessed from outside of this package. Due
 * to limitations with JNA (Java Native Access) this class must be public and all of its fields must be public. Do not
 * access the public fields of this class from outside of this class.
 * <br/><br/>
 * The fields of this class match the following native struct:
 * <pre>
 * struct group
 * {
 *   char   *gr_name;       // group name
 *   char   *gr_passwd;     // group password
 *   gid_t   gr_gid;        // group ID
 *   char  **gr_mem;        // group members
 * }
 * </pre>
 * This class stores information pertaining to a POSIX *NIX group.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class NativeGroup extends Structure
{
    public String gr_name;
    public String gr_passwd;
    public int gr_gid;
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
     * Set of the group's members' logins.
     *
     * @return
     */
    public Set<String> getMembers()
    {
        ImmutableSet.Builder<String> members = ImmutableSet.builder();
        Pointer memberPtr;
        for(int i = 0; (memberPtr = gr_mem.getPointer(i)) != null; i += Pointer.SIZE)
        {
            members.add(memberPtr.getString(0));
        }

        return members.build();
    }
}