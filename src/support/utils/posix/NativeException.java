package support.utils.posix;

import com.sun.jna.LastErrorException;

/**
 * Represents an exception that occurred from a native function call.
 *
 * @author jak2
 */
public class NativeException extends Exception
{
    public NativeException(String msg)
    {
        super(msg);
    }

    NativeException(LastErrorException cause, String msg)
    {
        super(msg + " \n" +
              ErrorCodes.getError(cause.getErrorCode()).name() + " (" + 
              cause.getErrorCode()  + "): " +
              ErrorCodes.getError(cause.getErrorCode()).getDescription(), cause);
    }
}