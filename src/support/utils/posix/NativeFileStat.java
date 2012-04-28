package support.utils.posix;

import com.sun.jna.Structure;

/**
 * The stat structure differs on Linux and OS X. As such, the actual fields are present in subclasses so that they may
 * properly interact with Java Native Access (JNA).
 *
 * @author jak2
 */
abstract class NativeFileStat extends Structure
{
    /**
     * The mode of the file. Stores file permission and type information.
     *
     * @return
     */
    public abstract int getMode();
    
    /**
     * The id of device upon which the file resides.
     * 
     * @return 
     */
    public abstract long getContainingDeviceId();
    
    /**
     * The id of the device for character and block device special files.
     * 
     * @return 
     */
    public abstract long getDeviceId();
    
    /**
     * The number of hard links to the file.
     * 
     * @return 
     */
    public abstract int getNumberOfHardLinks();
    
    /**
     * The inode (index-node) of the file.
     * 
     * @return 
     */
    public abstract long getInode();
    
    /**
     * The optimal file system I/O operation block size.
     * 
     * @return 
     */
    public abstract long getBlockSize();
    
    /**
     * The number of blocks allocated for the file.
     * 
     * @return 
     */
    public abstract long getNumberOfBlocks();

    /**
     * File size in bytes.
     * 
     * @return
     */
    public abstract long getFileSize();

    /**
     * Owner's user id (uid).
     *
     * @return
     */
    public abstract int getUserId();

    /**
     * Group owner's group id (gid).
     * 
     * @return
     */
    public abstract int getGroupId();

    /**
     * Time of last modification in seconds since the epoch.
     *
     * @return
     */
    public abstract long getLastModifiedSeconds();

    /**
     * Time of last access in seconds since the epoch.
     *
     * @return
     */
    public abstract long getLastAccessedSeconds();

    /**
     * Time of the last status change in seconds since the epoch. This can be updated by changes to file information
     * such as file permissions, while no changes to the file data itself have been made.
     * 
     * @return
     */
    public abstract long getLastStatusChangedSeconds();
}