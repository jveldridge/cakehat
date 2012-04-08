package cakehat;

/**
 *
 * @author jak2
 */
public class InitializationException extends RuntimeException
{
    public InitializationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
    
    public InitializationException(Throwable cause)
    {
        super(cause);
    }
}