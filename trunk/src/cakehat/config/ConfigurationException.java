package cakehat.config;

/**
 * Exception resulting from an error in parsing the configuration file.
 *
 * @author jak2
 */
@Deprecated
public class ConfigurationException extends Exception
{
    public ConfigurationException(String msg)
    {
        super(msg);
    }

    public ConfigurationException(String msg, Throwable cause)
    {
        super(msg, cause);
    }
}