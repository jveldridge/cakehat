package gradesystem.handin;

/**
 * An exception that occurred while performing a {@link DistributableAction} or
 * one of the non-configurable aspects of {@link DistributablePart} such as
 * viewing a readme.
 *
 * @author jak2
 */
public class ActionException extends Exception
{
    ActionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    ActionException(Throwable cause)
    {
        super(cause);
    }

    ActionException(String message)
    {
        super(message);
    }
}