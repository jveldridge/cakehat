package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
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

    public List<File> copy(File src, File dst) throws FileCopyingException
    {
        return this.copy(src, dst, false, false);
    }

    public List<File> copy(File src, File dst, boolean overwrite,
            boolean preserveDate) throws FileCopyingException
    {
        List<File> created;

        if(src.isFile())
        {
            created = new ArrayList<File>();
            this.copyFile(src, dst, overwrite, preserveDate);
            created.add(dst);
        }
        else if(src.isDirectory())
        {
            created = this.copyDirectory(src, dst, overwrite, preserveDate);
        }
        else
        {
            throw new FileCopyingException(false,
                    "Source is neither a file nor a directory: " +
                    src.getAbsolutePath());
        }

        return created;
    }

    /**
     * Recursively copies <code>srcDir</code> and all of its contents into
     * <code>dstDir</code>. Directories will be merged such that if the
     * destination directory or a directory in the destination directory needs
     * to be created and already exist, it will not be deleted. If
     * <code>overwrite</code> is <code>true</code> then files may be overwritten
     * in the copying process.
     *
     * @param srcDir directory to copy
     * @param dstDir
     * @param overwrite
     * @param preserveDate
     * @return all files and directories created in performing the copy
     * @throws IOException
     */
    private List<File> copyDirectory(File srcDir, File dstDir,
            boolean overwrite, boolean preserveDate) throws FileCopyingException
    {
        ArrayList<File> created = new ArrayList<File>();

        //Perform validation
        
        if(!srcDir.exists())
        {
            throw new FileCopyingException(false, "Source directory cannot be " +
                    "copied because it does not exist.\n" +
                    "Source Directory: " + srcDir.getAbsolutePath());
        }

        if(!srcDir.isDirectory())
        {
            throw new FileCopyingException(false, "Source is not a directory, " +
                    "this method only copies directories.\n" +
                    "Source directory: " + srcDir.getAbsolutePath());
        }

        // If the directory exists, that is ok, the contents of the source
        // directory will be merged into the destination directory.
        // However if the destination directory does not exist, create it
        // and record all directories created in the process
        if(!dstDir.exists())
        {
            //Create directory and parent directories (as needed)
            List<File> dirsCreated;
            try
            {
                dirsCreated = this.makeDirectory(dstDir);
                created.addAll(dirsCreated);
            }
            catch(IOException e1)
            {
                try
                {
                    this.deleteFiles(created);
                }
                catch(IOException e2)
                {
                    throw new FileCopyingException(true, "Unable to create " +
                        "directory or parent directory. Unable to delete all" +
                        "files and directories copied so far.\n" +
                        "Destination Directory: "+ dstDir.getAbsolutePath(),
                        e1);
                }

                throw new FileCopyingException(false, "Unable to create " +
                        "directory or parent directory. All files and " +
                        "directories copied so far have been deleted.\n" +
                        "Destination Directory: "+ dstDir.getAbsolutePath(),
                        e1);
            }

            if(preserveDate)
            {
                for(File dirCreated : dirsCreated)
                {
                    if(!dirCreated.setLastModified(srcDir.lastModified()))
                    {
                        try
                        {
                            this.deleteFiles(created);
                        }
                        catch(IOException e)
                        {
                            throw new FileCopyingException(true, "Unable to preserve " +
                                    "modification date. All files copied so far " +
                                    "could NOT be deleted.\n" +
                                    "Unable to modify directory: " + dirCreated.getAbsolutePath(), e);
                        }

                        throw new FileCopyingException(false, "Unable to preserve " +
                                    "modification date. All files copied so far " +
                                    "have been deleted.\n" +
                                    "Unable to modify directory: " + dirCreated.getAbsolutePath());
                    }
                }
            }
        }

        //Copy files and directories inside of this directory
        for(File entry : srcDir.listFiles())
        {
            //Build destination path
            String relativePath = entry.getAbsolutePath().replace(srcDir.getAbsolutePath(), "");
            File entryDst = new File(dstDir, relativePath);

            if(entry.isFile())
            {
                try
                {
                    this.copyFile(entry, entryDst, overwrite, preserveDate);
                    created.add(entryDst);
                }
                catch(FileCopyingException e1)
                {
                    try
                    {
                        this.deleteFiles(created);
                    }
                    catch(IOException e2)
                    {
                        throw new FileCopyingException(true, "Unable to copy file. " +
                                "Unable to delete all files copied so far.", e1);
                    }

                    if(e1.isPartialCopy())
                    {
                        throw new FileCopyingException(true, "Unable to copy file. " +
                            "Unable to delete all files copied so far.", e1);
                    }
                    else
                    {
                        throw new FileCopyingException(false, "Unable to copy file. " +
                            "All files copied so far have been deleted.", e1);
                    }
                }
            }
            else if(entry.isDirectory())
            {
                try
                {
                    created.addAll(this.copyDirectory(entry, entryDst, overwrite, preserveDate));
                }
                catch(IOException e1)
                {
                    try
                    {
                        this.deleteFiles(created);
                    }
                    catch(IOException e2)
                    {
                        throw new FileCopyingException(true, "Unable to copy directory. " +
                                "Unable to delete all files copied so far.", e1);
                    }

                    throw new FileCopyingException(false, "Unable to copy directory. " +
                            "All files copied so far have been deleted.", e1);
                }
            }
        }

        return created;
    }

    public void deleteFiles(List<File> files) throws IOException
    {
        ArrayList<File> failedToDelete = new ArrayList<File>();

        for(File file : files)
        {
            if(!file.delete())
            {
                failedToDelete.add(file);
            }
        }

        if(!failedToDelete.isEmpty())
        {
            throw new IOException("Unable to delete the following" +
                    "files and/or directories: \n" + failedToDelete);
        }
    }

    /**
     * Creates a directory, recursively creating parent directories as
     * necessary. This is similar to {@link java.io.File#mkdirs()} but differs
     * in two important ways. Instead of returning a boolean to indicate
     * success, an exception is thrown if a directory cannot be created. All
     * directories that are created are returned.
     *
     * @param dir the directory to create
     * @return directories created
     * @throws IOException thrown if unable to create any of the necessary
     * directories in order for <code>dir</code> to exist
     */
    private List<File> makeDirectory(File dir) throws IOException
    {
        ArrayList<File> dirsCreated = new ArrayList<File>();

        if(dir != null && !dir.exists())
        {
            dirsCreated.addAll(this.makeDirectory(dir.getParentFile()));

            if(!dir.mkdir())
            {
                throw new IOException("Unable to create directory: " + dir.getAbsolutePath());
            }

            dirsCreated.add(dir);
        }

        return dirsCreated;
    }

    /**
     * Copies <code>srcFile</code> to <code>dstDir</code>. If
     * <code>dstDir</code> exists and <code>overWrite</code> is
     * <code>false</code> then an exception will be thrown.
     *
     * @param srcFile
     * @param dstFile
     * @param overrite
     * @param preserveDate
     * @throws IOException
     */
    private void copyFile(File srcFile, File dstFile, boolean overwrite,
            boolean preserveDate) throws FileCopyingException
    {
        //Perform validation

        if(!srcFile.exists())
        {
            throw new FileCopyingException(false, "Source file cannot be copied " +
                    "because it does not exist.\n" +
                    "Source File: " + srcFile.getAbsolutePath());
        }

        if(!srcFile.isFile())
        {
            throw new FileCopyingException(false, "Source is not a file, this " +
                    "method only copies files.\n" +
                    "Source file: " + srcFile.getAbsolutePath());
        }

        if(dstFile.exists())
        {
            if(overwrite)
            {
                if(!dstFile.delete())
                {
                    throw new FileCopyingException(false, "Cannot overwrite " +
                            "destination file; unable to delete it.\n" +
                            "Destination file: " + dstFile.getAbsolutePath());
                }
            }
            else
            {
                throw new FileExistsException(false, srcFile, dstFile);
            }
        }

        //Attempt to copy
        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try
        {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(dstFile);
            input  = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while(pos < size)
            {
                count = (size - pos);
                pos += output.transferFrom(input, pos, count);
            }
        }
        catch(IOException e)
        {
            //Attempt to delete partially copied file
            if(dstFile.exists() && !dstFile.delete())
            {
                throw new FileCopyingException(true, "Error occurred during " +
                        "copying file. Unable to delete partially copied file.\n" +
                        "Source File: " + srcFile.getAbsolutePath() + "\n" +
                        "Destination File: " + dstFile.getAbsolutePath() + "\n",
                        e);
            }

            throw new FileCopyingException(false, "Error occurred during " +
                    "copying file. Partially copied file was deleted.\n" +
                    "Source File: " + srcFile.getAbsolutePath() + "\n" +
                    "Destination File: " + dstFile.getAbsolutePath() + "\n",
                    e);
        }
        finally
        {
            //Attempt to close the streams and channels, but if it fails that
            //does not actually mean anything went wrong with copyiny, so
            //there is no need to do anything about it
            try
            {
                if (output != null) {output.close();}
                if (fos != null) {fos.close();}
                if (input != null) {input.close();}
                if (input != null) {fis.close();}
            }
            catch(IOException e) { }
        }

        //If failed to copy the entire file
        if(srcFile.length() != dstFile.length())
        {
            if(dstFile.delete())
            {
                throw new FileCopyingException(false, "Unable to copy the full " +
                        "content of the file. The destination file has been deleted.\n" +
                        "Source File: " + srcFile + "\n" +
                        "Destination File: " + dstFile);
            }
            else
            {
                throw new FileCopyingException(true, "Unable to copy the full " +
                        "content of the file. The destination file could NOT be deleted.\n" +
                        "Source File: " + srcFile + "\n" +
                        "Destination File: " + dstFile);
            }
        }

        //If requestd, set the destination's modified date to that of the source
        if(preserveDate)
        {
            if(!dstFile.setLastModified(srcFile.lastModified()))
            {
                if(dstFile.delete())
                {
                    throw new FileCopyingException(false, "Unable to preserve the " +
                        "modification date. The destination file has been deleted.\n" +
                        "Source File: " + srcFile + "\n" +
                        "Destination File: " + dstFile);
                }
                else
                {
                    throw new FileCopyingException(true, "Unable to preserve the " +
                        "modification date. The destination file could NOT be deleted.\n" +
                        "Source File: " + srcFile + "\n" +
                        "Destination File: " + dstFile);
                }
            }
        }
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
        this.chmodDefault(file, true);
    }

    public void chmodDefault(File file, boolean recursive) throws NativeException
    {
        if(file.isDirectory())
        {
            this.chmod(file, false,
                    Permission.OWNER_READ, Permission.OWNER_WRITE, Permission.OWNER_EXECUTE,
                    Permission.GROUP_READ, Permission.GROUP_WRITE, Permission.GROUP_EXECUTE);

            if(recursive)
            {
                for(File entry : file.listFiles())
                {
                    this.chmodDefault(entry, recursive);
                }
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

    public List<File> getFiles(File file, FileFilter filter)
    {
        ArrayList<File> acceptedFiles = new ArrayList<File>();

        if(filter.accept(file))
        {
            acceptedFiles.add(file);
        }

        if(file.isDirectory())
        {
            for(File entry : file.listFiles())
            {
                acceptedFiles.addAll(this.getFiles(entry, filter));
            }
        }

        return acceptedFiles;
    }

    public List<File> getFiles(File file, FileFilter filter, Comparator<File> comparator)
    {
        List<File> files = this.getFiles(file, filter);
        Collections.sort(files, comparator);

        return files;
    }
}