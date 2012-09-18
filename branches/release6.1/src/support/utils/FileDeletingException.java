package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Represents a failure to delete one or more file or directory.
 *
 * @author jak2
 */
public class FileDeletingException extends IOException
{
    private final Set<File> _failedToDelete;
    
    public FileDeletingException(Iterable<File> failedToDelete)
    {
        super(buildMessage(failedToDelete));
        
        _failedToDelete = ImmutableSet.copyOf(failedToDelete);
    }
    
    private static String buildMessage(Iterable<File> failedToDelete)
    {
        StringBuilder failedToDeleteBuilder = new StringBuilder("Unable to delete:");
        for(File nonDeleted : failedToDelete)
        {
            failedToDeleteBuilder.append("\n");
            failedToDeleteBuilder.append(nonDeleted.getAbsolutePath());
        }
        
        return failedToDeleteBuilder.toString();
    }
    
    public Set<File> getFilesNotDeleted()
    {
        return _failedToDelete;
    }
}