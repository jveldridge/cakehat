package gradesystem.services;

import java.io.File;
import java.util.List;
import support.utils.FileCopyingException;
import support.utils.FileSystemUtilities;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;

/**
 * A service that interacts with the file system. Implementations make use of
 * course specific information, such as the course's TA group. This is the
 * distinction between methods in FileSystemServices and FileSystemUtilities.
 *
 * @author jak2
 */
public interface FileSystemServices
{
    /**
     * Sets the correct permissions and group owner of <code>file</code>.
     * <br/><br/>
     * If <code>file</code> is a directory this will be applied recursively
     * for all files and directories inside of the directory.
     *
     * @param file
     * @throws ServicesException
     */
    public void sanitize(File file) throws ServicesException;

    /**
     * If the directory already exists no action is taken. If the directory does
     * not exist it is created, the permissions are set appropriately and the
     * TA group is set as the group owner. Any directories above the directory
     * to be created that do not exist are also created in the same manner.
     *
     * @param dir
     * @return directories created
     * @throws ServicesException
     */
    public List<File> makeDirectory(File dir) throws ServicesException;

    /**
     * This method is identical to
     * {@link FileSystemUtilities#copy(java.io.File, java.io.File, boolean, boolean, java.lang.String, utils.FileSystemUtilities.CopyFilePermissionMode)}
     * with the TA group set as the <code>groupOwner</code>.
     *
     * @param src
     * @param dst
     * @param overwrite
     * @param preserveDate
     * @param copyPermissions
     * @return
     * @throws FileCopyingException
     *
     * @see FileSystemUtilities#copy(java.io.File, java.io.File, boolean, boolean, java.lang.String, utils.FileSystemUtilities.FileCopyPermissions) 
     */
    public List<File> copy(File src, File dst, OverwriteMode overwrite,
            boolean preserveDate, FileCopyPermissions copyPermissions) throws FileCopyingException;
}