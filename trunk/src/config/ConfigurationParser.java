package config;

import java.io.File;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import utils.Allocator;

/**
 * Parses the configuration file.
 * 
 * @author jak2
 */
public class ConfigurationParser
{
    //For testing purposes
    //Although this should be defined as a specific location in relation to a course directory
    //As in, not configurable by by the config file
    private final static String CONFIG_FILE_PATH = "/course/cs015/grading/bin/2009/config_new_spec.xml";

    //String constants that represent the tags used in the XML markup
    private final static String TEXT_NODE = "#text", COMMENT_NODE = "#comment",

                                CONFIG = "CONFIG",
          
                                ASSIGNMENTS = "ASSIGNMENTS", TAS = "TAS",

                                TA = "TA", LOGIN = "LOGIN", DEFAULT_GRADER = "DEFAULT-GRADER", ADMIN = "ADMIN",
                                BLACKLIST = "BLACKLIST", STUDENT = "STUDENT",

                                ASSIGNMENT = "ASSIGNMENT", NAME = "NAME", NUMBER = "NUMBER",
                                RUBRIC="RUBRIC", DEDUCTIONS="DEDUCTIONS", LOCATION = "LOCATION",


                                PART = "PART", LAB_NUMBER="LAB-NUMBER", TYPE="TYPE", POINTS="POINTS", LANGUAGE="LANGUAGE",
                                LATE_POLICY="LATE-POLICY", UNITS="UNITS",

                                EARLY = "EARLY", ONTIME="ONTIME", LATE = "LATE",
                                MONTH = "MONTH", DAY = "DAY", YEAR = "YEAR", TIME = "TIME",
                                VALUE = "VALUE",
                                RUN = "RUN", DEMO = "DEMO", TESTER = "TESTER", MODE = "MODE",
                                PROPERTY = "PROPERTY", KEY = "KEY";


