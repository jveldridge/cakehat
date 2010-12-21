package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collection;

/**
 * Utility methods for interacting with the file system.
 *
 */
public interface FileSystemUtilities
{
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
     * Makes a directory using the makeDirectoryHelper(...) method,
     * then changes the permissions to 770
     *
     * TODO: Check if changing the directory permission is working properly.
     *       In particular, check what it is considering the group (ugrad or TA group)
     *
     * @param dirPath directory to be created
     * @return whether the directory creation was successful
     */
    public boolean makeDirectory(String dirPath);

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