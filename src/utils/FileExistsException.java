package utils;

import java.io.File;

/**
 * An exception that is thrown when a file copy cannot occur because copying
 * would overwrite a destination file and overwrite permission has not been
 * granted.
 * <br/><br/>
 * This exists as a separate class so that it can be caught seperately because
 * in some instances it may be desirable to notify the cakehat user that an
 * existing destination file is the reason the copy failed, as opposed to an
 * inability to copy.
 *
 * @author jak2
 */
public class FileExistsException extends FileCopyingException
{
    private final File _src, _dst;

    public FileExistsException(boolean partialCopy, File src, File dst)
    {
        super(partialCopy, "Cannot copy source file to destination file " +
                "location because destination file exists and overwrite " +
                "permission was not granted.\n" +
                "Source File: " + src.getAbsolutePath() + "\n" +
                "Destination File: " + src.getAbsolutePath() + "\n");

        _src = src;
        _dst = dst;
    }

    public File getSourceFile()
    {
        return _src;
    }

    public File getDestinationFile()
    {
        return _dst;
    }
}