    static Configuration parse() throws ConfigurationException
    {
        Configuration config = new Configuration();

        //Get XML as a document
        Document document = getDocument(CONFIG_FILE_PATH);

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
    private static boolean skipNode(Node node)
    {
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
    private static void assignChildrenAttributes(Node configNode, Configuration config) throws ConfigurationException
    {
        NodeList configList = configNode.getChildNodes();
        for (int i = 0; i < configList.getLength(); i++)
        {
            Node currNode = configList.item(i);
            //Skip if appropriate
            if(skipNode(currNode))
            {
                continue;
            }
            //Assignments
            else if (currNode.getNodeName().equals(ASSIGNMENTS))
            {
                processAssignments(currNode.getChildNodes(), config);
            }
            //TAs
            else if (currNode.getNodeName().equals(TAS))
            {
                processTAs(currNode.getChildNodes(), config);
            }
            else
            {
                throw new ConfigurationException("Unknown CONFIG child: "+ currNode.getNodeName() +
                                                 ", supported children are " + ASSIGNMENTS + " and " + TAS);
            }
        }
    }

    private static void processTAs(NodeList taNodes, Configuration config) throws ConfigurationException
    {
        for (int i = 0; i < taNodes.getLength(); i++)
        {
            Node taNode = taNodes.item(i);

            //Skip if appropriate
            if(skipNode(taNode))
            {
                continue;
            }

            String taLogin = taNode.getAttributes().getNamedItem(LOGIN).getNodeValue();
            boolean isDefaultGrader = Boolean.getBoolean(taNode.getAttributes().getNamedItem(DEFAULT_GRADER).getNodeValue());
            boolean isAdmin = Boolean.getBoolean(taNode.getAttributes().getNamedItem(ADMIN).getNodeValue());

            TA ta = new TA(taLogin, isDefaultGrader, isAdmin);

            NodeList childNodes = taNode.getChildNodes();
            for(int j = 0; j < childNodes.getLength(); j++)
            {
                Node childNode = childNodes.item(j);

                //Skip if appropriate
                if(skipNode(childNode))
                {
                    continue;
                }
                //Blacklist
                else if (childNode.getNodeName().equals(BLACKLIST))
                {
                    NodeList blacklistNodes = childNode.getChildNodes();
                    for(int k = 0; k < blacklistNodes.getLength(); k++)
                    {
                        Node blacklistChildNode = blacklistNodes.item(k);

                        //Skip if appropriate
                        if(skipNode(blacklistChildNode))
                        {
                            continue;
                        }
                        else if(blacklistChildNode.getNodeName().equals(STUDENT))
                        {
                            String studentLogin = blacklistChildNode.getAttributes().getNamedItem(LOGIN).getNodeValue();
                            ta.addStudentToBlacklist(studentLogin);
                        }
                        else
                        {
                            throw new ConfigurationException("Unknown BLACKLIST child: " + blacklistChildNode.getNodeName() + ", only " + STUDENT + " is a supported child.");
                        }
                    }
                }
                else
                {
                    throw new ConfigurationException("Unknown TA child: " + childNode.getNodeName() + ", only " + BLACKLIST + " is a supported child.");
                }
            }

            config.addTA(ta);
        }
    }

    /**
     * Parses the assignments out of the XML file and assigns them to the Configuration.
     *
     * @param assignmentNodes NodeList of the assignment nodes
     * @param config the Configuration object that will represent the config XML
     */
    private static void processAssignments(NodeList assignmentNodes, Configuration config) throws ConfigurationException
    {
        for (int i = 0; i < assignmentNodes.getLength(); i++)
        {
            Node currNode = assignmentNodes.item(i);
            if(skipNode(currNode))
            {
                continue;
            }
            else if (currNode.getNodeName().equals(ASSIGNMENT))
            {
                addAssignment(currNode, config);
            }
            else
            {
                throw new ConfigurationException("Unsupported ASSIGNMENTS child: " + currNode.getNodeName() +
                                                 ", only " + ASSIGNMENT + " is supported.");
            }
        }
    }

    /**
     * Adds an individual assignment from the XML file and adds it to the Configuration.
     *
     * @param asgnNode Node of an assignment
     * @param config the Configuration object that will represent the config XML
     */
    private static void addAssignment(Node asgnNode, Configuration config) throws ConfigurationException
    {
        String name = asgnNode.getAttributes().getNamedItem(NAME).getNodeValue();
        int number = Integer.valueOf(asgnNode.getAttributes().getNamedItem(NUMBER).getNodeValue());

        Assignment asgn = new Assignment(name, number);

        NodeList childrenNodes = asgnNode.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++)
        {
            Node currNode = childrenNodes.item(i);
            if(skipNode(currNode))
            {
                continue;
            }
            //Rubric
            else if (currNode.getNodeName().equals(RUBRIC))
            {
                String location = currNode.getAttributes().getNamedItem(LOCATION).getNodeValue();
                asgn.setRubric(location);
            }
            //Deduction list
            else if (currNode.getNodeName().equals(DEDUCTIONS))
            {
                String location = currNode.getAttributes().getNamedItem(LOCATION).getNodeValue();
                asgn.setDeductionList(location);
            }
            //Part
            else if (currNode.getNodeName().equals(PART))
            {
                processPart(currNode, asgn);
            }
            else
            {
                throw new ConfigurationException("Unsupported ASSIGNMENT child: " + currNode.getNodeName() +
                                                 ", supported children are: " + RUBRIC + ", " + DEDUCTIONS +
                                                 ", and " + PART);
            }
        }

        config.addAssignment(asgn);
    }

    private static void processPart(Node partNode, Assignment asgn) throws ConfigurationException
    {
        String name = partNode.getAttributes().getNamedItem(NAME).getNodeValue();
        int points = Integer.valueOf(partNode.getAttributes().getNamedItem(POINTS).getNodeValue());
        String type = partNode.getAttributes().getNamedItem(TYPE).getNodeValue();

        if(type.equalsIgnoreCase("NON-CODE"))
        {
            asgn.addNonCodePart(new NonCodePart(asgn, name, points));
        }
        else if(type.equalsIgnoreCase("CODE"))
        {
            processCodePart(partNode, asgn, name, points);
        }
        else if(type.equalsIgnoreCase("LAB"))
        {
            int labNumber = Integer.valueOf(partNode.getAttributes().getNamedItem(LAB_NUMBER).getNodeValue());

            asgn.addLabPart(new LabPart(asgn, name, points, labNumber));
        }
        else
        {
            throw new ConfigurationException("Encountered PART of unsupported TYPE = " + type);
        }
    }

    private static void processCodePart(Node partNode, Assignment asgn, String name, int points) throws ConfigurationException
    {
        CodePart part = null;
        
        String language = partNode.getAttributes().getNamedItem(LANGUAGE).getNodeValue();

        //Create the appropriate subclass of CodePart based on the language
        if(language.equalsIgnoreCase("Java"))
        {
            part = new JavaPart(asgn, name, points);
        }
        else if(language.equalsIgnoreCase("C"))
        {
            throw new UnsupportedOperationException("C is not yet supported");
        }
        else if(language.equalsIgnoreCase("C++"))
        {
            throw new UnsupportedOperationException("C++ is not yet supported");
        }
        else if(language.equalsIgnoreCase("Matlab"))
        {
            throw new UnsupportedOperationException("Matlab is not yet supported");
        }
        else
        {
            throw new ConfigurationException("Encountered CODE PART of unsupported LANGUAGE = " + language);
        }

        //Process out LATE-POLICY, RUN, DEMO, TESTER tags
        NodeList childrenNodes = partNode.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++)
        {
            Node childNode = childrenNodes.item(i);

            if(skipNode(childNode))
            {
                continue;
            }
            //Time information (LATE-POLICY)
            else if(childNode.getNodeName().equals(LATE_POLICY))
            {
                processTimeInfo(childNode, part);
            }
            //RUN
            else if(childNode.getNodeName().equals(RUN))
            {
                //Run mode
                String mode = childNode.getAttributes().getNamedItem(MODE).getNodeValue();
                part.setRunMode(mode);

                //Properties
                NodeList propertyNodes = childNode.getChildNodes();
                for (int j = 0; j < propertyNodes.getLength(); j++)
                {
                    Node propertyNode = propertyNodes.item(j);

                    if(skipNode(propertyNode))
                    {
                        continue;
                    }
                    else if(propertyNode.getNodeName().equals(PROPERTY))
                    {
                        String key = propertyNode.getAttributes().getNamedItem(KEY).getNodeValue();
                        String value = propertyNode.getAttributes().getNamedItem(VALUE).getNodeValue();
                        part.setRunProperty(key, value);
                    }
                    else
                    {
                        throw new ConfigurationException("RUN may only have children of type PROPERTY," +
                                                         "encountered tag of " + propertyNode.getNodeName());
                    }
                }

            }
            //DEMO
            else if(childNode.getNodeName().equals(DEMO))
            {
                //Demo mode
                String mode = childNode.getAttributes().getNamedItem(MODE).getNodeValue();
                part.setDemoMode(mode);

                //Properties
                NodeList propertyNodes = childNode.getChildNodes();
                for (int j = 0; j < propertyNodes.getLength(); j++)
                {
                    Node propertyNode = propertyNodes.item(j);

                    if(skipNode(propertyNode))
                    {
                        continue;
                    }
                    else if(propertyNode.getNodeName().equals(PROPERTY))
                    {
                        String key = propertyNode.getAttributes().getNamedItem(KEY).getNodeValue();
                        String value = propertyNode.getAttributes().getNamedItem(VALUE).getNodeValue();
                        part.setDemoProperty(key, value);
                    }
                    else
                    {
                        throw new ConfigurationException("DEMO may only have children of type PROPERTY," +
                                                         "encountered tag of " + propertyNode.getNodeName());
                    }
                }

            }
            //TESTER
            else if(childNode.getNodeName().equals(TESTER))
            {
                //Tester mode
                String mode = childNode.getAttributes().getNamedItem(MODE).getNodeValue();
                part.setTesterMode(mode);

                //Properties
                NodeList propertyNodes = childNode.getChildNodes();
                for (int j = 0; j < propertyNodes.getLength(); j++)
                {
                    Node propertyNode = propertyNodes.item(j);

                    if(skipNode(propertyNode))
                    {
                        continue;
                    }
                    else if(propertyNode.getNodeName().equals(PROPERTY))
                    {
                        String key = propertyNode.getAttributes().getNamedItem(KEY).getNodeValue();
                        String value = propertyNode.getAttributes().getNamedItem(VALUE).getNodeValue();
                        part.setTesterProperty(key, value);
                    }
                    else
                    {
                        throw new ConfigurationException("TESTER may only have children of type PROPERTY," +
                                                         "encountered tag of " + propertyNode.getNodeName());
                    }
                }
            }
        }
        
        asgn.addCodePart(part);
    }

