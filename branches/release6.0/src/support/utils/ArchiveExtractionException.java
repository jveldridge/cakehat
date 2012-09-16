package support.utils;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author jak2
 */
public class ArchiveExtractionException extends IOException
{
    private final boolean _partialExtraction;
    private final Set<File> _remainingFiles;

    public ArchiveExtractionException(boolean partialExtraction, Set<File> remainingFiles, String message,
            Throwable cause)
    {
        super(message, cause);

        _partialExtraction = partialExtraction;
        _remainingFiles = ImmutableSet.copyOf(remainingFiles);
    }

    /**
     * If an extraction fails for any reason, an attempt will be made to delete all files created as a result of the
     * extraction. This attempt may not necessarily succeed. If {@code false} is returned then all files and directories
     * created as a result of the failed extraction no longer exist. It may be treated as if the extraction never
     * occurred. If {@code true} is returned then some number of directories and files (including possibly an incomplete
     * file) from the extraction remain.
     *
     * @return
     */
    public boolean isPartialExtraction()
    {
        return _partialExtraction;
    }
    
    /**
     * The files and directories that remain from the failed extraction. This will be an empty set if
     * {@link #isPartialExtraction()} return {@code false}.
     * 
     * @return 
     */
    public Set<File> getRemainingFiles()
    {
        return _remainingFiles;
    }
}