package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Utility methods for interacting with the file system.
 *
 */
public class FileSystemUtilities
{
    /**
     * Returns a Calendar that represents the last modified date and time
     * of the file specified by the file path.
     *
     * @param filePath
     * @return last modified date
     */
    public Calendar getModifiedDate(String filePath)
    {
        return getModifiedDate(new File(filePath));
    }

    /**
     * Returns a Calendar that represents the last modified date and time
     * of the file.
     *
     * @param file
     * @return last modified date
     */
    public Calendar getModifiedDate(File file)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(file.lastModified());

        return calendar;
    }

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
    public boolean copyFile(File sourceFile, File destFile)
    {
        if(!sourceFile.exists())
        {
            return false;
        }
        try
        {
             if(!destFile.exists())
             {
                destFile.createNewFile();
             }

             FileChannel source = null;
             FileChannel destination = null;
             try
             {
                destination = new FileOutputStream(destFile).getChannel();
                source = new FileInputStream(sourceFile).getChannel();
                destination.transferFrom(source, 0, source.size());
             }
             finally
             {
                 if(source != null)
                 {
                    source.close();
                 }
                 if(destination != null)
                 {
                    destination.close();
                 }
            }
        }
        catch(IOException e)
        {
            return false;
        }

        return true;
    }

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
    public boolean copyFile(String sourcePath, String destPath)
    {
         return copyFile(new File(sourcePath), new File(destPath));
    }

    /**
     * Copies the source file to the destination file. If the destination file
     * does not exist it will be created. If it already exists, it will be
     * overwritten. If permissions do not allow this copy then it will fail
     * and false will be returned.
     *
     * @param sourceFile
     * @param destPath
     * @return success of copying file
     */
    public boolean copyFile(File sourceFile, String destPath)
    {
        return copyFile(sourceFile, new File(destPath));
    }

    /**
     * Reads a text file into a String.
     *
     * @param the file to read
     * @return a String of the text in the file
     */
    public String readFile(File file)
    {
        StringBuilder text = new StringBuilder();
        try
        {
            BufferedReader input = new BufferedReader(new FileReader(file));
            try
            {
                String line = null;
                while ((line = input.readLine()) != null)
                {
                    text.append(line);
                    text.append(System.getProperty("line.separator"));
                }
            }
            finally
            {
                input.close();
            }
        }
        catch (IOException ex)
        {
            new ErrorView(ex, "Unable to read text of: " + file.getAbsolutePath());
        }
        
        return text.toString();
    }


        /**
     * Makes a directory using the makeDirectoryHelper(...) method,
     * then changes the permissions to 770
     *
     * TODO: Check if changing the directory permission is working properly.
     *       In particular, check what it is considering the group (ugrad or TA group)
     *
     * @param dirPath- directory to be created
     * @return whether the directory creation was successful
     */
    public boolean makeDirectory(String dirPath)
    {
        //Make directory if necessary
        boolean madeDir = false;

        File dir = new File(dirPath);
        if(!dir.exists())
        {
            madeDir = dir.mkdirs();
        }

        //If directory was made, change permissions to be totally accessible by user and group
        if(madeDir)
        {
            BashConsole.write("chmod 770 -R" + dirPath);
        }

        //Return if the directory now exists
        return dir.exists();
    }
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
    public boolean removeDirectory(String dirPath)
    {
        return removeDirectory(new File(dirPath));
    }

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
    public boolean removeDirectory(File dirPath)
    {
        //only proceed if directory exists
        if(dirPath.exists() && dirPath.isDirectory())
        {
            //for each directory and file in directory
            for(File file : dirPath.listFiles())
            {
                //if directory, recursively delete files and directories inside
                if(file.isDirectory())
                {
                    removeDirectory(file);
                }
                //if file, just delete the file
                else
                {
                    file.delete();
                }
            }

            //return success of deleting directory
            return dirPath.delete();
        }

        //if the directory didn't exist, report failure
        return false;
    }

    /**
     * Returns all files in a directory, recursing into subdirectories, that
     * contain files with the specified extension.
     *
     * @param dirPath starting directory
     * @param extension the file extension, e.g. java or class
     * @return the files found with the specified extension
     */
    public Collection<File> getFiles(String dirPath, String extension)
    {
        Vector<File> files = new Vector<File>();

        File dir = new File(dirPath);
        if (dir == null || !dir.exists())
        {
            return files;
        }
        for (String name : dir.list())
        {
            File entry = new File(dir.getAbsolutePath() + "/" + name);
            //If it is a directory, recursively explore and add files ending with the extension
            if (entry.isDirectory())
            {
                files.addAll(getFiles(entry.getAbsolutePath(), extension));
            }
            //Add if this entry is a file ending with the extension and not a hidden file
            if (entry.isFile() && name.endsWith("." + extension) && !name.startsWith("."))
            {
                files.add(entry);
            }
        }

        return files;
    }
}