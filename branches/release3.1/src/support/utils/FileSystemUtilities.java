package support.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import support.utils.posix.NativeException;

/**
 * Utility methods for interacting with the file system.
 * <br/><br/>
 * <b>NOTE:</b> None of the methods in this class are capable of detecting
 * symbolic links. Therefore unwanted behavior may occur if symbolic links
 * are encountered. If code is traversing a directory structure and a symbolic
 * links points within that directory structure such that a cycle exists, then
 * the traversal will not terminate until it has overflowed the stack.
 */
public interface FileSystemUtilities
{
    /**
     * POSIX file permissions. These are used instead of direct use of octal
     * values. This is for two reasons:
     *  - There is no guaranteed way to ensure octal values are used, and
     *    0700 and 700 have completely different values. Mistakenly using
     *    the non-octal value could introduce a horrendous bug.
     *  - This form is more object oriented.
     */
    public static enum Permission
    {
        //All values are octal
        SET_USER_ID_ON_EXECUTION(04000), SET_GROUP_ID_ON_EXECUTION(02000), STICKY_BIT(01000),
        OWNER_READ(0400), OWNER_WRITE(0200), OWNER_EXECUTE(0100),
        GROUP_READ(0040), GROUP_WRITE(0020), GROUP_EXECUTE(0010),
        OTHERS_READ(0004), OTHERS_WRITE(0002), OTHERS_EXECUTE(0001);

        private final int _value;

        private Permission(int value)
        {
            _value = value;
        }

        public int getValue()
        {
            return _value;
        }
    }

    /**
     * The permissions that will be set on files that are created in a copy
     * operation.
     */
    public static enum FileCopyPermissions
    {
        /**
         * Copied files have read and write permission for both owner and group.
         */
        READ_WRITE,

        /**
         * Copied files have read, write, and execute permision for both owner
         * and group.
         */
        READ_WRITE_EXECUTE,

        /**
         * Copied files have read and write permission for both owner and group.
         * If the file being copied is executable for the user copying the file
         * then the file to be created will also have execute permission.
         */
        READ_WRITE_PRESERVE_EXECUTE;
    }

    /**
     * Different options for overwriting files when copying.
     */
    public static enum OverwriteMode
    {
        /**
         * If a file exists with the path that a file to be copied would have,
         * overwrite it with the file to be copied.
         */
        REPLACE_EXISTING,
        
        /**
         * If a file exists with the path that a file to be copied would have,
         * do not overwrite the file.
         */
        KEEP_EXISTING,

        /**
         * If a file exists with the path that a file to be copied would have,
         * abort copying by throwing an exception.
         */
        FAIL_ON_EXISTING;
    }

    /**
     * Returns a Calendar that represents the last modified date and time
     * of the file.
     *
     * @param file
     * @return last modified date
     */
    public Calendar getModifiedDate(File file);

    /**
     * Copies a file or a directory. All files or directories created in the
     * copy by will be group owned by <code>groupOwner</code>. Directories
     * will be readable, writable, and accessible by both owner and group. Files
     * will have their permissions set according to <code>copyPermissions</code>.
     * <br/><br/>
     * If <code>src</code> is a file and the directory it is being copied into
     * does not exist then that directory and any parent directories that need
     * to exist for the copy to occur will be created.
     * <br/><br/>
     * If <code>src</code> is a directory then it recursively copies all of its
     * contents into <code>dst</code>. Directories will be merged such that if
     * the destination directory or a directory in the destination directory
     * needs to be created and already exists, it will not be deleted.
     * <br/><br/>
     * If <code>overwrite</code> is <code>true</code> then files may be
     * overwritten in the copying process.
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
    public List<File> copy(File src, File dst, OverwriteMode overwrite,
            boolean preserveDate, String groupOwner,
            FileCopyPermissions copyPermissions) throws FileCopyingException;


    /**
     * Deletes a file or directory on exit.
     * 
     * @param file
     */
    public void deleteFileOnExit(File file);

    /**
     * Deletes the <code>files</code>. Attempts to delete all files, and will
     * continue even if unable to delete one or more files.
     *
     * @param files
     * @throws IOException If unable to delete one or more files. Documents all
     * files that could not be deleted.
     */
    public void deleteFiles(Iterable<File> files) throws IOException;

    /**
     * Creates a directory, recursively creating parent directories as
     * necessary. This is similar to {@link java.io.File#mkdirs()} but differs
     * in several important ways. Instead of returning a boolean to indicate
     * success, an exception is thrown if a directory cannot be created. All
     * directories created will have group owner set as <code>groupOwner</code>
     * and will be made readable, writable and accessible by both the owner and
     * group. All directories that are created are returned.
     *
     * @param dir the directory to create
     * @param groupOwner the group owner of the directory
     *
     * @return directories created
     * @throws IOException thrown if unable to create any of the necessary
     * directories in order for <code>dir</code> to exist
     */
    public List<File> makeDirectory(File dir, String groupOwner) throws IOException;

    /**
     * Reads a text file into a String.
     *
     * @param file the file to read
     * @return a String of the text in the file
     */
    public String readFile(File file) throws FileNotFoundException, IOException;

    /**
     * Changes the permissions on a file or directory. The user <b>must</b> be
     * the owner of the files and/or directories, if the user is not, the
     * native call will fail and a {@link NativeException} will be thrown.
     * <br/><br/>
     * If changing the permissions on a directory and <code>recursive</code> is
     * true then the permissions of the files and directories within the
     * directory provided will also have their permissions changed.
     *
     * @param file
     * @param recursive
     * @param mode
     *
     * @throws NativeException
     */
    public void chmod(File file, boolean recursive, Permission... mode) throws NativeException;

    /**
     * Changes permissions of a file to be readable and writable by the owner
     * and group. Changes the permissions of a directory to be readable,
     * writable, and accessible by the owner and group. All files are made
     * readable and writable by the owner and group. The user <b>must</b> be the
     * owner of the files and/or directories, if the user is not, the native
     * call will fail and a {@link NativeException} will be thrown.
     * <br/><br/>
     * If <code>recursive</code> is <code>true</code>, then all subdirectories
     * and files are given the same permissions.
     *
     * @param file
     * @param recursive
     *
     * @throws NativeException
     */
    public void chmodDefault(File file, boolean recursive) throws NativeException;

    /**
     * Changes the specified file or directory's group. The user calling this
     * method must own the file or directory in order to succesfully change the
     * group. If the user is not, the native call will fail and a {@link NativeE
     *
     * @param file
     * @param group the name of the group, such as cs000ta
     * @param recursive
     *
     * @throws NativeException
     */
    public void changeGroup(File file, String group, boolean recursive) throws NativeException;

    /**
     * Returns all files that satisfy the filter. If <code>file</code> is a
     * directory, the directory will be recursively searched to find all
     * accepting files.
     *
     * @param file
     * @param filter
     * @return
     * @throws IOException
     */
    public List<File> getFiles(File file, FileFilter filter) throws IOException;

    /**
     * Returns all files that satisfy the filter. If the file is a directory
     * the directory will be recursively searched to find all accepting files.
     * Sorts the files according to the comparator.
     * 
     * @param file
     * @param filter
     * @param comparator
     * @return
     * @throws IOException
     */
    public List<File> getFiles(File file, FileFilter filter, Comparator<File> comparator) throws IOException;
}