    private static void processTimeInfo(Node timeNode, CodePart part) throws ConfigurationException
    {
        LatePolicy policy = LatePolicy.valueOf(timeNode.getAttributes().getNamedItem(TYPE).getNodeValue());
        GradeUnits units = GradeUnits.valueOf(timeNode.getAttributes().getNamedItem(UNITS).getNodeValue());

        TimeInformation timeInfo = new TimeInformation();
        timeInfo.setGradeUnits(units);
        timeInfo.setLatePolicy(policy);

        NodeList childrenNodes = timeNode.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++)
        {
            Node childNode = childrenNodes.item(i);

            if(skipNode(childNode))
            {
                continue;
            }
            else if (childNode.getNodeName().equals(EARLY))
            {
                CalendarValue calval = getTimeFromNode(childNode);
                timeInfo.setEarlyDate(calval.cal);
                timeInfo.setEarlyValue(calval.val);
            }
            else if (childNode.getNodeName().equals(ONTIME))
            {
                CalendarValue calval = getTimeFromNode(childNode);
                timeInfo.setOntimeDate(calval.cal);
                timeInfo.setOntimeValue(calval.val);
            }
            else if (childNode.getNodeName().equals(LATE))
            {
                CalendarValue calval = getTimeFromNode(childNode);
                timeInfo.setLateDate(calval.cal);
                timeInfo.setLateValue(calval.val);
            }
            else
            {
                throw new ConfigurationException("Encountered unknown time tag of = " + childNode.getNodeName());
            }
        }

