package support.utils;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import support.utils.posix.NativeFunctions;
import support.utils.FileSystemUtilities.Permission;
import support.utils.posix.NativeException;

public class FileSystemUtilitiesImpl implements FileSystemUtilities
{
    private static final NativeFunctions NATIVE_FUNCTIONS = new NativeFunctions();

    private static final Permission[] READ_WRITE_PERMISSIONS = new Permission[]
    {
        Permission.OWNER_READ, Permission.OWNER_WRITE,
        Permission.GROUP_READ, Permission.GROUP_WRITE
    };

    private static final Permission[] READ_WRITE_EXECUTE_PERMISSIONS = new Permission[]
    {
        Permission.OWNER_READ, Permission.OWNER_WRITE, Permission.OWNER_EXECUTE,
        Permission.GROUP_READ, Permission.GROUP_WRITE, Permission.GROUP_EXECUTE
    };

    public Calendar getModifiedDate(File file)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(file.lastModified());

        return calendar;
    }
    
    public List<File> copy(File src, File dst, OverwriteMode overwrite,
            boolean preserveDate, String groupOwner,
            FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        List<File> created;

        if(src.isFile())
        {
            created = this.copyFile(src, dst, overwrite, preserveDate, groupOwner, copyPermissions);
        }
        else if(src.isDirectory())
        {
            created = this.copyDirectory(src, dst, overwrite, preserveDate, groupOwner, copyPermissions);
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
     * @param groupOwner
     * @param copyPermissions
     *
     * @return all files and directories created in performing the copy
     * @throws IOException
     */
    private List<File> copyDirectory(File srcDir, File dstDir,
            OverwriteMode overwrite, boolean preserveDate, String groupOwner,
            FileCopyPermissions copyPermissions) throws FileCopyingException
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
                dirsCreated = this.makeDirectory(dstDir, groupOwner);
                created.addAll(dirsCreated);
            }
            catch(IOException e)
            {
                throw this.cleanupFailedCopy(created,
                        "Unable to create directory or parent directory.", e, srcDir, dstDir);
            }

            if(preserveDate)
            {
                for(File dirCreated : dirsCreated)
                {
                    if(!dirCreated.setLastModified(srcDir.lastModified()))
                    {
                        throw this.cleanupFailedCopy(dirsCreated,
                                "Unable to preserve modification date", null, srcDir, dstDir);
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
                    created.addAll(this.copyFile(entry, entryDst, overwrite, preserveDate, groupOwner, copyPermissions));
                }
                catch(FileCopyingException e)
                {
                    throw this.cleanupFailedCopy(created,
                            "Unable to copy file contained in directory", e, srcDir, dstDir);
                }
            }
            else if(entry.isDirectory())
            {
                try
                {
                    created.addAll(this.copyDirectory(entry, entryDst, overwrite, preserveDate, groupOwner, copyPermissions));
                }
                catch(IOException e)
                {
                    throw this.cleanupFailedCopy(created,
                            "Unable to copy directory contained in directory", e, srcDir, dstDir);
                }
            }
        }

        return created;
    }

    /**
     * Deletes all files, throwing an informative exception if any of the files
     * cannot be deleted.
     *
     * @param files
     * @throws IOException
     */
    private void deleteFiles(List<File> files) throws IOException
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
     * Helper method used by copy methods to delete files if an issue has been
     * encountered in copying. This should be used when a copy has failed and
     * an exception must be thrown, but before doing so all of the files created
     * so far in the copy should be deleted. The exception to be thrown will be
     * generated and its contents will differ depending on the success of
     * deleting the files.
     *
     * @param toDelete files that need to be deleted
     * @param message explanation of what went wrong
     * @param cause the reason that the copy is being aborted, may be <code>null</code>
     * @param srcFile
     * @param dstFile
     *
     * @returns FileCopyingException an exception built from the parameters
     * passed and whether deleting the files succeeded
     */
    private FileCopyingException cleanupFailedCopy(List<File> toDelete,
            String message, Throwable cause, File srcFile, File dstFile)
    {
        try
        {
            this.deleteFiles(toDelete);

            return new FileCopyingException(false, message + " "  +
                "The files and/or directories created in the copy have been" +
                "deleted.\n" +
                "Source File: " + srcFile + "\n" +
                "Destination File: " + dstFile, cause);
        }
        catch(IOException e)
        {
            return new FileCopyingException(true, message + " " +
                "Unable to delete partially copied files and/or directories.\n" +
                "Source File: " + srcFile + "\n" +
                "Destination File: " + dstFile, cause);
        }
    }

    public void deleteFile(File file) throws IOException
    {
        if(!file.exists())
        {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }
        else if(file.isFile())
        {
            if(!file.delete())
            {
                throw new IOException("Cannot delete file: " + file.getAbsolutePath());
            }
        }
        else if(file.isDirectory())
        {
            //To delete a directory its entire contents must first be deleted
            for(File entry : file.listFiles())
            {
                deleteFile(entry);
            }

            if(!file.delete())
            {
                throw new IOException("Cannot delete directory: " + file.getAbsolutePath());
            }
        }
        else
        {
            throw new IOException("Unable to handle non-file and non-directory: " +
                    file.getAbsolutePath());
        }
    }

    public List<File> makeDirectory(File dir, String groupOwner) throws IOException
    {
        ArrayList<File> dirsCreated = new ArrayList<File>();

        if(dir != null && !dir.exists())
        {
            dirsCreated.addAll(this.makeDirectory(dir.getParentFile(), groupOwner));

            if(!dir.mkdir())
            {
                throw new IOException("Unable to create directory: " + dir.getAbsolutePath());
            }

            try
            {
                this.chmod(dir, false, READ_WRITE_EXECUTE_PERMISSIONS);
                this.changeGroup(dir, groupOwner, false);
            }
            catch(NativeException e)
            {
                throw new IOException("Unable to set correct permissions and " +
                        "ownership for directory: " + dir.getAbsolutePath(), e);
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
     * @param overwrite
     * @param preserveDate
     * @param groupOwner
     * @param copyPermissions
     *
     * @throws FileCopyingException
     *
     * @return
     */
    private List<File> copyFile(File srcFile, File dstFile,
            OverwriteMode overwrite, boolean preserveDate, String groupOwner,
            FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        ArrayList<File> created = new ArrayList<File>();

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
            if(overwrite == OverwriteMode.REPLACE_EXISTING)
            {
                if(!dstFile.delete())
                {
                    throw new FileCopyingException(false, "Cannot overwrite " +
                            "destination file; unable to delete it.\n" +
                            "Destination file: " + dstFile.getAbsolutePath());
                }
            }
            else if(overwrite == OverwriteMode.FAIL_ON_EXISTING)
            {
                throw new FileExistsException(false, srcFile, dstFile);
            }
            else if(overwrite == OverwriteMode.KEEP_EXISTING)
            {
                return created;
            }
            else
            {
                throw new FileCopyingException(false, "Invalid "
                        + OverwriteMode.class.getCanonicalName() + ": " +
                        overwrite + ".");
            }
        }

        //If the destination location needs directories to be created
        if(!dstFile.getParentFile().exists())
        {
            try
            {
                created.addAll(makeDirectory(dstFile.getParentFile(), groupOwner));
            }
            catch(IOException e)
            {
                throw new FileCopyingException(false, "Unable to create the " +
                        "necessary directories in order to perform the copy.", e);
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
            if(dstFile.exists())
            {
                created.add(dstFile);
            }

            this.cleanupFailedCopy(created,
                    "Error occurred during copying the file.", e, srcFile, dstFile);
        }
        finally
        {
            //Attempt to close the streams and channels, but if it fails that
            //does not actually mean anything went wrong with copying, so there
            //is no need to do anything about it
            try
            {
                if(output != null)
                {
                    output.close();
                }
                if(fos != null)
                {
                    fos.close();
                }
                if(input != null)
                {
                    input.close();
                }
                if(input != null)
                {
                    fis.close();
                }
            }
            catch(IOException e) { }
        }

        //File has now been created
        created.add(dstFile);

        //If failed to copy the entire file
        if(srcFile.length() != dstFile.length())
        {
            throw this.cleanupFailedCopy(created,
                    "Unable to copy the full content of the file.", null, srcFile, dstFile);
        }

        //If requested, set the destination's modified date to that of the source
        if(preserveDate)
        {
            if(!dstFile.setLastModified(srcFile.lastModified()))
            {
                throw this.cleanupFailedCopy(created,
                        "Unable to preserve the modification date.", null, srcFile, dstFile);
            }
        }

        //Set the specified group owner
        try
        {
            this.changeGroup(dstFile, groupOwner, false);
        }
        catch(NativeException e)
        {
            throw this.cleanupFailedCopy(created,
                    "Unable to set the group owner.", e, srcFile, dstFile);
        }

        //Set the specified permissions
        Permission[] permissions;
        if(copyPermissions == FileCopyPermissions.READ_WRITE ||
            (copyPermissions == FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE &&
             !srcFile.canExecute()))
        {
            permissions = READ_WRITE_PERMISSIONS;
        }
        else if(copyPermissions == FileCopyPermissions.READ_WRITE_EXECUTE ||
                 (copyPermissions == FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE &&
                  srcFile.canExecute()))
        {
            permissions = READ_WRITE_EXECUTE_PERMISSIONS;
        }
        //This should never arise, but if another FileCopyPermission enum value
        //was added and this code was not updated then this block could be reached
        else
        {
            throw this.cleanupFailedCopy(created,
                    "Invalid " + FileCopyPermissions.class.getCanonicalName() +
                    ": " + copyPermissions + ".", null, srcFile, dstFile);
        }

        try
        {
            this.chmod(dstFile, false, permissions);
        }
        catch(NativeException e)
        {
            throw this.cleanupFailedCopy(created,
                    "Unable to set permissions: " + Arrays.toString(permissions) + ".",
                    e, srcFile, dstFile);
        }

        created.add(dstFile);

        return created;
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

    public void chmodDefault(File file, boolean recursive) throws NativeException
    {
        if(file.isDirectory())
        {
            this.chmod(file, false, READ_WRITE_EXECUTE_PERMISSIONS);

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
            this.chmod(file, false, READ_WRITE_PERMISSIONS);
        }
    }

    /**
     * Changes the permissions of a file. The user <strong>must</strong> be the
     * owner of the file or else a {@link NativeException} will be thrown.
     *
     * @param file
     * @param recursive
     * @param mode the permission mode, an octal value
     * @throws NativeException
     */
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

    public List<File> getFiles(File file, FileFilter filter) throws IOException
    {
        ArrayList<File> acceptedFiles = new ArrayList<File>();

        if(filter.accept(file))
        {
            acceptedFiles.add(file);
        }

        if(file.isDirectory())
        {
            File[] entries = file.listFiles();

            if(entries == null)
            {
                throw new IOException("Unable to retrieve contents of " +
                        "directory: " + file.getAbsoluteFile() + ".\nThis " +
                        "is likely due to a permissions issue or an IO error.");
            }

            for(File entry : entries)
            {
                acceptedFiles.addAll(this.getFiles(entry, filter));
            }
        }

        return acceptedFiles;
    }

    public List<File> getFiles(File file, FileFilter filter, Comparator<File> comparator) throws IOException
    {
        List<File> files = this.getFiles(file, filter);
        Collections.sort(files, comparator);

        return files;
    }
}