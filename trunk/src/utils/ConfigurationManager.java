package utils;

import java.io.File;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


//TODO: Have better error management. Currently throwing Error, which is a rather dirty hack.
/**
 * Allows retrieval of data from the config file.
 *
 * @author jak2 (Joshua Kaplan)
 */
public class ConfigurationManager {
    
    //A representation of the configuration from the XML file
    private static Configuration _config = null;
    
    //String constants that represent the tags used in the XML markup
    private final static String CONFIG = "CONFIG",
                                TEXT_NODE = "#text", COMMENT_NODE = "#comment",
                                ASSIGNMENTS = "ASSIGNMENTS", ASSIGNMENT = "ASSIGNMENT",
                                GRADERS = "GRADERS", GRADER = "GRADER",
                                TAS = "TAS", TA = "TA",
                                ADMINS = "ADMINS", ADMIN = "ADMIN",
                                NAME = "NAME", NUMBER = "NUMBER", TYPE = "TYPE",
                                POINTS = "POINTS", DQ = "DQ", TOTAL = "TOTAL",
                                OUTDATE = "OUTDATE", EARLY = "EARLY",
                                ONTIME="ONTIME", LATE = "LATE",
                                MONTH = "MONTH", DAY = "DAY", YEAR = "YEAR",
                                TIME = "TIME",
                                LOGIN = "login";

    //Common error message used
    private final static String ERROR_MSG = "XML not formatted properly, encountered node of name = ";

    /**
     * Exists so that the configuration file is only parsed once,
     * and only parsed if needed.
     *
     * @return
     */
    private static Configuration getInstance() {
        if (_config == null) {
            _config = processXML(Allocator.getConstants().getConfigFilePath());
        }
        return _config;
    }

    /**
     * The assignments as specified by the configuration manager.
     *
     * @return assignments
     */
    public static Iterable<Assignment> getAssignments() {
        return getInstance().Assignments;
    }

    /**
     * The grader logins as specified by the configuration manager.
     *
     * @return grader logins
     */
    public static Iterable<String> getGraderLogins() {
        return getInstance().Graders;
    }

    /**
     * The admin logins as specified by the configuration manager.
     *
     * @return admin logins
     */
    public static Iterable<String> getAdminLogins() {
        return getInstance().Admins;
    }

    /**
     * The ta logins as specified by the configuration manager.
     *
     * @return ta logins
     */
    public static Iterable<String> getTALogins() {
        return getInstance().TAs;
    }

    /**
     * Processes the XML file and returns a Configuration.
     *
     * @param XMLFilePath absolute file path to the XML configuration file
     * @return configuration
     */
    private static Configuration processXML(String XMLFilePath) {
        Configuration config = new Configuration();

        //Get XML as a document
        Document document = getDocument(XMLFilePath);

        //Get root node
        Node configNode = getRootNode(document);

        //Children
        assignChildrenAttributes(configNode, config);

        return config;
    }

    /**
     * Indicates whether a node should be skipped or not. A node should not be processed if it
     * is a text node or a comment node.
     *
     * @param node
     * @return whether a node should be skipped
     */
    private static boolean skipNode(Node node){
        return (node.getNodeName().equals(TEXT_NODE) || node.getNodeName().equals(COMMENT_NODE));
    }

    /**
     * Takes in the root node of the XML and the Configuration object that will represent
     * the markup and then takes the data from the XML and places it into the Configuration
     * object.
     *
     * @param configNode the root configuration node of the XML documentt
     * @param config the Configuration object that will represent the config XML
     */
    private static void assignChildrenAttributes(Node configNode, Configuration config) {
        NodeList configList = configNode.getChildNodes();
        for (int i = 0; i < configList.getLength(); i++) {
            Node currNode = configList.item(i);
            //Skip if appropriate
            if(skipNode(currNode)){
                continue;
            }
            //Assignments
            else if (currNode.getNodeName().equals(ASSIGNMENTS)) {
                processAssignments(currNode.getChildNodes(), config);
            }
            //Graders
            else if (currNode.getNodeName().equals(GRADERS)) {
                processLogins(currNode.getChildNodes(), GRADER, config.Graders);
            }
            //TAs
            else if (currNode.getNodeName().equals(TAS)) {
                processLogins(currNode.getChildNodes(), TA, config.TAs);
            }
            //Admins
            else if (currNode.getNodeName().equals(ADMINS)) {
                processLogins(currNode.getChildNodes(), ADMIN, config.Admins);
            }
            else {
                throw new Error(ERROR_MSG + currNode.getNodeName());
            }
        }
    }

    /**
     * Parses the assignments out of the XML file and assigns them to the Configuration.
     *
     * @param assignmentNodes NodeList of the assignment nodes
     * @param config the Configuration object that will represent the config XML
     */
    private static void processAssignments(NodeList assignmentNodes, Configuration config) {
        for (int i = 0; i < assignmentNodes.getLength(); i++) {
            Node currNode = assignmentNodes.item(i);
            if(skipNode(currNode)){
                continue;
            }
            else if (currNode.getNodeName().equals(ASSIGNMENT)) {
                addAssignment(currNode, config);
            }
            else {
                throw new Error(ERROR_MSG + currNode.getNodeName());
            }
        }
    }

