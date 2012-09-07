package cakehat.assignment;

/**
 * An exception that occurred while performing a {@link Task}.
 *
 * @author jak2
 */
public class TaskException extends Exception
{
    TaskException(String message, Throwable cause)
    {
        super(message, cause);
    }

    TaskException(Throwable cause)
    {
        super(cause);
    }

    TaskException(String message)
    {
        super(message);
    }
}