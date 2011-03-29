package utils.system;

import com.sun.jna.Structure;

/**
 * This class is for <strong>INTERNAL</strong> use only, it should never be
 * accessed from outside of this package. Due to limitations with JNA (Java
 * Native Access) this class must be public and all of its fields must be
 * public. Do not access the public fields of this class from outside of this
 * class.
 * <br/><br/>
 * This class stores the login records of users on the machine. Each terminal
 * counts as a login, so there is not a one to one correspondence between a
 * record existing and a user.
 * <br/><br/>
 * Information for this struct comes from the header files found on the
 * department machines. The basic definition of this structure can be
 * found at <code>/usr/include/utmpx.h</code> and the specifics are located at
 * <code>/usr/include/bits/utmpx.h</code>.
 * <br/><br/>
 * Note: This implementation will not work properly on OS X; however, there is
 * no need for this functionality when developing locally.
 * <br/><br/>
 * The fields of this class match the following native struct. (Implementation
 * note: native char is equivalent to a Java byte, Java char is actually an
 * unsigned short - this is done to support 16-bit Unicode.)
 * <pre>
 * struct utmpx
 * {
 *  short ut_type;       // Type of login
 *  int ut_pid;          // Process ID of login process
 *  char ut_line[32];    // Device name
 *  char ut_id[4];       // Inittab ID
 *  char ut_user[32];    // Username
 *  char ut_host[256];   // Hostname for remote login
 *
 *  short e_termination; // Process termination status (first field of ut_exit struct)
 *  short e_exit;        // Process exit status (second field of ut_exit struct)
 *
 *  int ut_session;      // Session ID, used for windowing
 *  int tv_sec;          // Time entry was made, seconds (first field of ut_tv struct)
 *  int tv_usec;         // Time entry was made, microseconds (second fields of ut_tv struct)
 *
 *  int ut_addr_v6[4];   // Internet address of remote host
 *  char __unused[20];   // Reserved for future use
 * }
 * </pre>
 *
 * @author jak2
 */
public class NativeUTMPX extends Structure
{
    /**
     * The type of record UTMPX structure represents.
     */
    public static enum UTMPXType
    {
        EMPTY(0),         //No valid user accounting information
        RUN_LVL(1),       //The system's runlevel.
        BOOT_TIME(2),     //Time of system boot.
        NEW_TIME(3),      //Time after system clock changed
        OLD_TIME(4),      //Time when system clock changed
        INIT_PROCESS(5),  //Process spawned by the init process.
        LOGIN_PROCESS(6), //Session leader of a logged in user.
        USER_PROCESS(7),  //Normal process
        DEAD_PROCESS(8),  //Terminated process
        ACCOUNTING(9);    //System accounting

        private final int _nativeValue;

        private UTMPXType(int value)
        {
            _nativeValue = value;
        }

        private static UTMPXType getFromNativeValue(int value)
        {
            UTMPXType matchingType = null;

            for(UTMPXType type : values())
            {
                if(type._nativeValue == value)
                {
                    matchingType = type;
                    break;
                }
            }

            return matchingType;
        }
    }

    // Fields of the struct - DO NOT REORDER (fields map directly to native memory)

    public short ut_type;
    public int ut_pid;

    //All of these byte arrays are arrays of characters that can be represented
    //as a String. They may have trailing whitespace that should be trimmed.
    public byte[] ut_line = new byte[32];
    public byte[] ut_id = new byte[4];
    public byte[] ut_user = new byte[32];
    public byte[] ut_host = new byte[256];

    //Fields of ut_exit
    public short e_termination;
    public short e_exit;

    public int ut_session;

    //Fields of ut_tv
    public int tv_sec;
    public int tv_usec;
    
    //The C struct for utmpx represents ut_addr_v6 as 4 unsigned 32-bit integers
    //However, Java's only unsigned primitive type is char, which is 16-bits
    //So instead of using 4 unsigned 32-bit integers, this uses 8 unsignd 16-bit
    //chars
    public char[] ut_addr_v6 = new char[8];

    //Space reserved for future space - do not bother reading whatever data is
    //put in here (if at all)
    public byte[] __unused = new byte[20];

    /**
     * The type of information this is representing.
     *
     * @return
     */
    public UTMPXType getType()
    {
        return UTMPXType.getFromNativeValue(ut_type);
    }

    /**
     * The process ID associated with this login.
     *
     * @return
     */
    public int getPID()
    {
        return ut_pid;
    }

    /**
     * Name of the device (commonly a name generated by a terminal).
     *
     * @return
     */
    public String getLine()
    {
        return new String(ut_line).trim();
    }

    /**
     * The login of the user represented by this entry.
     * <br/><br/>
     * Example: <code>jak2</code>
     *
     * @return
     */
    public String getUser()
    {
        return new String(ut_user).trim();
    }

    /**
     * The name of the host for this entry.
     * <br/><br/>
     * Local entry example: <code>localhost:12.0</code> or <code>:0</code>
     * Remote entry example: <code>incoming.cs.brown.edu</code>
     *
     * @return
     */
    public String getHost()
    {
        return new String(ut_host).trim();
    }

    /**
     * Whether the IP address associated with this entry is coming from outside
     * of this computer.
     *
     * @return
     */
    public boolean isRemoteIP()
    {
        boolean isRemote = false;

        //If there is no remote host then all values will be 0
        for(int block : ut_addr_v6)
        {
            if(block != 0)
            {
                isRemote = true;
                break;
            }
        }

        return isRemote;
    }

    /**
     * Returns the IP address associated with this entry formatted according to
     * the standard IPv6 convention. IPv6 consists of 128 bits, with each 16-bit
     * group represented in hexadecimal from highest bit to lowest bit.
     * <br/><br/>
     * Example: <code>2001:0db8:85a3:0000:0000:8a2e:0370:7334</code>
     * <br/><br/>
     * Note: When the IP scheme actually used is IPv4 (as is commonly the case
     * as of 2011), then all but the last four hexadecimal values will be 0.
     *
     * @return
     */
    public String getIPv6Address()
    {
        String str = "";

        //Print from highest order bit to lowest order bit, 16 bits as a time
        //interpreted as unsigned short values printed as hexadecimal
        for(int i = ut_addr_v6.length - 1; i >= 0; i--)
        {
            //Add as hex
            str += Integer.toString(ut_addr_v6[i], 16).toUpperCase();

            if(i != 0)
            {
                str += ":";
            }
        }

        return str;
    }
}