        part.setTimeInfo(timeInfo);
    }


    private static class CalendarValue
    {
        Calendar cal;
        int val;

        CalendarValue(Calendar cal, int val)
        {
            this.cal = cal;
            this.val = val;
        }
    }


    /**
     * Parses the date information out of a node that has date information
     * and returns a Calendar that represents
     * this.
     *
     * @param node Node with date information
     * @return Calendar instance representing this date information
     */
    private static CalendarValue getTimeFromNode(Node node) throws ConfigurationException
    {
        String month = "", day = "", year = "", time = "";
        int val = 0;

        for (int i = 0; i < node.getAttributes().getLength(); i++)
        {
            Node attributeNode = node.getAttributes().item(i);

            if (attributeNode.getNodeName().equals(MONTH))
            {
                month = attributeNode.getNodeValue();
            }
            else if (attributeNode.getNodeName().equals(DAY))
            {
                day = attributeNode.getNodeValue();
            }
            else if (attributeNode.getNodeName().equals(YEAR))
            {
                year = attributeNode.getNodeValue();
            }
            else if (attributeNode.getNodeName().equals(TIME))
            {
                time = attributeNode.getNodeValue();
            }
            else if (attributeNode.getNodeName().equals(VALUE))
            {
                val = Integer.valueOf(attributeNode.getNodeValue());
            }
            else
            {
                throw new ConfigurationException("Could not process date information from " + attributeNode.getNodeName());
            }
        }

        Calendar cal = Allocator.getGeneralUtilities().getCalendar(year, month, day, time);

        return new ConfigurationParser.CalendarValue(cal, val);
    }

    /**
     * Get the Document representing the XML file specified by the string passed in.
     *
     * @param XMLFilePath Absolute file path to the XML file
     * @return Document representing the XML file
     */
    private static Document getDocument(String XMLFilePath) throws ConfigurationException
    {
        Document document = null;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(new File(XMLFilePath));
        }
        catch (Exception e)
        {
            throw new ConfigurationException("Exception thrown during parsing, " + XMLFilePath + " is illegally formatted");
        }

        return document;
    }

    /**
     * Returns the root CONFIG node. If it cannot be found, an error is printed and null is returned.
     *
     * @param document Document representing this XML file
     * @return root CONFIG node
     */
    private static Node getRootNode(Document document) throws ConfigurationException
    {
        Node rubricNode = document.getDocumentElement();

        if (!rubricNode.getNodeName().equals(CONFIG))
        {
            throw new ConfigurationException("XML not formatted properly. The root " + CONFIG + " node cannot be found.");
        }

        return rubricNode;
    }

}