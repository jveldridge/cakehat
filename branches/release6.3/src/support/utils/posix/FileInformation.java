package support.utils.posix;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.Set;

/**
 * Information about a {@link File}. Unlike {@code File}'s information, this information is not automatically kept in
 * sync with the file system. It is only valid for the moment at which it is retrieved. If later more up to date
 * information is needed, retrieve another instance.
 *
 * @author jak2
 */
public class FileInformation
{
    //File this information references
    private final File _file;
    
    //Built from NativeFileStat info
    private final ImmutableSet<FilePermission> _permissions;
    private final FileType _type;
    private final int _groupId;
    private final int _userId;
    
    FileInformation(NativeFileStat stat, File file)
    {
        _permissions = buildPermissionsSet(stat);
        _type = determineType(stat);
        _groupId = stat.getGroupId();
        _userId = stat.getUserId();
        
        _file = file;
    }

    /**
     * The file this information is about.
     * 
     * @return
     */
    public File getFile()
    {
        return _file;
    }
    
    /******************************************************************************************************************\
    |*                                                   OWNERSHIP                                                    *|
    \******************************************************************************************************************/
    
    /**
     * The gid of the group that owns this file.
     * 
     * @return
     * @throws NativeException 
     */
    public int getGroupId()
    {   
        return _groupId;
    }

    /**
     * The uid of the user that owns this file.
     * 
     * @return
     * @throws NativeException 
     */
    public int getUserId()
    {
        return _userId;
    }

    /******************************************************************************************************************\
    |*                                                  PERMISSIONS                                                   *|
    \******************************************************************************************************************/

    private static ImmutableSet<FilePermission> buildPermissionsSet(NativeFileStat stat)
    {   
        int mode = stat.getMode();
        ImmutableSet.Builder<FilePermission> permissions = ImmutableSet.builder();
        for(FilePermission permission : FilePermission.values())
        {
            if((mode & permission.getValue()) != 0)
            {
                permissions.add(permission);
            }
        }

        return permissions.build();
    }
    
    /**
     * Returns an immutable set of the permissions for the file. 
     * 
     * @return 
     */
    public Set<FilePermission> getFilePermissions()
    {
        return _permissions;
    }

    /**
     * If allow read by owner.
     *
     * @return
     */
    public boolean isOwnerReadable()
    {
        return _permissions.contains(FilePermission.OWNER_READ);
    }

    /**
     * If allow read by group members.
     *
     * @return
     */
    public boolean isGroupReadable()
    {
        return _permissions.contains(FilePermission.GROUP_READ);
    }

    /**
     * If allow read by others.
     *
     * @return
     */
    public boolean isOthersReadable()
    {
        return _permissions.contains(FilePermission.OTHERS_READ);
    }

    /**
     * If allow write by owner.
     *
     * @return
     */
    public boolean isOwnerWritable()
    {
        return _permissions.contains(FilePermission.OWNER_WRITE);
    }

    /**
     * If allow write by group members.
     *
     * @return
     */
    public boolean isGroupWritable()
    {
        return _permissions.contains(FilePermission.GROUP_WRITE);
    }

    /**
     * If allow write by others.
     *
     * @return
     */
    public boolean isOthersWritable()
    {
        return _permissions.contains(FilePermission.OTHERS_WRITE);
    }

    /**
     * If allow executable by owner.
     *
     * @return
     */
    public boolean isOwnerExecutable()
    {
        return _permissions.contains(FilePermission.OWNER_EXECUTE);
    }

    /**
     * If allow executable by group members.
     *
     * @return
     */
    public boolean isGroupExecutable()
    {
        return _permissions.contains(FilePermission.GROUP_EXECUTE);
    }

    /**
     * If allow executable by others.
     *
     * @return
     */
    public boolean isOthersExecutable()
    {
        return _permissions.contains(FilePermission.OTHERS_EXECUTE);
    }

    /**
     * If the setuid bit is set.
     * <br/><br/>
     * From Wikipedia:<br/>
     * When a binary executable file has been given the setuid attribute, normal users on the system who have permission
     * to execute this file gain the privileges of the user who owns the file  within the created process. Setting this
     * bit on anything that is not an executable file has no meaning on most Linux and UNIX systems.
     *
     * @return
     */
    public boolean isSetUserIdUponExecution()
    {
        return _permissions.contains(FilePermission.SET_USER_ID_UPON_EXECUTION);
    }

