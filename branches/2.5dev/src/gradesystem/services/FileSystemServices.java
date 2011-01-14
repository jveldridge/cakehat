package gradesystem.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

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
     * @throws ServicesException
     */
    public void makeDirectory(File dir) throws ServicesException;

    /**
     * Copies a file or a directory and then sets the correct group owner for
     * all copied files and directories.
     * <br/><br/>
     * If <code>src</code> is a directory then it recursively copies all of its
     * contents into <code>dst</code>. Directories will be merged such that if
     * the destination directory or a directory in the destination directory
     * needs to be created and already exists, it will not be deleted. If
     * <code>overWrite</code> is <code>true</code> then files maybe overwritten
     * in the copying process.
     *
     * @param src
     * @param dst
     * @param overWrite
     * @param preserveDate
     * @return all files and directories created during the copy
     * @throws IOException
     *
     * @return all files and directories created
     *
     * @see #copy(java.io.File, java.io.File)
     */
    public List<File> copy(File src, File dst, boolean overWrite,
            boolean preserveDate) throws ServicesException;

    /**
     * Equivalent to <code>copy(src, dst, false, false)</code>.
     *
     * @param src
     * @param dst
     * @return all files and directories created during the copy
     * @throws IOException
     *
     * @return all files and directories created
     *
     * @see #copy(java.io.File, java.io.File, boolean, boolean)
     */
    public List<File> copy(File src, File dst) throws ServicesException;
}