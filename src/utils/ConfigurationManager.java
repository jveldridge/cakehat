package utils;

import utils.Utils;
import utils.Constants;
import java.io.File;
import java.util.Calendar;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import cs015.tasupport.utils.Utils;
public class ConfigurationManager {
    //A representation of the configuration from the XML file

    private static Configuration _config = null;

    /**
     * Exists so that the configuration file is only parsed once,
     * and only parsed if needed.
     *
     * @return
     */
    private static Configuration getInstance() {
        if (_config == null) {
            _config = processXML(Constants.CONFIG_FILE_PATH);
        }
        return _config;
    }

    public static Assignment[] getAssignments() {
        return getInstance().Assignments.toArray(new Assignment[0]);
    }

    public static String[] getGraderLogins() {
        return getInstance().Graders.toArray(new String[0]);
    }

    public static String[] getAdminLogins() {
        return getInstance().Admins.toArray(new String[0]);
    }

    public static String[] getTALogins() {
        return getInstance().TAs.toArray(new String[0]);
    }

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

    private static void assignChildrenAttributes(Node configNode, Configuration config) {
        NodeList configList = configNode.getChildNodes();
        for (int i = 0; i < configList.getLength(); i++) {
            Node currNode = configList.item(i);
            //Skip if an empty text node
            if (currNode.getNodeName().equals("#text") || currNode.getNodeName().equals("#comment")) {
                continue;
            } //Assignments
            else if (currNode.getNodeName().equals("ASSIGNMENTS")) {
                processAssignments(currNode.getChildNodes(), config);
            } //Graders
            else if (currNode.getNodeName().equals("GRADERS")) {
                processLogins(currNode.getChildNodes(), config, "GRADER", config.Graders);
            } //TAs
            else if (currNode.getNodeName().equals("TAS")) {
                processLogins(currNode.getChildNodes(), config, "TA", config.TAs);
            } //Admins
            else if (currNode.getNodeName().equals("ADMINS")) {
                processLogins(currNode.getChildNodes(), config, "ADMIN", config.Admins);
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + currNode.getNodeName());
            }
        }
    }

    private static void processAssignments(NodeList assignmentNodes, Configuration config) {
        for (int i = 0; i < assignmentNodes.getLength(); i++) {
            Node currNode = assignmentNodes.item(i);
            if (currNode.getNodeName().equals("#text") || currNode.getNodeName().equals("#comment")) {
                continue;
            } else if (currNode.getNodeName().equals("ASSIGNMENT")) {
                addAssignment(currNode, config);
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + currNode.getNodeName());
            }
        }
    }

    private static void addAssignment(Node asgnNode, Configuration config) {
        Assignment asgn = new Assignment();

        asgn.Name = asgnNode.getAttributes().getNamedItem("NAME").getNodeValue();
        asgn.Number = Integer.valueOf(asgnNode.getAttributes().getNamedItem("NUMBER").getNodeValue());
        asgn.Type = AssignmentType.getInstance(asgnNode.getAttributes().getNamedItem("TYPE").getNodeValue());

        //System.out.println(asgn.Name + " - " + asgn.Number + " - " + asgn.Type);

        NodeList childrenNodes = asgnNode.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node currNode = childrenNodes.item(i);
            if (currNode.getNodeName().equals("#text")) {
                continue;
            } else if (currNode.getNodeName().equals("POINTS")) {
                for (int j = 0; j < currNode.getAttributes().getLength(); j++) {
                    Node attributeNode = currNode.getAttributes().item(j);
                    if (attributeNode.getNodeName().equals("DQ")) {
                        asgn.Points.DQ = Integer.valueOf(attributeNode.getNodeValue());
                    //System.out.print("DQ = " + asgn.Points.DQ + ", ");
                    } else if (attributeNode.getNodeName().equals("TOTAL")) {
                        asgn.Points.TOTAL = Integer.valueOf(attributeNode.getNodeValue());
                    //System.out.println("TOTAL = " + asgn.Points.TOTAL);
                    }
                }
            } else if (currNode.getNodeName().equals("OUTDATE")) {
                asgn.Outdate = getCalendarFromNode(currNode);
            //System.out.println("Outdate = " + Utils.getCalendarAsString(asgn.Outdate));
            } else if (currNode.getNodeName().equals("EARLY")) {
                asgn.Early = getCalendarFromNode(currNode);
            //System.out.println("Early = " + Utils.getCalendarAsString(asgn.Early));
            } else if (currNode.getNodeName().equals("ONTIME")) {
                asgn.Ontime = getCalendarFromNode(currNode);
            //System.out.println("Ontime = " + Utils.getCalendarAsString(asgn.Ontime));
            } else if (currNode.getNodeName().equals("LATE")) {
                asgn.Late = getCalendarFromNode(currNode);
            //System.out.println("Late = " + Utils.getCalendarAsString(asgn.Late));
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + currNode.getNodeName());
            }
        }
        //System.out.println("");

        config.Assignments.add(asgn);
    }

    private static Calendar getCalendarFromNode(Node node) {
        String month = "", day = "", year = "", time = "";

        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            Node attributeNode = node.getAttributes().item(i);

            if (attributeNode.getNodeName().equals("MONTH")) {
                month = attributeNode.getNodeValue();
            } else if (attributeNode.getNodeName().equals("DAY")) {
                day = attributeNode.getNodeValue();
            } else if (attributeNode.getNodeName().equals("YEAR")) {
                year = attributeNode.getNodeValue();
            } else if (attributeNode.getNodeName().equals("TIME")) {
                time = attributeNode.getNodeValue();
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + attributeNode.getNodeName());
            }
        }

        return Utils.getCalendar(year, month, day, time);
    }

    private static void processLogins(NodeList graderNodes, Configuration config, String tag, Vector<String> logins) {
        for (int j = 0; j < graderNodes.getLength(); j++) {
            Node graderNode = graderNodes.item(j);
            if (graderNode.getNodeName().equals("#text")) {
                continue;
            } else if (graderNode.getNodeName().equals(tag)) {
                String login = graderNode.getAttributes().getNamedItem("login").getNodeValue();
                //System.out.println(tag + ": " + login);
                logins.add(login);
            } else {
                throw new Error("XML not formatted properly, encountered node of name = " + graderNode.getNodeName());
            }
        }
    }

    private static Document getDocument(String XMLFilePath) {
        Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(XMLFilePath));
        } catch (Exception e) {
            throw new Error("Exception thrown during parsing, " + XMLFilePath + " is illegally formatted");
        }

        return document;
    }

    private static Node getRootNode(Document document) {
        Node rubricNode = document.getDocumentElement();
        if (!rubricNode.getNodeName().equals("CONFIG")) {
            System.out.println("XML not formatted properly.");
            System.exit(0);
        }

        return rubricNode;
    }
}