package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Vector;

public class FileSystemUtilitiesImpl implements FileSystemUtilities
{
    public Calendar getModifiedDate(File file)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(file.lastModified());

        return calendar;
    }

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

    public boolean copyFile(String sourcePath, String destPath)
    {
         return copyFile(new File(sourcePath), new File(destPath));
    }

    public String readFile(File file) throws FileNotFoundException, IOException
    {
        StringBuilder text = new StringBuilder();
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

        return text.toString();
    }

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

    public boolean removeDirectory(String dirPath)
    {
        return removeDirectory(new File(dirPath));
    }

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