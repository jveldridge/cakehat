package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;
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
     * Copies the source file to the destination file. If the destination file
     * does not exist it will be created. If it already exists, it will be
     * overwritten. If permissions do not allow this copy then it will fail
     * and false will be returned.
     *
     * @param sourceFile
     * @param destFile
     * @return success of copying file
     */
    public boolean copyFile(File sourceFile, File destFile);

    /**
     * Copies the source file to the destination file. If the destination file
     * does not exist it will be created. If it already exists, it will be
     * overwritten. If permissions do not allow this copy then it will fail
     * and false will be returned.
     *
     * @param sourcePath
     * @param destPath
     * @return success of copying file
     */
    public boolean copyFile(String sourcePath, String destPath);

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
     * Changes permissions of a file to be readable and writable by the owner
     * and group. Changes the permissions of a directory to be readable,
     * writable, and accessible by the owner and group. All subdirectories are
     * given the same permissions. All files are made readable and writable by
     * the owner and group.
     * <b>NOTE: symlinks are not detected, therefore circular references are not
     * detected and as such this method is not guaranteed to terminate if the
     * directory provided or any of its subdirectories contain a symlink.</b>
     *
     * @param file
     *
     * @see #chmod(java.io.File, boolean, utils.FileSystemUtilities.Permission[])
     */
    public void chmodDefault(File file) throws NativeException;

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
}