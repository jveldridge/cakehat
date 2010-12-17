package gradesystem.rubric;

import java.util.Arrays;
import org.w3c.dom.Node;

/**
 * An exception that occurs while parsing or writing a GML file.
 *
 * @author jak2
 */
public class RubricException extends Exception
{
    /**
     * Constructs an instance of <code>RubricException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RubricException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs a message that a given parent only allows for certain
     * supported children and that this node is not supported.
     *
     * @param parentTag
     * @param node unsupported node
     * @param supportedTags
     */
    public RubricException(String parentTag, Node node, String... supportedTags)
    {
        super("Unsupported " + parentTag + " child: " + node.getNodeName() + ", only " +
              Arrays.toString(supportedTags) +
              (supportedTags.length == 1 ? " is" : " are") + " supported.");
    }
}
