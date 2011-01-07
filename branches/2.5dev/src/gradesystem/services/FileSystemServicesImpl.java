package gradesystem.services;

import gradesystem.Allocator;
import java.io.File;
import utils.system.NativeException;

/**
 *
 * @author jak2
 */
public class FileSystemServicesImpl implements FileSystemServices
{
    public void sanitize(File file) throws NativeException
    {
        //Permissions
        Allocator.getFileSystemUtilities().chmodDefault(file);

        //Group owner
        this.changeGroupToTAGroup(file);
    }

    public void makeDirectory(File dir) throws NativeException
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

    /**
     * Changes the file or directory's group to be the TA group. The user
     * must own the file or directory in order to change the group. For
     * directories this call is recursive.
     *
     * @param file
     * @throws NativeException thrown if the group cannot be changed
     */
    private void changeGroupToTAGroup(File file) throws NativeException
    {
        Allocator.getFileSystemUtilities().changeGroup(file,
                    Allocator.getCourseInfo().getTAGroup(), true);
    }
}