package utils;

import java.io.File;
import java.io.IOException;

/**
 * An exception that indicates the permission of a file or directory could not
 * be changed.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class PermissionException extends IOException
{
    public PermissionException(File file)
    {
        super("Permission could not be changed for: " + file.getAbsolutePath());
    }

    public PermissionException(Throwable cause)
    {
        super(cause);
    }

    public PermissionException(String message)
    {
        super(message);
    }
}