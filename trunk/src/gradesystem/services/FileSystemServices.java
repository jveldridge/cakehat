package gradesystem.services;

import java.io.File;
import utils.system.NativeException;

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
     *
     * If <code>file</code> is a directory this will be applied recursively
     * for all files and directories inside of the directory.
     *
     * @param file
     */
    public void sanitize(File file) throws NativeException;

    /**
     * If the directory already exists no action is taken. If the directory does
     * not exist it is created, the permissions are set appropriately and the
     * TA group is set as the group owner. Any directories above the directory
     * to be created that do not exist are also created in the same manner.
     *
     * @param dir
     * @throws NativeException
     */
    public void makeDirectory(File dir) throws NativeException;
}