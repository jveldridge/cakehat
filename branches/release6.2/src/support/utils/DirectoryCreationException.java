package support.utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Represents a failure to create a directory.
 *
 * @author jak2
 */
public class DirectoryCreationException extends IOException
{
    private final File _dir;
    private final Set<File> _remainingDirectories;
    
    public DirectoryCreationException(File dir, Set<File> remainingDirectories, Throwable cause)
    {
        super(buildMessage(dir, remainingDirectories));
        
        _dir = dir;
        _remainingDirectories = remainingDirectories;
    }
    
    private static String buildMessage(File dir, Set<File> remainingDirectories)
    {
        StringBuilder messageBuilder = new StringBuilder("Unable to create directory: " + dir.getAbsolutePath());
        
        if(!remainingDirectories.isEmpty())
        {
            messageBuilder.append("\nUnable to delete the following directories that were created:");
            for(File nonDeleted : remainingDirectories)
            {
                messageBuilder.append("\n");
                messageBuilder.append(nonDeleted.getAbsolutePath());
            }
        }
        
        return messageBuilder.toString();
    }
    
    /**
     * The directory that could not be successfully created.
     * 
     * @return 
     */
    public File getDirectory()
    {
        return _dir;
    }
    
    /**
     * The directories that remain from the failed directory creation.
     * 
     * @return 
     */
    public Set<File> getRemainingDirectories()
    {
        return _remainingDirectories;
    }
}