package support.utils.posix;

/**
 * This class is for <strong>INTERNAL</strong> use only, it should never be accessed from outside of this package. Due
 * to limitations with JNA (Java Native Access) this class must be public and all of its fields must be public. Do not
 * access the public fields of this class from outside of this class.
 * <br/><br/>
 * See <code>/usr/include/bits/stat.h</code> on a Brown CS Department machine.
 * <br/><br/>
 * After following through the type definitions, the structure is:
 * <pre>
 * struct stat
 * {
 *   long st_dev;              // Device.
 *   short __pad1;
 *   int st_ino;               // File serial number.
 *   int st_mode;              // File mode.
 *   int st_nlink;             // Link count.
 *   int st_uid;               // User ID of the file's owner.
 *   int st_gid;               // Group ID of the file's group.
 *   long st_rdev;             // Device number, if device.
 *   short __pad2;
 *   long st_size;             // Size of file, in bytes.
 *   int st_blksize;           // Optimal block size for I/O.
 *   int st_blocks;            // Number 512-byte blocks allocated.
 *   int __unused4;
 *   int st_atim_sec;          // Time of last access.
 *   int st_atim_nsec;         // Nscecs of last access.
 *   int st_mtim_sec;          // Time of last modification.
 *   int st_mtim_nsec;         // Nsecs of last modification.
 *   int st_ctim_sec;          // Time of last status change.
 *   int st_ctim_nsec;         // Nsecs of last status change.
 *   long __unused5;
 * }
 * </pre>
 * 
 * @author jak2
 */
public class LinuxFileStat extends NativeFileStat
{
    public long st_dev;
    public short __pad1;
    public int st_ino;
    public int st_mode;
    public int st_nlink;
    public int st_uid;
    public int st_gid;
    public long st_rdev;
    public short __pad2;
    public long st_size;
    public int st_blksize;
    public int st_blocks;
    public int __unused4;
    public int st_atim_sec;
    public int st_atim_nsec;
    public int st_mtim_sec;
    public int st_mtim_nsec;
    public int st_ctim_sec;
    public int st_ctim_nsec;
    public long __unused5;

    @Override
    public int getMode()
    {
        return st_mode;
    }

    @Override
    public long getFileSize()
    {
        return st_size;
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
        return st_mtim_sec;
    }

    @Override
    public long getLastAccessedSeconds()
    {
        return st_atim_sec;
    }

    @Override
    public long getLastStatusChangedSeconds()
    {
        return st_ctim_sec;
    }
}