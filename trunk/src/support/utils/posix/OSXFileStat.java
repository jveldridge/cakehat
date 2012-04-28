package support.utils.posix;

/**
 * This class is for <strong>INTERNAL</strong> use only, it should never be accessed from outside of this package. Due
 * to limitations with JNA (Java Native Access) this class must be public and all of its fields must be public. Do not
 * access the public fields of this class from outside of this class.
 * <br/><br/>
 * See <code>/usr/include/sys/stat.h</code> on a computer running OS X or consult
 * http://developer.apple.com/library/mac/#documentation/Darwin/Reference/ManPages/man2/stat.2.html
 * <br/><br/>
 * This is a structure for a the 64bit version of the stat struct. It is defined in the above mentioned file and has the
 * defined name <code>__DARWIN_STRUCT_STAT64</code>.
 * <br/><br/>
 * After following through the type definitions, the structure is:
 * <pre>
 * struct stat
 * {
 *   int    st_dev;            // [XSI] ID of device containing file
 *   short  st_mode;           // [XSI] Mode of file (see below)
 *   short  st_nlink;          // [XSI] Number of hard links
 *   long   st_ino;            // [XSI] File serial number
 *   int    st_uid;            // [XSI] User ID of the file
 *   int    st_gid;            // [XSI] Group ID of the file
 *   int    st_rdev;           // [XSI] Device ID
 *   long   st_atime;          // [XSI] Time of last access
 *   long   st_atimensec;      // nsec of last access
 *   long   st_mtime;          // [XSI] Last data modification time
 *   long   st_mtimensec;      // last data modification nsec
 *   long   st_ctime;          // [XSI] Time of last status change
 *   long   st_ctimensec;      // nsec of last status change
 *   long   st_birthtime;      //  File creation time(birth)
 *   long   st_birthtimensec;  // nsec of File creation time
 *   long   st_size;           // [XSI] file size, in bytes
 *   long   st_blocks;         // [XSI] blocks allocated for file
 *   int    st_blksize;        // [XSI] optimal blocksize for I/O
 *   int    st_flags;          // user defined flags for file
 *   int    st_gen;            // file generation number
 *   int    st_lspare;         // RESERVED: DO NOT USE!
 *   long   st_qspare[2];      // RESERVED: DO NOT USE!
 * }
 * </pre>
 * 
 * @author jak2
 */
public class OSXFileStat extends NativeFileStat
{
    public int st_dev;
    public short st_mode;
    public short st_nlink;
    public long st_ino;
    public int st_uid;
    public int st_gid;
    public int st_rdev;
    public long st_atime;
    public long st_atimensec;
    public long st_mtime;
    public long st_mtimensec;
    public long st_ctime;
    public long st_ctimensec;
    public long st_birthtime;
    public long st_birthtimensec;
    public long st_size;
    public long st_blocks;
    public int st_blksize;
    public int st_flags;
    public int st_gen;
    public int st_lspare;
    public long[] st_qspare = new long[2];

    @Override
    public int getMode()
    {
        return st_mode;
    }
    
    @Override
    public long getContainingDeviceId()
    {
        return st_dev;
    }
    
    @Override
    public long getDeviceId()
    {
        return st_rdev;
    }
    
    @Override
    public int getNumberOfHardLinks()
    {
        return st_nlink;
    }
    
    @Override
    public long getInode()
    {
        return st_ino;
    }
    
    @Override
    public long getBlockSize()
    {
        return st_blksize;
    }
    
    @Override
    public long getNumberOfBlocks()
    {
        return st_blocks;
    }

    @Override
    public long getFileSize()
    {
        return st_size;
    }

    @Override
    public int getUserId()
    {
        return st_uid;
    }

    @Override
    public int getGroupId()
    {
        return st_gid;
    }

    @Override
    public long getLastModifiedSeconds()
    {
        return st_mtime;
    }

    @Override
    public long getLastAccessedSeconds()
    {
        return st_atime;
    }

    @Override
    public long getLastStatusChangedSeconds()
    {
        return st_ctime;
    }
}