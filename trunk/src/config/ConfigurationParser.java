package config;

import java.io.File;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
          
                                ASSIGNMENTS = "ASSIGNMENTS", TAS = "TAS", DEFAULTS = "DEFAULTS", EMAIL = "EMAIL",

                                COURSE = "COURSE", LENIENCY = "LENIENCY",

                                TA = "TA", DEFAULT_GRADER = "DEFAULT-GRADER", ADMIN = "ADMIN", HTA = "HTA",

                                NOTIFY = "NOTIFY", ADDRESS = "ADDRESS",
                                SEND_FROM = "SEND-FROM", LOGIN = "LOGIN", PASSWORD = "PASSWORD",
                                CERT_PATH = "CERT-PATH", CERT_PASSWORD = "CERT-PASSWORD",

                                ASSIGNMENT = "ASSIGNMENT", NAME = "NAME", NUMBER = "NUMBER",
                                RUBRIC="RUBRIC", DEDUCTIONS="DEDUCTIONS", LOCATION = "LOCATION",

                                PART = "PART", LAB_NUMBER="LAB-NUMBER", TYPE="TYPE", POINTS="POINTS", LANGUAGE="LANGUAGE",
                                LATE_POLICY="LATE-POLICY", UNITS="UNITS",

                                EARLY = "EARLY", ONTIME="ONTIME", LATE = "LATE",
                                MONTH = "MONTH", DAY = "DAY", YEAR = "YEAR", TIME = "TIME",
                                VALUE = "VALUE",
                                RUN = "RUN", DEMO = "DEMO", TESTER = "TESTER", MODE = "MODE",
                                PROPERTY = "PROPERTY", KEY = "KEY";

    /**
     * Parses the configuration file. 
     *
     * @return config
     * @throws ConfigurationException
     */
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
     * Indicates whether a node should be skipped or not. A node should not be
     * processed if it is a text node or a comment node.
     *
     * @param node
     * @return whether a node should be skipped
     */
    private static boolean skipNode(Node node)
    {
        return (node.getNodeName().equals(TEXT_NODE) || node.getNodeName().equals(COMMENT_NODE));
    }

    /**
     * Takes in the root node of the XML and the Configuration object that will
     * represent the markup and then takes the data from the XML and places it
     * into the Configuration object.
     *
     * @param configNode the root configuration node of the XML documentt
     * @param config the Configuration object that will represent the config XML
     * @throws ConfigurationException
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
            //DEFAULTS
            else if (currNode.getNodeName().equals(DEFAULTS))
            {
                processDefaults(currNode.getChildNodes(), config);
            }
            //TAS
            else if (currNode.getNodeName().equals(TAS))
            {
                processTAs(currNode.getChildNodes(), config);
            }
            //EMAIL
            else if (currNode.getNodeName().equals(EMAIL))
            {
                processEmail(currNode.getChildNodes(), config);
            }
            else
            {
                throw new ConfigurationException(CONFIG, currNode, ASSIGNMENTS, DEFAULTS, TAS, EMAIL);
            }
        }
    }

    private static void processDefaults(NodeList defaultNodes, Configuration config) throws ConfigurationException
    {
        for (int i = 0; i < defaultNodes.getLength(); i++)
        {
            Node defaultNode = defaultNodes.item(i);

            //Skip if appropriate
            if(skipNode(defaultNode))
            {
                continue;
            }
            else if (defaultNode.getNodeName().equals(COURSE))
            {
                String course = defaultNode.getFirstChild().getNodeValue();
                config.setCourse(course);
            }
            else if (defaultNode.getNodeName().equals(LENIENCY))
            {
                int leniency = Integer.parseInt(defaultNode.getFirstChild().getNodeValue());
                config.setLeniency(leniency);
            }
            else
            {
                throw new ConfigurationException(DEFAULTS, defaultNode, COURSE, LENIENCY);
            }
        }
    }

/**
     * Parses out the TA information.
     *
     * @param taNodes
     * @param config
     * @throws ConfigurationException
     */
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
            //TA
            else if(taNode.getNodeName().equals(TA))
            {
                String taLogin = taNode.getAttributes().getNamedItem(LOGIN).getNodeValue();
                boolean isDefaultGrader = Boolean.parseBoolean(taNode.getAttributes().getNamedItem(DEFAULT_GRADER).getNodeValue());
                boolean isAdmin = Boolean.parseBoolean(taNode.getAttributes().getNamedItem(ADMIN).getNodeValue());
                boolean isHTA = Boolean.parseBoolean(taNode.getAttributes().getNamedItem(HTA).getNodeValue());

                config.addTA(new TA(taLogin, isDefaultGrader, isAdmin, isHTA));
            }
            else
            {
                throw new ConfigurationException(TAS, taNode, TA);
            }
        }
    }


    private static void processEmail(NodeList emailNodes, Configuration config) throws ConfigurationException
    {
        for (int i = 0; i < emailNodes.getLength(); i++)
        {
            Node emailNode = emailNodes.item(i);

            //Skip if appropriate
            if(skipNode(emailNode))
            {
                continue;
            }
            else if (emailNode.getNodeName().equals(NOTIFY))
            {
                NodeList notifyNodes = emailNode.getChildNodes();
                for(int j = 0; j < notifyNodes.getLength(); j++)
                {
                    Node notifyNode = notifyNodes.item(j);
                    if(skipNode(notifyNode))
                    {
                        continue;
                    }
                    else if(notifyNode.getNodeName().equals(ADDRESS))
                    {
                        String address = notifyNode.getFirstChild().getNodeValue();
                        config.addNotifyAddress(address);
                    }
                    else
                    {
                        throw new ConfigurationException(NOTIFY, notifyNode, ADDRESS);
                    }
                }
            }
            else if (emailNode.getNodeName().equals(SEND_FROM))
            {
                EmailAccount account = new EmailAccount();

                NodeList sendFromNodes = emailNode.getChildNodes();
                for(int j = 0; j < sendFromNodes.getLength(); j++)
                {
                    Node sendFromNode = sendFromNodes.item(j);

                    if(skipNode(sendFromNode))
                    {
                        continue;
                    }
                    else if(sendFromNode.getNodeName().equals(LOGIN))
                    {
                        String login = sendFromNode.getFirstChild().getNodeValue();
                        account.setLogin(login);
                    }
                    else if(sendFromNode.getNodeName().equals(PASSWORD))
                    {
                        String password = sendFromNode.getFirstChild().getNodeValue();
                        account.setPassword(password);
                    }
                    else if(sendFromNode.getNodeName().equals(CERT_PATH))
                    {
                        String certPath = sendFromNode.getFirstChild().getNodeValue();
                        account.setCertPath(certPath);
                    }
                    else if(sendFromNode.getNodeName().equals(CERT_PASSWORD))
                    {
                        String certPass = sendFromNode.getFirstChild().getNodeValue();
                        account.setCertPassword(certPass);
                    }
                    else
                    {
                        throw new ConfigurationException(SEND_FROM, sendFromNode, LOGIN, PASSWORD, SEND_FROM);
                    }
                }

                config.setEmailAccount(account);
            }
            else
            {
                throw new ConfigurationException(EMAIL, emailNode, NOTIFY, SEND_FROM);
            }
        }
    }

    /**
     * Parses the assignments out of the XML file and assigns them to the Configuration.
     *
     * @param assignmentNodes NodeList of the assignment nodes
     * @param config the Configuration object that will represent the config XML
     * @throws ConfigurationException
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
                throw new ConfigurationException(ASSIGNMENTS, currNode, ASSIGNMENT);
            }
        }
    }

    /**
     * Adds an individual assignment from the XML file and adds it to the Configuration.
     *
     * @param asgnNode Node of an assignment
     * @param config the Configuration object that will represent the config XML
     * @throws ConfigurationException
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
            //Part
            else if (currNode.getNodeName().equals(PART))
            {
                processPart(currNode, asgn);
            }
            else
            {
                throw new ConfigurationException(ASSIGNMENT, currNode, RUBRIC, DEDUCTIONS, PART);
            }
        }

        config.addAssignment(asgn);
    }

    /**
     * Adds a part to the assignment. Supports PARTs of
     * TYPE: NON-CODE, CODE, & LAB
     *
     * @param partNode
     * @param asgn
     * @throws ConfigurationException
     */
    private static void processPart(Node partNode, Assignment asgn) throws ConfigurationException
    {
        String name = partNode.getAttributes().getNamedItem(NAME).getNodeValue();
        int points = Integer.valueOf(partNode.getAttributes().getNamedItem(POINTS).getNodeValue());
        String type = partNode.getAttributes().getNamedItem(TYPE).getNodeValue();

        if(type.equalsIgnoreCase("NON-HANDIN"))
        {
            asgn.addNonHandinPart(new NonHandinPart(asgn, name, points));
        }
        else if(type.equalsIgnoreCase("HANDIN"))
        {
            processHandinPart(partNode, asgn, name, points);
        }
        else if(type.equalsIgnoreCase("LAB"))
        {
            int labNumber = Integer.valueOf(partNode.getAttributes().getNamedItem(LAB_NUMBER).getNodeValue());

            asgn.addLabPart(new LabPart(asgn, name, points, labNumber));
        }
        else
        {
            throw new ConfigurationException("Encountered " + PART + " of unsupported "+ TYPE + ": " + type);
        }
    }

    private static void processHandinPart(Node partNode, Assignment asgn, String name, int points) throws ConfigurationException
    {
        String language = null;

        //Look for language tag
        NamedNodeMap attributes = partNode.getAttributes();
        for(int i = 0; i < attributes.getLength(); i++)
        {
            Node attribute = attributes.item(i);

            if(skipNode(attribute))
            {
                continue;
            }
            else if(attribute.getNodeName().equals(LANGUAGE))
            {
                language = attribute.getNodeValue();
            }
        }

        HandinPart part = null;
        
        //Create CodePart
        if(language != null)
        {
            part = processCodePart(partNode, language, asgn, name, points);
        }
        //Create NonCodePart
        else
        {
            part = new NonCodeHandin(asgn, name, points);
        }

        //Find rubric and deduction list
        NodeList childNodes = partNode.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++)
        {
            Node currNode = childNodes.item(i);

            if(skipNode(currNode))
            {
                continue;
            }
            //Time information (LATE-POLICY)
            else if(currNode.getNodeName().equals(LATE_POLICY))
            {
                processTimeInfo(currNode, part);
            }
            //Rubric
            else if (currNode.getNodeName().equals(RUBRIC))
            {
                String location = currNode.getAttributes().getNamedItem(LOCATION).getNodeValue();
                part.setRubric(location);
            }
            //Deduction list
            else if (currNode.getNodeName().equals(DEDUCTIONS))
            {
                String location = currNode.getAttributes().getNamedItem(LOCATION).getNodeValue();
                part.setDeductionList(location);
            }
            else if(language != null && currNode.getNodeName().equals(RUN)) {}
            else if(language != null && currNode.getNodeName().equals(DEMO)) {}
            else if(language != null && currNode.getNodeName().equals(TESTER)) {}
            else
            {
                throw new ConfigurationException("HANDIN node only supports children of types " +
                                                 RUBRIC + " and " + DEDUCTIONS + ". Encountered node:" +
                                                 currNode.getNodeName());
            }
        }

        asgn.addHandinPart(part);

    }

    /**
     * Parses out a code part. Currently supports Java, C, C++, & Matlab.
     *
     * @param partNode
     * @param asgn
     * @param name
     * @param points
     * @throws ConfigurationException
     */
    private static HandinPart processCodePart(Node partNode, String language, Assignment asgn, String name, int points) throws ConfigurationException
    {
        CodeHandin part = null;
        
        //String language = partNode.getAttributes().getNamedItem(LANGUAGE).getNodeValue();

        //Create the appropriate subclass of CodePart based on the language
        if(language.equalsIgnoreCase("Java"))
        {
            part = new JavaHandin(asgn, name, points);
        }
        else if(language.equalsIgnoreCase("C"))
        {
            part = new CHandin(asgn, name, points);
        }
        else if(language.equalsIgnoreCase("C++"))
        {
            part = new CPPHandin(asgn, name, points);
        }
        else if(language.equalsIgnoreCase("Matlab"))
        {
            part = new MatlabHandin(asgn, name, points);
        }
        else
        {
            throw new ConfigurationException("Encountered CODE PART of unsupported" + LANGUAGE + ": " + language);
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
                        throw new ConfigurationException(RUN, propertyNode, PROPERTY);
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
                        throw new ConfigurationException(DEMO, propertyNode, PROPERTY);
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
                        throw new ConfigurationException(TESTER, propertyNode, PROPERTY);
                    }
                }
            }
            else if(childNode.getNodeName().equals(RUBRIC)) {}
            else if(childNode.getNodeName().equals(DEDUCTIONS)) {}
            else if(childNode.getNodeName().equals(LATE_POLICY)) {}
            else
            {
                throw new ConfigurationException(PART, childNode, LATE_POLICY, RUN, DEMO, TESTER, RUBRIC, DEDUCTIONS);
            }
        }
        
        return part;
    }

    /**
     * Processes the LATE-POLICY tag information.
     *
     * @param timeNode
     * @param part
     * @throws ConfigurationException
     */
    private static void processTimeInfo(Node timeNode, HandinPart part) throws ConfigurationException
    {
        LatePolicy policy = LatePolicy.valueOf(timeNode.getAttributes().getNamedItem(TYPE).getNodeValue());
        GradeUnits units = GradeUnits.valueOf(timeNode.getAttributes().getNamedItem(UNITS).getNodeValue());

        TimeInformation timeInfo = new TimeInformation(policy, units);

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
                timeInfo.setEarly(calval.cal, calval.val);
            }
            else if (childNode.getNodeName().equals(ONTIME))
            {
                CalendarValue calval = getTimeFromNode(childNode);
                timeInfo.setOntime(calval.cal, calval.val);
            }
            else if (childNode.getNodeName().equals(LATE))
            {
                CalendarValue calval = getTimeFromNode(childNode);
                timeInfo.setLate(calval.cal, calval.val);
            }
            else
            {
                throw new ConfigurationException(LATE_POLICY, childNode, EARLY, ONTIME, LATE);
            }
        }

        part.setTimeInfo(timeInfo);
    }


    /**
     * A simple data structure that holds a Calendar and an integer value
     */
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
     * Parses the date information out of a node that has date and value
     * information and returns a Calendar and integer value that represents
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
        //Check if file exists
        File file = new File(XMLFilePath);
        if(!file.exists())
        {
            throw new ConfigurationException("Configuration could not be read, location specified: " + XMLFilePath);
        }

        //Parse document
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