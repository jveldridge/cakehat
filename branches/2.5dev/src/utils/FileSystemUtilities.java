package utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import utils.system.NativeException;

/**
 * Utility methods for interacting with the file system.
 *
 */
public interface FileSystemUtilities
{
    /**
     * *NIX file permissions. These are used instead of direct use of octal
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

        private int _value;

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
     * Returns a Calendar that represents the last modified date and time
     * of the file.
     *
     * @param file
     * @return last modified date
     */
    public Calendar getModifiedDate(File file);

    /**
     * Copies a file or a directory.
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
     * @param overwrite
     * @param preserveDate
     * @return all files and directories created during the copy
     * @throws IOException
     *
     * @see #copy(java.io.File, java.io.File)
     */
    public List<File> copy(File src, File dst, boolean overwrite,
            boolean preserveDate) throws FileCopyingException;

    /**
     * Equivalent to <code>copy(src, dst, false, false)</code>.
     *
     * @param src
     * @param dst
     * @return all files and directories created during the copy
     * @throws IOException
     *
     * @see #copy(java.io.File, java.io.File, boolean, boolean)
     */
    public List<File> copy(File src, File dst) throws FileCopyingException;

    /**
     * Deletes all files, throwing an informative exception if any of the files
     * cannot be deleted.
     *
     * @param files
     * @throws IOException
     */
    public void deleteFiles(List<File> files) throws IOException;

    /**
     * Reads a text file into a String.
     *
     * @param file the file to read
     * @return a String of the text in the file
     */
    public String readFile(File file) throws FileNotFoundException, IOException;

    /**
     * Changes the permissions on a file or directory. If changing the
     * permissions on a directory and <code>recursive</code> is true then the
     * permissions of the files and directories within the directory provided
     * will also have their permissions changed.
     * <br/><br/>
     * <b>NOTE: symlinks are not detected, therefore circular references are not
     * detected and as such this method is not guaranteed to terminate if a
     * directory with symlinks is provided and <code>recursive</code> is set
     * to true.</b>
     *
     * @param file
     * @param recursive
     * @param mode
     */
    public void chmod(File file, boolean recursive, Permission... mode) throws NativeException;

    /**
     * Equivalent to <code>chmodDefault(file, true)</code>.
     *
     * @param file
     */
    public void chmodDefault(File file) throws NativeException;

    /**
     * Changes permissions of a file to be readable and writable by the owner
     * and group. Changes the permissions of a directory to be readable,
     * writable, and accessible by the owner and group. All files are made
     * readable and writable by the owner and group. If <code>recursive</code>
     * is <code>true</coe>, then all subdirectories and files are given the same
     * permissions.
     * <br/><br/>
     * <b>NOTE: symlinks are not detected, therefore circular references are not
     * detected and as such this method is not guaranteed to terminate if the
     * directory provided or any of its subdirectories contain a symlink.</b>
     *
     * @param file
     */
    public void chmodDefault(File file, boolean recursive) throws NativeException;

    /**
     * Changes the specified file or directory's group. The user calling this
     * method must own the file or directory in order to succesfully change the
     * group.
     *
     * @param file
     * @param group the name of the group, such as cs000ta
     * @param recursive
     * @throws NativeException
     */
    public void changeGroup(File file, String group, boolean recursive) throws NativeException;

    /**
     *
     * Removes a directory and all of its files and subdirectories.
     *
     * @author jak2
     * @date 1/8/2010
     *
     * @param dirPath
     * @return success of deletion
     */
    public boolean removeDirectory(String dirPath);

    /**
     *
     * Removes a directory and all of its files and subdirectories.
     *
     * @author jak2
     * @date 1/8/2010
     *
     * @param dirPath
     * @return success of deletion
     */
    public boolean removeDirectory(File dirPath);

    /**
     * Returns all files in a directory, recursing into subdirectories, that
     * contain files with the specified extension.
     *
     * @param dirPath starting directory
     * @param extension the file extension, e.g. java or class
     * @return the files found with the specified extension
     */
    public Collection<File> getFiles(String dirPath, String extension);


    /**
     * Returns all files that satisfy the filter. If the file is a directory
     * the directory will be recursively searched to find all accepting files.
     *
     * @param file
     * @param filter
     * @return
     */
    public List<File> getFiles(File file, FileFilter filter);

    /**
     * Returns all files that satisfy the filter. If the file is a directory
     * the directory will be recursively searched to find all accepting files.
     * Sorts the files according to the comparator.
     *
     * @param file
     * @param filter
     * @param comparator
     * @return
     */
    public List<File> getFiles(File file, FileFilter filter, Comparator<File> comparator);
}