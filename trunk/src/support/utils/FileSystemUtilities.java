package support.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import support.utils.posix.FilePermission;
import support.utils.posix.NativeException;

/**
 * Utility methods for interacting with the file system.
 * <br/><br/>
 * <b>NOTE:</b> None of the methods in this class are capable of detecting symbolic links. Therefore unwanted behavior
 * may occur if symbolic links are encountered. If code is traversing a directory structure and a symbolic links points
 * within that directory structure such that a cycle exists, then the traversal will not terminate until it has
 * overflowed the stack.
 */
public interface FileSystemUtilities
{
    /**
     * The permissions that will be set on files that are created in a copy operation.
     */
    public static enum FileCopyPermissions
    {
        /**
         * Copied files have read and write permission for both owner and group.
         */
        READ_WRITE,

        /**
         * Copied files have read, write, and execute permission for both owner and group.
         */
        READ_WRITE_EXECUTE,

        /**
         * Copied files have read and write permission for both owner and group. If the file being copied is executable
         * for the user copying the file then the file to be created will also have execute permission.
         */
        READ_WRITE_PRESERVE_EXECUTE;
    }

    /**
     * Different options for overwriting files when copying.
     */
    public static enum OverwriteMode
    {
        /**
         * If a file exists with the path that a file to be copied would have, overwrite it with the file to be copied.
         */
        REPLACE_EXISTING,
        
        /**
         * If a file exists with the path that a file to be copied would have, do not overwrite the file.
         */
        KEEP_EXISTING,

        /**
         * If a file exists with the path that a file to be copied would have, abort copying by throwing an exception.
         */
        FAIL_ON_EXISTING;
    }

    /**
     * Creates an empty file in the default temporary-file directory, using the given prefix and suffix to generate its
     * name. Deletion of the file will be attempted on JVM shutdown.  This is accomplished via the built-in
     * {@link File#createTempFile(java.lang.String, java.lang.String)} and {@link File#deleteOnExit()} methods.
     * 
     * @return 
     */
    public File createTempFile(String prefix, String suffix) throws IOException;
    
    /**
     * Creates a new empty file in the specified directory, using the given prefix and suffix strings to generate its
     * name. Deletion of the file will be attempted on JVM shutdown.  This is accomplished via the built-in
     * {@link File#createTempFile(java.lang.String, java.lang.String, java.io.File)} and {@link File#deleteOnExit()}
     * methods.
     * 
     * @return 
     */
    public File createTempFile(String prefix, String suffix, File directory) throws IOException;

    /**
     * Copies a file or a directory. All files or directories created in the copy by will be group owned by
     * {@code groupOwner}. Directories will be readable, writable, and accessible by both owner and group. Files
     * will have their permissions set according to {@code copyPermissions}.
     * <br/><br/>
     * If {@code src} is a file and the directory it is being copied into does not exist then that directory and any
     * parent directories that need to exist for the copy to occur will be created.
     * <br/><br/>
     * If {@code src} is a directory then it recursively copies all of its contents into {@code dst}. Directories will
     * be merged such that if the destination directory or a directory in the destination directory needs to be created
     * and already exists, it will not be deleted.
     * <br/><br/>
     * If {@code overwrite} is {@code true} then files may be overwritten in the copying process.
     *
     * @param src
     * @param dst
     * @param overwrite
     * @param preserveDate
     * @param groupOwner
     * @param copyPermissions
     *
     * @return all files and directories created during the copy
     * @throws FileCopyingException
     */
    public Set<File> copy(File src, File dst, OverwriteMode overwrite, boolean preserveDate, String groupOwner,
            FileCopyPermissions copyPermissions) throws FileCopyingException;

    /**
     * Runs {@link #deleteFiles(java.lang.Iterable)} during JVM shutdown. Failure will occur silently.
     * 
     * @param file
     */
    public void deleteFilesOnExit(Iterable<File> file);

    /**
     * Deletes the {@code files}. Attempts to delete all files, and will continue even if unable to delete one or more
     * files.
     *
     * @param files
     * @throws FileDeletingException if unable to delete one or more files
     */
    public void deleteFiles(Iterable<File> files) throws FileDeletingException;
    
    /**
     * Deletes the {@code files}. Attempts to delete all files, and will continue even if unable to delete one or more
     * files. Failures are handled silently. Failures can occur in cases where due to NFS (network file system)
     * temp files can be created during the deletion process and these temp files cannot be deleted.
     * 
     * @param files 
     */
    public void deleteFilesSilently(Iterable<File> files);

    /**
     * Creates a directory, recursively creating parent directories as necessary. This is similar to
     * {@link java.io.File#mkdirs()} but differs in several important ways. Instead of returning a boolean to indicate
     * success, an exception is thrown if a directory cannot be created. All directories created will have group owner
     * set as {@code groupOwner} and will be made readable, writable and accessible by both the owner and group. All
     * directories that are created are returned.
     *
     * @param dir the directory to create
     * @param groupOwner the group owner of the directory
     *
     * @return directories created
     * @throws DirectoryCreationException if unable to create any of the necessary directories in order for {@code dir}
     * to exist
     */
    public Set<File> makeDirectory(File dir, String groupOwner) throws DirectoryCreationException;

    /**
     * Reads a text file into a String.
     *
     * @param file the file to read
     * @return a String of the text in the file
     */
    public String readFile(File file) throws FileNotFoundException, IOException;

    /**
     * Changes the permissions on a file or directory. The user <b>must</b> be the owner of the files and/or
     * directories, if the user is not, the native call will fail and a {@link IOException} will be thrown.
     *
     * @param file
     * @param mode
     *
     * @throws IOException
     */
    public void chmod(File file, Set<FilePermission> mode) throws IOException;

    /**
     * Changes the specified file or directory's group. The user calling this method must own the file or directory in
     * order to successfully change the group. If the user is not, the native call will fail and an {@link IOException}
     * will be thrown.
     *
     * @param file
     * @param group the name of the group, such as cs000ta
     *
     * @throws NativeException
     */
    public void changeGroup(File file, String group) throws IOException;

    /**
     * Returns all files that satisfy the filter. If {@code file} is a directory, the directory will be recursively
     * searched to find all accepting files.
     *
     * @param file
     * @param filter
     * @return
     * @throws FileAccessException
     */
    public Set<File> getFiles(File file, FileFilter filter) throws FilePermissionException;

    /**
     * Returns all files that satisfy the filter. If the file is a directory the directory will be recursively searched
     * to find all accepting files. Sorts the files according to the comparator.
     * 
     * @param file
     * @param filter
     * @param comparator
     * @return
     * @throws FileAccessException
     */
    public List<File> getFiles(File file, FileFilter filter, Comparator<File> comparator) throws FilePermissionException;
}