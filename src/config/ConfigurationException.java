package config;

import java.util.Arrays;
import org.w3c.dom.Node;

/**
 * Exception resulting from an error in parsing the configuration file.
 *
 * @author jak2
 */
public class ConfigurationException extends Exception
{
    /**
     * Constructs an instance of <code>ConfigurationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConfigurationException(String msg)
    {
        super(msg);
    }

    /**
     * Constructed with a message that a given parent only allows for certain supported
     * tags and that this node is not supported.
     *
     * @param parentTag
     * @param node unsupported node
     * @param supportedTags
     */
    public ConfigurationException(String parentTag, Node node, String... supportedTags)
    {
        super("Unsupported " + parentTag + " child: " + node.getNodeName() + ", only " +
              Arrays.toString(supportedTags) +
              (supportedTags.length == 1 ? " is" : " are") + " supported.");
    }
}