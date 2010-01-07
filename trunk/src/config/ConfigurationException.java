package config;

/**
 *
 * @author jak2
 */
public class ConfigurationException extends Exception
{
    /**
     * Creates a new instance of <code>ConfigurationException</code> without detail message.
     */
    public ConfigurationException() { }


    /**
     * Constructs an instance of <code>ConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConfigurationException(String msg)
    {
        super(msg);
    }
}