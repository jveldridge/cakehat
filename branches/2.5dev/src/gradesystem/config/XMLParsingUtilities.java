package gradesystem.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Helper functionality for parsing XML.
 *
 * @author jak2
 */
class XMLParsingUtilities
{
    /**
     * Indicates whether a node should be skipped or not. A node should not be
     * processed if it is a text node or a comment node.
     *
     * @param node
     * @return whether a node should be skipped
     */
    static boolean skipNode(Node node)
    {
        return (node.getNodeName().equals("#text") || node.getNodeName().equals("#comment"));
    }

    /**
     * Parses out all of the attributes of a node, ensuring that all required
     * attributes are present and that only valid attributes are present. All
     * attributes must be unique.
     *
     * @param node
     * @param requiredAttr
     * @param validAttr
     * @return
     * @throws ConfigurationException
     */
    static AttributeMap getAttributes(Node node, String[] requiredAttr,
            String[] validAttr) throws ConfigurationException
    {
        HashSet<String> requiredSet = new HashSet<String>(Arrays.asList(requiredAttr));
        HashSet<String> validSet = new HashSet<String>(Arrays.asList(validAttr));

        NamedNodeMap namedMap = node.getAttributes();
        Map<String, String> attrMap = new HashMap<String, String>();

        for(int i = 0; i < namedMap.getLength(); i++)
        {
            Node attrNode = namedMap.item(i);
            String attrName = attrNode.getNodeName();

            if(skipNode(node))
            {
                continue;
            }
            else if(!validSet.contains(attrName))
            {
                throw new ConfigurationException(attrName + " is not a valid " +
                        "attribute for " + node.getNodeName() + ".\n" +
                        "Valid attributes: " + validSet);
            }
            else if(attrMap.containsKey(attrName))
            {
                throw new ConfigurationException(node.getNodeName() +
                        " has 2 or more " + attrName + " attributes. All" +
                        " attributes must be unique.");
            }
            else
            {
                requiredSet.remove(attrName);
                attrMap.put(attrName, attrNode.getNodeValue());
            }
        }

        //Check all required are present
        if(!requiredSet.isEmpty())
        {
            String msg = "The following required attributes of " + node.getNodeName() +
                    " are not present: " + requiredSet;
            throw new ConfigurationException(msg);
        }

        return new AttributeMap(attrMap);
    }

    /**
     * Wrapper around a map of String to String. Used to conveniently handle
     * the attributes of a Node.
     */
    static class AttributeMap
    {
        private final Map<String, String> _map;

        public AttributeMap(Map<String, String> map)
        {
            _map = map;
        }

        public boolean hasAttribute(String name)
        {
            return _map.containsKey(name);
        }

        public String getString(String name) throws ConfigurationException
        {
            if(_map.containsKey(name))
            {
                return _map.get(name);
            }
            else
            {
               throw new ConfigurationException("Expected attribute does not exist: " + name);
            }
        }

        public String getString(String name, String defaultVal)
        {
            String value = defaultVal;
            if(_map.containsKey(name))
            {
                value = _map.get(name);
            }

            return value;
        }

        public boolean getBoolean(String name) throws ConfigurationException
        {
            if(_map.containsKey(name))
            {
                return Boolean.parseBoolean(_map.get(name));
            }
            else
            {
               throw new ConfigurationException("Expected attribute does not exist: " + name);
            }
        }

        public boolean getBoolean(String name, boolean defaultVal)
        {
            boolean value = defaultVal;
            if(_map.containsKey(name))
            {
                value = Boolean.parseBoolean(_map.get(name));
            }

            return value;
        }

        public int getInt(String name) throws ConfigurationException
        {
            if(_map.containsKey(name))
            {
                try
                {
                    return Integer.parseInt(_map.get(name));
                }
                catch(NumberFormatException e)
                {
                    throw new ConfigurationException(name + " attribute has " +
                            "non-integer value; integer value expected.", e);
                }
            }
            else
            {
                throw new ConfigurationException("Expected attribute does not exist: " + name);
            }
        }