    /**
     * If the setgid bit is set.
     * <br/><br/>
     * From Wikipedia:<br/>
     * Setting the setgid permission on a directory causes new files and subdirectories created within it to inherit its
     * groupID, rather than the primary groupID of the user who created the file (the ownerID is never affected, only
     * the groupID). Newly created subdirectories inherit the setgid bit.
     *
     * @return
     */
    public boolean isSetGroupIdUponExecution()
    {
        return _permissions.contains(FilePermission.SET_GROUP_ID_UPON_EXECUTION);
    }

    /**
     * If the sticky bit is set.
     * <br/><br/>
     * From Wikipedia:<br/>
     * The most common use of the sticky bit today is on directories. When the sticky bit is set, only the item's owner,
     * the directory's owner, or the superuser can rename or delete files. Without the sticky bit set, any user with
     * write and execute permissions for the directory can rename or delete contained files, regardless of owner.
     *
     * @return
     */
    public boolean isStickyBit()
    {
        return _permissions.contains(FilePermission.STICKY_BIT);
    }

    /******************************************************************************************************************\
    |*                                                    FILE TYPE                                                   *|
    \******************************************************************************************************************/

    /**
     * Bit mask for file type checks. 
     */
    private static final int S_IFMT = 0170000;
    
    private static FileType determineType(NativeFileStat stat)
    {
        int mode = stat.getMode();
        FileType matchingType = null;
        for(FileType type : FileType.values())
        {
            if((mode & S_IFMT) == type.getValue())
            {
                matchingType = type;
                break;
            }
        }
        
        return matchingType;
    }

    /**
     * The type of the file. If the file type is not a known type {@code null} will be returned.
     *
     * @return
     */
    public FileType getFileType()
    {
        return _type;
    }

    /**
     * If a regular file.
     *
     * @return
     */
    public boolean isRegularFile()
    {
        return (_type == FileType.REGULAR_FILE);
    }

    /**
     * If a directory.
     *
     * @return
     */
    public boolean isDirectory()
    {
        return (_type == FileType.DIRECTORY);
    }

    /**
     * If a symbolic link.
     * <br/><br/>
     * From Wikipedia:<br/>
     * A symbolic link is a special type of file that contains a reference to another file or directory in the form of
     * an absolute or relative path and that affects pathname resolution.
     *
     * @return
     */
    public boolean isSymbolicLink()
    {
        return (_type == FileType.SYMBOLIC_LINK);
    }

    /**
     * If a special type of file called a Unix domain socket.
     * <br/><br/>
     * From Wikipedia:<br/>
     * A Unix domain socket or IPC socket (inter-process communication socket) is a data communications endpoint for
     * exchanging data between processes executing within the same host operating system. While similar in functionality
     * to named pipes, Unix domain sockets may be created as byte streams or as datagram sequences, while pipes are byte
     * streams only.
     *
     * @return
     */
    public boolean isDomainSocket()
    {
        return (_type == FileType.DOMAIN_SOCKET);
    }

    /**
     * If a special type of file called a character special file.
     * <br/><br/>
     * From Wikipedia:<br/>
     * Character special files relate to devices through which the system transmits data one character at a time. These
     * device nodes often serve for stream communication with devices such as mice, keyboards, virtual terminals, and
     * serial modems, and usually do not support random access to data.
     *
     * @return
     */
    public boolean isCharacterSpecial()
    {
        return (_type == FileType.CHARACTER_SPECIAL);
    }

    /**
     * If a special type of file called a block special file.
     * <br/><br/>
     * From Wikipedia:<br/>
     * Block special files or block devices correspond to devices through which the system moves data in the form of
     * blocks. These device nodes often represent addressable devices such as hard disks, CD-ROM drives, or
     * memory-regions.
     *
     * @return
     */
    public boolean isBlockSpecial()
    {
        return (_type == FileType.BLOCK_SPECIAL);
    }

    /**
     * If a special type of file called a named piped.
     * <br/><br/>
     * From Wikipedia:<br/>
     * A named pipe (also known as a FIFO for its behavior) is an extension to the traditional pipe concept on Unix and
     * Unix-like systems, and is one of the methods of inter-process communication. A named pipe is system-persistent
     * and exists beyond the life of the process and must be deleted once it is no longer being used. Processes
     * generally attach to the named pipe (usually appearing as a file) to perform inter-process communication (IPC).
     *
     * @return
     */
    public boolean isNamedPipe()
    {
        return (_type == FileType.NAMED_PIPE);
    }
}