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
import utils.system.NativeFunctions;
import utils.FileSystemUtilities.Permission;
import utils.system.NativeException;

public class FileSystemUtilitiesImpl implements FileSystemUtilities
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();

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

    public void chmod(File file, boolean recursive, Permission... mode) throws NativeException
    {
        int modeValue = 0;
        for(Permission permission : mode)
        {
            modeValue += permission.getValue();
        }

        this.chmod(file, recursive, modeValue);
    }

    public void chmodDefault(File file) throws NativeException
    {
        if(file.isDirectory())
        {
            this.chmod(file, false,
                    Permission.OWNER_READ, Permission.OWNER_WRITE, Permission.OWNER_EXECUTE,
                    Permission.GROUP_READ, Permission.GROUP_WRITE, Permission.GROUP_EXECUTE);

            for(File entry : file.listFiles())
            {
                this.chmodDefault(entry);
            }
        }
        else
        {
            this.chmod(file, false,
                    Permission.OWNER_READ, Permission.OWNER_WRITE,
                    Permission.GROUP_READ, Permission.GROUP_WRITE);
        }
    }

    private void chmod(File file, boolean recursive, int mode) throws NativeException
    {
        NATIVE_FUNCTIONS.chmod(file, mode);

        if(recursive && file.isDirectory())
        {
            for(File subfile : file.listFiles())
            {
                this.chmod(subfile, recursive, mode);
            }
        }
    }

    public void changeGroup(File file, String group, boolean recursive) throws NativeException
    {
        NATIVE_FUNCTIONS.changeGroup(file, group);

        if(file.isDirectory() && recursive)
        {
            for(File entry : file.listFiles())
            {
                this.changeGroup(entry, group, recursive);
            }
        }
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