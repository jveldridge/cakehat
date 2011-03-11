package gradesystem.services;

import gradesystem.Allocator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import utils.FileCopyingException;
import utils.FileSystemUtilities.FileCopyPermissions;
import utils.system.NativeException;

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

    public List<File> copy(File src, File dst, boolean overwrite,
        boolean preserveDate, FileCopyPermissions copyPermissions) throws FileCopyingException
    {
        return Allocator.getFileSystemUtilities()
                .copy(src, dst, overwrite, preserveDate,
                      Allocator.getCourseInfo().getTAGroup(), copyPermissions);
    }
}