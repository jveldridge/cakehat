package cakehat.services;

import cakehat.Allocator;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import support.utils.FileCopyingException;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;

/**
 *
 * @author jak2
 */
public class FileSystemServicesImpl implements FileSystemServices
{
    @Override
    public Set<File> makeDirectory(File dir) throws ServicesException
    {
        try
        {
            return Allocator.getFileSystemUtilities().makeDirectory(dir, Allocator.getCourseInfo().getTAGroup());
        }
        catch(IOException e)
        {
            throw new ServicesException("Unable to create directory: " + dir.getAbsolutePath(), e);
        }
    }

    @Override
    public Set<File> copy(File src, File dst, OverwriteMode overwrite, boolean preserveDate,
            FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        return Allocator.getFileSystemUtilities().copy(src, dst, overwrite, preserveDate,
                Allocator.getCourseInfo().getTAGroup(), copyPermissions);
    }
    
    @Override
    public void makeTempDir() throws ServicesException
    {
        File tempDir = Allocator.getPathServices().getTempDir();

        //If the temporary directory already exists, attempt to delete it
        if(tempDir.exists())
        {
            Allocator.getFileSystemUtilities().deleteFilesSilently(ImmutableSet.of(tempDir));
        }

        //Create the temporary directory
        try
        {
            Allocator.getFileSystemServices().makeDirectory(tempDir);
        }
        catch(ServicesException e)
        {
            throw new ServicesException("Unable to create user's temp directory: " + tempDir.getAbsolutePath(), e);
        }
        
        //Due to NFS (networked file system) behavior, the directory might not always be succesfully deleted - there is
        //NOTHING that can be done about this, even 'rm -rf' will fail in these situations
        Allocator.getFileSystemUtilities().deleteFilesOnExit(ImmutableSet.of(tempDir));
    }
    
    @Override
    public void makeDatabaseBackup() throws ServicesException
    {
        String backupFileName =  "database_backup_" + System.currentTimeMillis() + ".db";
        File backupFile = new File(Allocator.getPathServices().getDatabaseBackupDir(), backupFileName);
        try
        {
            Allocator.getFileSystemServices().copy(Allocator.getPathServices().getDatabaseFile(), backupFile,
                    OverwriteMode.FAIL_ON_EXISTING, false, FileCopyPermissions.READ_WRITE);
        }
        catch(FileCopyingException ex)
        {
            throw new ServicesException("Unable to make database backup.", ex);
        }
    }
}