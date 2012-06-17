package support.utils;

import java.io.IOException;

/**
 * Exception that represents a failure to copy a {@link java.io.File}.
 *
 * @author jak2
 */
public class FileCopyingException extends IOException
{
    private final boolean _partialCopy;

    public FileCopyingException(boolean partialCopy, String message)
    {
        super(message);

        _partialCopy = partialCopy;
    }

    public FileCopyingException(boolean partialCopy, String message, Throwable cause)
    {
        super(message, cause);

        _partialCopy = partialCopy;
    }

    public FileCopyingException(boolean partialCopy, Throwable cause)
    {
        super(cause);
        
        _partialCopy = partialCopy;
    }

    /**
     * If a copy fails for any reason, an attempt will be made to delete all files created as a result of the copy. This
     * attempt may not necessarily succeed. If {@code false} is returned then all files and directories created as a
     * result of the failed copy no longer exist. It may be treated as if the copy never occurred. If {@code true} is
     * returned then some number of directories and files (including possibly an incomplete file) from the copy remain.
     *
     * @return
     */
    public boolean isPartialCopy()
    {
        return _partialCopy;
    }
}