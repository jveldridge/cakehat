package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author jak2
 */
public class FilePermissionException extends IOException
{
    private final Set<File> _files;
    
    public FilePermissionException(Iterable<File> files, String msg)
    {
        super(buildMessage(msg, files));
        
        _files = ImmutableSet.copyOf(files);
    }
    
    public Set<File> getFiles()
    {
        return _files;
    }
    
    private static String buildMessage(String baseMsg, Iterable<File> files)
    {
        StringBuilder msgBuilder = new StringBuilder(baseMsg);
        msgBuilder.append("\n\nFiles:");
        for(File file : files)
        {
            msgBuilder.append("\n");
            msgBuilder.append(file.getAbsolutePath());
        }
        
        return msgBuilder.toString();
    }
}