package cakehat.gml;

import cakehat.gml.InMemoryGML.Subsection;
import cakehat.gml.InMemoryGML.Section;
import org.w3c.dom.NodeList;
import cakehat.database.Group;
import cakehat.database.assignment.Part;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import static cakehat.gml.GMLConstants.*;

/**
 *
 * @author Hannah
 */
public class GMLParser {
    
    private GMLParser() { }
    
    /**
     * Parses a GML file into its in memory representation - an {@link InMemoryGML} object.
     *
     * @param gmlFile the GML file
     * @param part the Part associated with this GML file
     * @param the Group associated with this GML file; null if the file is a template
     * @return
     */
    public static InMemoryGML parse(File gmlFile, Part part, Group group) throws GradingSheetException {
        InMemoryGML gml = new InMemoryGML(part, group);

        //Get XML as a document
        Document document = getDocument(gmlFile);

        //Get root node
        Node rootNode = getRootNode(document);
        
        // parse root node to set version and type
        parseRootNode(rootNode, gml);

        //Children
        assignChildrenAttributes(rootNode, gml);

        return gml;
    }
    
    private static Document getDocument(File gmlFile) throws GradingSheetException {
        //Check if file exists
        if(!gmlFile.exists()) {
            throw new GradingSheetException("GML file could not be read, location specified: " + gmlFile.getAbsolutePath());
        }

        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(gmlFile);
        }
        catch (Exception e) {
            throw new GMLException("Exception thrown during parsing, " +
                    gmlFile.getAbsolutePath() + " is illegally formatted.", e);
        }

        return document;
    }
    
    private static Node getRootNode(Document document) throws GMLException {
        Node rootNode = document.getDocumentElement();
        if (!rootNode.getNodeName().equals(GRADING_SHEET)) {
            throw new GMLException("Expected root node " + GRADING_SHEET + ", encountered: " + rootNode.getNodeName());
        }
        
        return rootNode;
    }
    
    private static void parseRootNode(Node root, InMemoryGML gml) throws GMLException {
        NamedNodeMap attrMap = root.getAttributes();
                
        if (attrMap.getNamedItem(GML_VERSION) == null) {
            throw new GMLException(GRADING_SHEET + " section must have " + GML_VERSION + " attribute.");
        }
        if (attrMap.getNamedItem(TYPE) == null) {
            throw new GMLException(GRADING_SHEET + " must have " + TYPE + " attribute.");
        }
        
        for (int i = 0; i < attrMap.getLength(); i++) {
            Node attr = attrMap.item(i);
            if (attr.getNodeName().equals(GML_VERSION)) {
                gml.setVersion(attr.getNodeValue());
            }
            else if (attr.getNodeName().equals(TYPE)) {
                gml.setType(attr.getNodeValue());
            }
            else {
                throw new GMLException(GRADING_SHEET + " node must only have " + TYPE + " and " + GML_VERSION + " attributes.");
            }
        }
    }
    
    /**
     * Indicates whether a node should be skipped or not. A node should not be
     * processed if it is a text node or a comment node.
     *
     * @param node
     * @return whether a node should be skipped
     */
    private static boolean skipNode(Node node) {
        return (node.getNodeName().equals(TEXT_NODE) || node.getNodeName().equals(COMMENT_NODE));
    }
    
    private static void assignChildrenAttributes(Node rubricNode, InMemoryGML gml) throws GMLException, GradingSheetException {
        NodeList rubricList = rubricNode.getChildNodes();
        
        for (int i = 0; i < rubricList.getLength(); i++) {
            Node currNode = rubricList.item(i);
            //Skip if necessary
            if(skipNode(currNode)) {
                continue;
            }
            //Section
            else if (currNode.getNodeName().equals(SECTION)) {
                parseSection(currNode, gml);
            }
            else {
                throw new GMLException(GRADING_SHEET, currNode, SECTION);
            }
        }
    }
    
    private static void parseSection(Node sectionNode, InMemoryGML gml) throws GMLException, GradingSheetException {
        Section section;
        
        Node nameNode = sectionNode.getAttributes().getNamedItem(NAME);
        if(nameNode != null) {
            section = gml.addSection(nameNode.getNodeValue());
        }
        else {
            throw new GMLException(SECTION + " must have " + NAME + " attribute.");
        }
        
        NodeList subsectionList = sectionNode.getChildNodes();
        
        for (int i = 0; i < subsectionList.getLength(); i++) {
            Node childNode = subsectionList.item(i);
            if (skipNode(childNode)) {
                continue;
            }
            else if (childNode.getNodeName().equals(SUBSECTION)) {
                parseSubsection(childNode, section);
            }
            else if (childNode.getNodeName().equals(COMMENTS)) {
                if (section.getComment() == null) {
                    section.setComment(childNode.getTextContent());
                }
                else {
                    throw new GMLException("Only one " + COMMENTS + " node is allowed per " + SECTION + ".");
                }
            }
            else {
                throw new GMLException("Illegal node encountered : " + childNode.getNodeName() + ".");
            }
        }
    }
    
    private static void parseSubsection(Node subsectionNode, Section section) throws GMLException, GradingSheetException {
        String name = "";
        double outof = 0, earned = 0;

        NamedNodeMap attrMap = subsectionNode.getAttributes();
                
        if (attrMap.getNamedItem(NAME) == null) {
            throw new GMLException(SUBSECTION + " must have " + NAME + " attribute.");
        }
        if (attrMap.getNamedItem(OUTOF) == null) {
            throw new GMLException(SUBSECTION + " must have " + OUTOF + " attribute.");
        }
        
        for (int i = 0; i < attrMap.getLength(); i++) {
            Node attr = attrMap.item(i);
            
            if (attr.getNodeName().equals(NAME)) {
                name = attr.getNodeValue();
            }
            else if (attr.getNodeName().equals(OUTOF)) {
                outof = Double.valueOf(attr.getNodeValue());
            }
            else if (attr.getNodeName().equals(EARNED)) {
                earned = Double.valueOf(attr.getNodeValue());
            }
            else {
                throw new GMLException("Illegal attribute encountered for " + SUBSECTION + " with name " + name + ".");
            }
        }

        //Add subsections
        Subsection subsection = section.addSubsection(name, earned, outof);

        //Detail children and comment children (if they exist)
        NodeList nodeList = subsectionNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node childNode = nodeList.item(i);
            
            if(skipNode(childNode)) {
                continue;
            }
            else if (childNode.getNodeName().equals(DETAIL)) {
                subsection.addDetail(childNode.getTextContent());
            }
        }
    }
}