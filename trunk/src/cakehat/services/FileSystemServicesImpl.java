package cakehat.services;

import cakehat.Allocator;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import support.utils.FileCopyingException;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;
import support.utils.posix.NativeException;

/**
 *
 * @author jak2
 */
public class FileSystemServicesImpl implements FileSystemServices
{
    public void sanitize(File file) throws ServicesException
    {
        try
        {
            //Permissions
            Allocator.getFileSystemUtilities().chmodDefault(file, true);

            //Group owner
            Allocator.getFileSystemUtilities().changeGroup(file,
                    Allocator.getCourseInfo().getTAGroup(), true);
        }
        catch(NativeException e)
        {
            throw new ServicesException("Unable to set group or permissions: " +
                    file.getAbsolutePath(), e);
        }
    }

    public List<File> makeDirectory(File dir) throws ServicesException
    {
        try
        {
            List<File> directoriesMade = Allocator.getFileSystemUtilities()
                    .makeDirectory(dir, Allocator.getCourseInfo().getTAGroup());

            return directoriesMade;
        }
        catch(IOException e)
        {
            throw new ServicesException("Unable to create directory: " +
                    dir.getAbsolutePath(), e);
        }
    }

    public List<File> copy(File src, File dst, OverwriteMode overwrite,
        boolean preserveDate, FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        return Allocator.getFileSystemUtilities()
                .copy(src, dst, overwrite, preserveDate,
                      Allocator.getCourseInfo().getTAGroup(), copyPermissions);
    }
    
    @Override
    public void makeUserWorkspace() throws ServicesException
    {
        File workspace = Allocator.getPathServices().getUserWorkspaceDir();

        //If the workspace already exists, attempt to delete it
        if(workspace.exists())
        {
            try
            {
                Allocator.getFileSystemUtilities().deleteFiles(Arrays.asList(workspace));
            }
            //Do not do anything if this fails, because it will almost certainly be due to NFS (networked file system)
            //issues about which nothing can be done
            catch(IOException e) { }
        }

        //Create the workspace
        try
        {
            Allocator.getFileSystemServices().makeDirectory(workspace);
        }
        catch(ServicesException e)
        {
            throw new ServicesException("Unable to create user's workspace: " + workspace.getAbsolutePath(), e);
        }
        
        //Due to NFS (networked file system) behavior, the workspace might not always be succesfully deleted - there is
        //NOTHING that can be done about this, even 'rm -rf' will fail in these situations
        Allocator.getFileSystemUtilities().deleteFileOnExit(workspace);
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