        public int getInt(String name, int defaultVal) throws ConfigurationException
        {
            int value = defaultVal;
            if(_map.containsKey(name))
            {
                try
                {
                    value = Integer.parseInt(_map.get(name));
                }
                catch(NumberFormatException e)
                {
                    throw new ConfigurationException(name + " attribute has " +
                            "non-integer value; integer value expected.", e);
                }
            }

            return value;
        }
    }

    /**
     * Get the Document representing the XML file specified by the string passed in.
     *
     * @param XMLFilePath Absolute file path to the XML file
     * @return Document representing the XML file
     */
    static Document getDocument(File xmlFile) throws ConfigurationException
    {
        //Check if file exists
        if(!xmlFile.exists())
        {
            throw new ConfigurationException("Configuration could not be read, location specified: " +
                    xmlFile.getAbsolutePath());
        }

        //Parse document
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            return document;
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Exception thrown during parsing, "
                    + xmlFile.getAbsolutePath() + " is illegally formatted", e);
        }
    }

    /**
     * Returns the root node.
     *
     * @param document Document representing this XML file
     * @param rootName the name of the root node
     */
    static Node getRootNode(Document document, String rootName) throws ConfigurationException
    {
        Node rubricNode = document.getDocumentElement();

        if(!rubricNode.getNodeName().equals(rootName))
        {
            throw new ConfigurationException("XML not formatted properly. " +
                    "The root " + rootName + " node cannot be found.");
        }

        return rubricNode;
    }

    /**
     * Returns a map of the name of the child node to a list of the nodes with
     * that name. All valid children names will be in the map, but if there are
     * no child nodes of that name then the list associated with that name will
     * be empty.
     *
     * @param node
     * @param validChildren
     * @return
     * @throws ConfigurationException thrown if an invalid child exists
     */
    static Map<String, List<Node>> getChildren(Node node,
            String[] validChildren) throws ConfigurationException
    {
        NodeList children = node.getChildNodes();
        Map<String, List<Node>> map = new HashMap<String, List<Node>>();

        for(String validChild : validChildren)
        {
            map.put(validChild, new ArrayList<Node>());
        }

        for(int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            String childName = child.getNodeName();

            if(!skipNode(child))
            {
                if(map.containsKey(childName))
                {
                    map.get(childName).add(child);
                }
                //An invalid child name
                else
                {
                    String msg = "Unsupported " + node.getNodeName() + " child: " +
                        childName + ". \n" +
                        "Valid children: " + Arrays.toString(validChildren);
                    throw new ConfigurationException(msg);
                }
            }
        }

        return map;
    }

    /**
     * Parses out the children of <code>node</code> with a mapping from the
     * child's name to the child. Ensures all <code>requiredChildren</code> are
     * present and  that only <code>validChildren</code> are present.
     * <br/></br>
     * All children must have unique names.
     *
     * @param node
     * @param requiredChildren
     * @param validChildren
     * @return
     * @throws ConfigurationException
     */
    static Map<String, Node> getUniqueChildren(Node node,
            String[] requiredChildren, String[] validChildren) throws ConfigurationException
    {
        HashSet<String> requiredSet = new HashSet<String>(Arrays.asList(requiredChildren));
        HashSet<String> validSet = new HashSet<String>(Arrays.asList(validChildren));

        NodeList children = node.getChildNodes();
        Map<String, Node> map = new HashMap<String, Node>();

        for(int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            String childName = child.getNodeName();

            if(skipNode(child))
            {
                continue;
            }
            else if(!validSet.contains(childName))
            {
                throw new ConfigurationException(childName + " is not a valid " +
                        "child for " + node.getNodeName() + ".\n" +
                        "Valid children: " + validSet);
            }
            else if(map.containsKey(childName))
            {
                throw new ConfigurationException(node.getNodeName() +
                        " has 2 or more " + childName + " children. All" +
                        " children must be unique for this parent.");
            }
            else
            {
                requiredSet.remove(childName);
                map.put(childName, child);
            }
        }

        //Check all required are present
        if(!requiredSet.isEmpty())
        {
            String msg = "The following required children of " + node.getNodeName() +
                    " are not present: " + requiredSet;
            throw new ConfigurationException(msg);
        }

        return map;
    }
}