    /**
     * Adds an individual assignment from the XML file and adds it to the Configuration.
     *
     * @param asgnNode Node of an assignment
     * @param config the Configuration object that will represent the config XML
     */
    private static void addAssignment(Node asgnNode, Configuration config) {
        Assignment asgn = new Assignment();

        asgn.Name = asgnNode.getAttributes().getNamedItem(NAME).getNodeValue();
        asgn.Number = Integer.valueOf(asgnNode.getAttributes().getNamedItem(NUMBER).getNodeValue());
        asgn.Type = AssignmentType.getInstance(asgnNode.getAttributes().getNamedItem(TYPE).getNodeValue());

        NodeList childrenNodes = asgnNode.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node currNode = childrenNodes.item(i);
            if(skipNode(currNode)){
                continue;
            }
            else if (currNode.getNodeName().equals(POINTS)) {
                for (int j = 0; j < currNode.getAttributes().getLength(); j++) {
                    Node attributeNode = currNode.getAttributes().item(j);
                    if (attributeNode.getNodeName().equals(DQ)) {
                        asgn.Points.DQ = Integer.valueOf(attributeNode.getNodeValue());
                    }
                    else if (attributeNode.getNodeName().equals(TOTAL)) {
                        asgn.Points.TOTAL = Integer.valueOf(attributeNode.getNodeValue());
                    }
                }
            }
            else if (currNode.getNodeName().equals(OUTDATE)) {
                asgn.Outdate = getCalendarFromNode(currNode);
            }
            else if (currNode.getNodeName().equals(EARLY)) {
                asgn.Early = getCalendarFromNode(currNode);
            }
            else if (currNode.getNodeName().equals(ONTIME)) {
                asgn.Ontime = getCalendarFromNode(currNode);
            }
            else if (currNode.getNodeName().equals(LATE)) {
                asgn.Late = getCalendarFromNode(currNode);
            }
            else {
                throw new Error(ERROR_MSG + currNode.getNodeName());
            }
        }

        config.Assignments.add(asgn);
    }

    /**
     * Parses the date information out of a node that has date information
     * (OUTDATE, EARLY, ONTIME, LATE) and returns a Calendar that represents
     * this.
     *
     * @param node Node with date information
     * @return Calendar instance representing this date information
     */
    private static Calendar getCalendarFromNode(Node node) {
        String month = "", day = "", year = "", time = "";

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            Node attributeNode = node.getAttributes().item(i);

            if (attributeNode.getNodeName().equals(MONTH)) {
                month = attributeNode.getNodeValue();
            }
            else if (attributeNode.getNodeName().equals(DAY)) {
                day = attributeNode.getNodeValue();
            }
            else if (attributeNode.getNodeName().equals(YEAR)) {
                year = attributeNode.getNodeValue();
            }
            else if (attributeNode.getNodeName().equals(TIME)) {
                time = attributeNode.getNodeValue();
            }
            else {
                throw new Error(ERROR_MSG + attributeNode.getNodeName());
            }
        }

        return Allocator.getGeneralUtilities().getCalendar(year, month, day, time);
    }

    /**
     * Adds the logins for a given tag to the vector of logins passed in.
     *
     * @param loginNodes NodeList containing the nodes of logins
     * @param tag The tag to check, such as GRADER or TA
     * @param logins The vector that the parsed out logins will be added to
     */
    private static void processLogins(NodeList loginNodes, String tag, Vector<String> logins) {
        for (int j = 0; j < loginNodes.getLength(); j++) {
            Node graderNode = loginNodes.item(j);
            if(skipNode(graderNode)){
                continue;
            }
            else if (graderNode.getNodeName().equals(tag)) {
                String login = graderNode.getAttributes().getNamedItem(LOGIN).getNodeValue();
                logins.add(login);
            }
            else {
                throw new Error(ERROR_MSG + graderNode.getNodeName());
            }
        }
    }

    /**
     * Get the Document representing the XML file specified by the string passed in.
     *
     * @param XMLFilePath Absolute file path to the XML file
     * @return Document representing the XML file
     */
    private static Document getDocument(String XMLFilePath) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(XMLFilePath));
        }
        catch (Exception e) {
            throw new Error("Exception thrown during parsing, " + XMLFilePath + " is illegally formatted");
        }

        return document;
    }

    /**
     * Returns the root CONFIG node. If it cannot be found, an error is printed and null is returned.
     *
     * @param document Document representing this XML file
     * @return root CONFIG node
     */
    private static Node getRootNode(Document document) {
        Node rubricNode = document.getDocumentElement();

        if (!rubricNode.getNodeName().equals(CONFIG)) {
            System.err.println("XML not formatted properly. The root " + CONFIG + " node cannot be found.");
            return null;
        }

        return rubricNode;
    }
}
