package cakehat.gml;

import java.util.Arrays;
import org.w3c.dom.Node;

/**
 *
 * @author Hannah
 */
public class GMLException extends GradingSheetException {
    /**
     * Constructs an instance of <code>GradingSheetException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public GMLException(String msg)
    {
        super(msg);
    }
    
    
    public GMLException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a message that a given parent only allows for certain
     * supported children and that this node is not supported.
     *
     * @param parentTag
     * @param node unsupported node
     * @param supportedTags
     */
    public GMLException(String parentTag, Node node, String... supportedTags)
    {
        super("Unsupported " + parentTag + " child: " + node.getNodeName() + ", only " +
              Arrays.toString(supportedTags) +
              (supportedTags.length == 1 ? " is" : " are") + " supported.");
    }

}
