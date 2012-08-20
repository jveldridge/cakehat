package cakehat.database.assignment;

import cakehat.Allocator;
import cakehat.database.Group;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.HashSet;
import support.ui.ModalDialog;
import support.utils.FileCopyingException;
import support.utils.OrFileFilter;
import support.utils.FileExtensionFilter;
import support.utils.FileSystemUtilities.FileCopyPermissions;
import support.utils.FileSystemUtilities.OverwriteMode;
import support.utils.NoFileExtensionFilter;

/**
 * Functionality used commonly by tasks. This class exists to prevent code duplication.
 *
 * @author jak2
 */
class TaskUtilities
{
    /**
     * Creates a FileFilter that parses a string containing a comma-separated list of file extensions. To represent a
     * file that has no file extension, an underscore {@code _} is to be used.
     * <br/><br/>
     * Examples:
     * <br/>
     * {@code java}
     * <br/>
     * {@code c, cpp, h, _}
     *
     * @param extensions
     * @return file filter
     */
    static FileFilter parseFileExtensions(String extensions)
    {
        //To allow for spaces not being used, remove spaces if they are present
        extensions = extensions.replace(" ", "");

        //Build a set of the extensions supplied
        HashSet<String> extensionSet = new HashSet<String>(Arrays.asList(extensions.split(",")));

        //If underscore is used, accept files without file extensions
        FileFilter filter;
        if(extensionSet.contains("_"))
        {
            extensionSet.remove("_");
            filter = new OrFileFilter(new NoFileExtensionFilter(), new FileExtensionFilter(extensionSet));
        }
        else
        {
            filter = new FileExtensionFilter(extensionSet);
        }
        
        return filter;
    }
    
    /**
     * Performs a copy. Intended to support copy test tasks. Copies into the action temporary directory.
     * 
     * @param source
     * @param context
     * @param part
     * @param group
     * @return if copy occurred
     */
    static boolean copyForTest(File source, TaskContext context, Action action, Group group) throws TaskException,
            TaskConfigurationIssue
    {
        if(!source.exists() || !source.canRead())
        {
            throw new TaskConfigurationIssue("Cannot perform test because the directory or file to copy does not " +
                    "exist or cannot be read.\n" +
                    "Source: " + source.getAbsoluteFile());
        }
          
        boolean performedCopy;
        try
        {
            File actionDir = Allocator.getPathServices().getActionTempDir(action, group);
            File destination;
            if(source.isFile())
            {
                destination = new File(actionDir, source.getName());
            }
            else
            {
                destination = actionDir;
            }

            Allocator.getFileSystemServices().copy(source, destination, OverwriteMode.FAIL_ON_EXISTING, false,
                    FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE);
            performedCopy = true;
        }
        catch(FileCopyingException e)
        {
            if(e.isFailureDueToExistingFile())
            {
                ModalDialog.showMessage(context.getGraphicalOwner(), "Cannot copy test file",
                    "Cannot perform test because a file to be copied for the test already exists in the unarchived " +
                    "handin.\n\n" +
                    "Test File: " + e.getSourceFileForFailureDueToExistingFile().getAbsolutePath() + "\n" +
                    "Handin File: " + e.getDestinationFileForFailureDueToExistingFile().getAbsolutePath());
                performedCopy = false;
            }
            else
            {
                throw new TaskException("Unable to perform copy necessary for testing.", e);
            }
        }
        
        return performedCopy;
    }
    
    static void copyUnarchivedHandinToTemp(Action action, Group group) throws TaskException
    {
        File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
        File tempDir = Allocator.getPathServices().getActionTempDir(action, group);
        
        try
        {
            Allocator.getFileSystemServices().copy(unarchiveDir, tempDir, OverwriteMode.REPLACE_EXISTING, true,
                    FileCopyPermissions.READ_WRITE_PRESERVE_EXECUTE);
        }
        catch(FileCopyingException e)
        {
            throw new TaskException("Unable to copy unarchived handin contents to temporary action directory\n" +
                    "Action: " + action.getDebugName() + "\n" +
                    "Group: " + group + "\n" +
                    "Unarchive Handin Directory: " + unarchiveDir.getAbsolutePath() + "\n" +
                    "Action Temp Directory: " + tempDir.getAbsolutePath(), e);
        }
    }
}