package gradesystem.services;

import gradesystem.Allocator;
import java.io.File;
import java.io.IOException;
import java.util.List;
import utils.FileCopyingException;
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
            Allocator.getFileSystemUtilities().chmodDefault(file);

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

    public void makeDirectory(File dir) throws ServicesException
    {
        //Do nothing if it already exists
        if(!dir.exists())
        {
            //Attempt to directly make the directory
            //If it cannot be done, recursively make all parent directories and
            //then make the directory
            if(!dir.mkdir())
            {
                this.makeDirectory(dir.getParentFile());
                dir.mkdir();
            }

            this.sanitize(dir);
        }
    }

    public List<File> copy(File src, File dst) throws ServicesException
    {
        return this.copy(src, dst, false, false);
    }

    public List<File> copy(File src, File dst, boolean overWrite,
            boolean preserveDate) throws ServicesException
    {
        try
        {
            List<File> files = Allocator.getFileSystemUtilities().copy(src, dst, overWrite, preserveDate);

            for(File file : files)
            {
                //Change the group owner of the copied file
                try
                {
                    Allocator.getFileSystemUtilities().changeGroup(file,
                        Allocator.getCourseInfo().getTAGroup(), false);
                }
                catch(NativeException e1)
                {
                    try
                    {
                        Allocator.getFileSystemUtilities().deleteFiles(files);
                    }
                    catch(IOException e2)
                    {
                        throw new ServicesException("Unable to change the group" +
                                "for: " + file.getAbsolutePath() + "\n" +
                                "Unable to delete all copied files and directories.", e1);
                    }

                    throw new ServicesException("Unable to change the group for: " +
                            file.getAbsolutePath() + "\n" +
                            "All copied files and directories have been deleted.", e1);
                }
            }

            return files;
        }
        catch(FileCopyingException e)
        {
            throw new ServicesException(e);
        }
    }
}