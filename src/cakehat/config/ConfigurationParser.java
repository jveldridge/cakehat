package cakehat.config;

import java.io.File;
import java.util.Calendar;
import org.w3c.dom.Node;
import cakehat.Allocator;
import cakehat.config.handin.ActionRepository;
import cakehat.config.handin.DistributableAction;
import cakehat.config.handin.DistributableActionDescription.ActionMode;
import cakehat.config.handin.DistributablePart;
import cakehat.config.handin.Handin;
import cakehat.config.handin.file.AlwaysAcceptingFilterProvider;
import cakehat.config.handin.file.DirectoryFilterProvider;
import cakehat.config.handin.file.FileFilterProvider;
import cakehat.config.handin.file.FilterProvider;
import cakehat.config.handin.file.OrFilterProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static cakehat.config.XMLParsingUtilities.*;
import static cakehat.config.ConfigurationParserHelper.*;

/**
 * Parses the configuration file.
 * 
 * @author jak2
 */
@Deprecated
public class ConfigurationParser
{
    private final static ActionRepository ACTION_REPOSITORY = new ActionRepository();

    /**
     * Parses the configuration file. 
     *
     * @return config
     * @throws ConfigurationException
     */
    static Configuration parse() throws ConfigurationException
    {
        Configuration config = new Configuration();
        assignChildrenAttributes(getConfigurationRoot(), config);

        return config;
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
        Map<String, Node> children = getUniqueChildren(configNode,
                new String[] { DEFAULTS, EMAIL, ASSIGNMENTS, TAS },
                new String[] { DEFAULTS, EMAIL, ASSIGNMENTS, TAS });

        processAssignments(children.get(ASSIGNMENTS), config);
        processDefaults(children.get(DEFAULTS), config);
        processTAs(children.get(TAS), config);
        processEmail(children.get(EMAIL), config);
    }

    /**
     * Parses DEFAULTS from <code>defaultNode</code> and adds information to
     * <code>config</code>.
     *
     * <pre>
     * {@code
     * <DEFAULTS>
     *     <LENIENCY>10</LENIENCY>
     *
     *     <!-- Tag optional, all attributes optional -->
     *     <SUBMIT-OPTIONS SUBMIT="TRUE" NOTIFY="FALSE" EMAIL-GRD="FALSE" PRINT-GRD="FALSE" />
     * </DEFAULTS>
     * }
     * </pre>
     * 
     * @param defaultNode
     * @param config
     * @throws ConfigurationException
     */
    private static void processDefaults(Node defaultNode, Configuration config) throws ConfigurationException
    {
        Map<String, Node> children = getUniqueChildren(defaultNode,
                new String[] { LENIENCY }, new String[] { LENIENCY, SUBMIT_OPTIONS });

        config.setLeniency(Integer.parseInt(children.get(LENIENCY).getFirstChild().getNodeValue()));

        SubmitOptions submitOptions;
        if(children.containsKey(SUBMIT_OPTIONS))
        {
            AttributeMap submitAttr = getAttributes(children.get(SUBMIT_OPTIONS),
                    new String[] { }, new String[] { SUBMIT, NOTIFY, EMAIL_GRD, PRINT_GRD });

            submitOptions = new SubmitOptions(
                    submitAttr.getBoolean(SUBMIT, SubmitOptions.SUBMIT_DEFAULT),
                    submitAttr.getBoolean(NOTIFY, SubmitOptions.NOTIFY_DEFAULT),
                    submitAttr.getBoolean(EMAIL_GRD, SubmitOptions.EMAIL_GRD_DEFAULT),
                    submitAttr.getBoolean(PRINT_GRD, SubmitOptions.PRINT_GRD_DEFAULT));
        }
        else
        {
           submitOptions = new SubmitOptions(SubmitOptions.SUBMIT_DEFAULT,
                   SubmitOptions.NOTIFY_DEFAULT,
                   SubmitOptions.EMAIL_GRD_DEFAULT,
                   SubmitOptions.PRINT_GRD_DEFAULT);
        }
        
        config.setSubmitOptions(submitOptions);
    }

    /**
     * Parses out the TA information.
     *
     * <pre>
     * {@code
     * <TAS>
     *     <TA LOGIN="jak2" NAME="Joshua Kaplan" DEFAULT-GRADER="TRUE" ADMIN="TRUE" HTA="TRUE" />
     *     ...
     * </TAS>
     * }
     * </pre>
     *
     * @param taNodes
     * @param config
     * @throws ConfigurationException
     */
    private static void processTAs(Node tasNode, Configuration config) throws ConfigurationException
    {
        Map<String, List<Node>> taNodes = getChildren(tasNode, new String[] { TA });

        for(Node taNode : taNodes.get(TA))
        {
            AttributeMap taAttrs = getAttributes(taNode,
                    new String[] { LOGIN, NAME, DEFAULT_GRADER, ADMIN },
                    new String[] { LOGIN, NAME, DEFAULT_GRADER, ADMIN });

            String taLogin = taAttrs.getString(LOGIN);
            String taName = taAttrs.getString(NAME);
            boolean isDefaultGrader = taAttrs.getBoolean(DEFAULT_GRADER);
            boolean isAdmin = taAttrs.getBoolean(ADMIN);

            config.addTA(new TA(taLogin, taName, isDefaultGrader, isAdmin));
        }
    }

    /**
     * Parses out the information for which email accounts to notify and
     * information necessary to send email from.
     * 
     * <pre>
     * {@code
     * <EMAIL>
     *     <NOTIFY>
     *       <ADDRESS>account@cs.brown.edu</ADDRESS>
     *       ...
     *     </NOTIFY>
     *     <SEND-FROM>
     *         <LOGIN>cs101000</LOGIN>
     *         <PASSWORD>thePassword</PASSWORD>
     *         <CERT-PATH>/course/cs101/grading/smtp_certs/browncscerts.cert</CERT-PATH>
     *         <CERT-PASSWORD>anotherPassword</CERT-PASSWORD>
     *     </SEND-FROM>
     * </EMAIL>
     * }
     * </pre>
     *
     * @param emailNode
     * @param config
     * @throws ConfigurationException
     */
    private static void processEmail(Node emailNode, Configuration config) throws ConfigurationException
    {
        Map<String, Node> emailNodes = getUniqueChildren(emailNode,
                new String[] { NOTIFY_ADDRESS, SEND_FROM },
                new String[] { NOTIFY_ADDRESS, SEND_FROM });
        
        // Add all email addresses to notify
        Map<String, List<Node>> notifyNodes = getChildren(emailNodes.get(NOTIFY_ADDRESS),
                new String[] { ADDRESS });
        for(Node notifyNode : notifyNodes.get(ADDRESS))
        {
            config.addNotifyAddress(notifyNode.getFirstChild().getNodeValue());
        }

        // Email account info
        Map<String, Node> sendFromNodes = getUniqueChildren(emailNodes.get(SEND_FROM),
                new String[] { LOGIN, PASSWORD, CERT_PATH, CERT_PASSWORD},
                new String[] { LOGIN, PASSWORD, CERT_PATH, CERT_PASSWORD});
        EmailAccount account = new EmailAccount(
                sendFromNodes.get(LOGIN).getFirstChild().getNodeValue(),
                sendFromNodes.get(PASSWORD).getFirstChild().getNodeValue(),
                sendFromNodes.get(CERT_PATH).getFirstChild().getNodeValue(),
                sendFromNodes.get(CERT_PASSWORD).getFirstChild().getNodeValue());
        config.setEmailAccount(account);
    }

    /**
     * Parses the assignments from <code>assignmentsNode</code> and adds them
     * to <code>config</code>.
     *
     * <pre>
     * {@code
     * <ASSIGNMENTS>
     *     <ASSIGNMENT ... >
     *     </ASSIGNMENT>
     *     ...
     * </ASSIGNMENTS>
     * }
     * </pre>
     *
     * @param assignmentsNode
     * @param config the Configuration object that will represent the config XML
     * @throws ConfigurationException
     */
    private static void processAssignments(Node assignmentsNode, Configuration config) throws ConfigurationException
    {
        Map<String, List<Node>> children = getChildren(assignmentsNode,
                new String[] { ASSIGNMENT } );

        for(Node asgnNode : children.get(ASSIGNMENT))
        {
            config.addAssignment(getAssignment(asgnNode));
        }
    }

    /**
     * Parses out an assignment from <code>asgnNode</code>.
     *
     * <pre>
     * {@code
     * <ASSIGNMENT NAME="Awesome Assignment" NUMBER="1">
     *     ...
     * </ASSIGNMENT>
     * }
     * </pre>
     *
     * @param asgnNode Node of an assignment
     * @throws ConfigurationException
     */
    private static Assignment getAssignment(Node asgnNode) throws ConfigurationException
    {
        AttributeMap asgnAttrs = getAttributes(asgnNode,
                new String[] { NAME, NUMBER }, new String[] { NAME, NUMBER, HAS_GROUPS });
        Assignment asgn = new Assignment(asgnAttrs.getString(NAME),
                asgnAttrs.getInt(NUMBER), asgnAttrs.getBoolean(HAS_GROUPS, false));

        Map<String, List<Node>> children =
                getChildren(asgnNode, new String[] { LAB, NON_HANDIN, HANDIN });

        for(Node labNode : children.get(LAB))
        {
            asgn.addLabPart(getLabPart(labNode, asgn));
        }

        for(Node nonHandinNode : children.get((NON_HANDIN)))
        {
            asgn.addNonHandinPart(getNonHandinPart(nonHandinNode, asgn));
        }

        //Only one handin child per assignment allowed
        if(children.get(HANDIN).size() > 1)
        {
            throw new ConfigurationException(asgnNode.getNodeName() + " may " +
                    "have at most one " + HANDIN + " child node");
        }
        //Iterate, because this is more convenient than handling the cases of 0
        //or 1 handin nodes separately
        for(Node handinNode : children.get((HANDIN)))
        {
            processHandin(handinNode, asgn);
        }

        return asgn;
    }

    /**
     * Parses out a lab from <code>labNode</code>.
     *
     * <pre>
     * {@code
     * <LAB NAME="Image Processing" NUMBER="2" LAB-NUMBER="8" POINTS="15"/>
     * }
     * </pre>
     *
     * @param labNode
     * @param asgn
     * @return
     * @throws ConfigurationException
     */
    private static LabPart getLabPart(Node labNode, Assignment asgn) throws ConfigurationException
    {
        AttributeMap labAttrs = getAttributes(labNode,
                new String[] { NAME, POINTS, NUMBER, LAB_NUMBER },
                new String[] { NAME, POINTS, NUMBER, LAB_NUMBER });

        String name = labAttrs.getString(NAME);
        int points = labAttrs.getInt(POINTS);
        int number = labAttrs.getInt(NUMBER);
        int labNumber = labAttrs.getInt(LAB_NUMBER);

        return new LabPart(asgn, name, number, points, labNumber);
    }

    /**
     * Parses out a non-handin from <code>nonHandinNode</code>.
     *
     * <pre>
     * {@code
     * <NON-HANDIN NAME="Design Check" NUMBER="3" POINTS="10"/>
     * }
     * </pre>
     *
     * @param nonHandinNode
     * @param asgn
     * @return
     * @throws ConfigurationException
     */
    private static NonHandinPart getNonHandinPart(Node nonHandinNode, Assignment asgn) throws ConfigurationException
    {
        AttributeMap nonHandinAttr = getAttributes(nonHandinNode,
                new String[] { NAME, POINTS, NUMBER },
                new String[] { NAME, POINTS, NUMBER });

        String name = nonHandinAttr.getString(NAME);
        int points = nonHandinAttr.getInt(POINTS);
        int number = nonHandinAttr.getInt(NUMBER);

        return new NonHandinPart(asgn, name, number, points);
    }

    /**
     * Parses out a handin from <code>handinNode</code> and adds it to
     * <code>asgn</code>.
     *
     * <pre>
     * {@code
     * <HANDIN>
     *     <LATE-POLICY ...>
     *         ...
     *     </LATE-POLICY>
     *
     *     <PART ...>
     *         ...
     *     </PART>
     *     ...
     * </HANDIN>
     * }
     * </pre>
     *
     * @param handinNode
     * @param asgn
     * @throws ConfigurationException
     */
    private static void processHandin(Node handinNode, Assignment asgn) throws ConfigurationException
    {
        Map<String, List<Node>> children = getChildren(handinNode,
                new String[] { LATE_POLICY, PART });

        //Time information
        TimeInformation timeInfo = null;
        if(children.get(LATE_POLICY).size() == 1)
        {
            timeInfo = getTimeInfo(children.get(LATE_POLICY).get(0));
        }
        else
        {
            throw new ConfigurationException(handinNode.getNodeName() +
                    " requires 1 " + LATE_POLICY + " child node. There are " +
                    children.get(LATE_POLICY).size() + " children nodes.");
        }

        //Handin
        Handin handin = new Handin(asgn, timeInfo);
        asgn.setHandin(handin);

        //Parts
        List<Node> parts = children.get(PART);
        if(parts.isEmpty())
        {
            throw new ConfigurationException(handinNode.getNodeName() +
                    " must have 1 or more " + PART + " children nodes.");
        }
        for(Node partNode : parts)
        {
            asgn.addDistributablePart(getDistributablePart(partNode, handin));
        }
    }

    /**
     * Parses out a distributable part from <code>partNode</code>. All children
     * of PART are optional and must be unique if present.
     *
     * <pre>
     * {@code
     * <PART NAME="Code" NUMBER="4" POINTS="80">
     *     <RUBRIC LOCATION="..."/>
     *     <DEDUCTIONS LOCATION="..."/>
     *
     *     <RUN MODE="java:magic-run">
     *         <PROPERTY KEY="propName" VALUE="greaaaat"/>
     *         ...
     *     </RUN>
     *
     *     <DEMO MODE="awesome:intimidate-students">
     *         <PROPERTY KEY="propName" VALUE="greaaaat"/>
     *         ...
     *     </DEMO>
     *
     *     <TEST MODE="amazing:fail-all">
     *         <PROPERTY KEY="propName" VALUE="greaaaat"/>
     *         ...
     *     </TEST>
     *
     *     <OPEN MODE="incredible:abracadabra">
     *         <PROPERTY KEY="propName" VALUE="greaaaat"/>
     *         ...
     *     </OPEN>
     *
     *     <PRINT MODE="external:command">
     *         <PROPERTY KEY="propName" VALUE="greaaaat"/>
     *         ...
     *     </OPEN>
     * </PART>
     * }
     * </pre>
     *
     * @param partNode
     * @param handin
     * @return
     * @throws ConfigurationException
     */
    private static DistributablePart getDistributablePart(Node partNode,
            Handin handin) throws ConfigurationException
    {
        Map<String, Node> children = getUniqueChildren(partNode, new String[] { },
            new String[] { INCLUDE_FILES, RUBRIC, DEDUCTIONS, RUN, DEMO, TEST, OPEN, PRINT });

        File deductions = null;
        if(children.containsKey(DEDUCTIONS))
        {
            AttributeMap attrs = getAttributes(children.get(DEDUCTIONS),
                    new String[] { LOCATION }, new String[] { LOCATION });

            deductions = new File(attrs.getString(LOCATION));
        }

        File rubric = null;
        if(children.containsKey(RUBRIC))
        {
            AttributeMap attrs = getAttributes(children.get(RUBRIC),
                    new String[] { LOCATION }, new String[] { LOCATION });

            rubric = new File(attrs.getString(LOCATION));
        }

        FilterProvider provider = new AlwaysAcceptingFilterProvider();
        if(children.containsKey(INCLUDE_FILES))
        {
            provider = getInclusionFilter(children.get(INCLUDE_FILES));
        }

        //Read out the RUN, OPEN, DEMO, TEST, OPEN, & PRINT modes
        Map<ActionMode, DistributableAction> actions =
                new HashMap<ActionMode, DistributableAction>();
        for(ActionMode mode : ActionMode.values())
        {
            if(children.containsKey(mode.toString()))
            {
                Node modeNode = children.get(mode.toString());

                AttributeMap attrs = getAttributes(modeNode,
                        new String[] { MODE }, new String[] { MODE });
                String actionName = attrs.getString(MODE);

                Map<String, String> properties = getPropertyMap(modeNode);

                //This will throw an exception if the action name does not exist,
                //there are required properties missing or invalid properties are present
                actions.put(mode, ACTION_REPOSITORY.getAction(mode, actionName, properties));
            }
        }

        AttributeMap partAttrs = getAttributes(partNode,
                new String[] { NAME, POINTS, NUMBER },
                new String[] { NAME, POINTS, NUMBER });

        DistributablePart part = new DistributablePart(handin,
                partAttrs.getString(NAME),
                partAttrs.getInt(NUMBER),
                partAttrs.getInt(POINTS),
                deductions, rubric, provider,
                actions.get(ActionMode.RUN),
                actions.get(ActionMode.DEMO),
                actions.get(ActionMode.TEST),
                actions.get(ActionMode.OPEN),
                actions.get(ActionMode.PRINT));

        return part;
    }

    /**
     * Creates a FilterProvider from <code>inclusionNode</code>.
     *
     * <pre>
     * {@code
     * <INCLUDE-FILES>
     *     <FILE PATH=”hw1_1a.m” />
     *     <DIRECTORY PATH=”problem_2/” />
     * </INCLUDE-FILES>
     * }
     * </pre>
     *
     * @param inclusionNode
     * @return
     * @throws ConfigurationException
     */
    private static FilterProvider getInclusionFilter(Node inclusionNode)
            throws ConfigurationException
    {
        FilterProvider provider;

        Map<String, List<Node>> children = getChildren(inclusionNode,
                new String[] { DIRECTORY, FILE });


        ArrayList<FilterProvider> providers = new ArrayList<FilterProvider>();

        for(Node includeNode : children.get(DIRECTORY))
        {
            AttributeMap attrs = getAttributes(includeNode,
                    new String[] { PATH }, new String[] { PATH });
            String relativePath = attrs.getString(PATH);
            providers.add(new DirectoryFilterProvider(relativePath));
        }
        for(Node includeNode : children.get(FILE))
        {
            AttributeMap attrs = getAttributes(includeNode,
                    new String[] { PATH }, new String[] { PATH });
            String relativePath = attrs.getString(PATH);
            providers.add(new FileFilterProvider(relativePath));
        }

        if(providers.isEmpty())
        {
            provider = new AlwaysAcceptingFilterProvider();
        }
        else
        {
            provider = new OrFilterProvider(providers);
        }

        return provider;
    }

    /**
     * Parses out the properties for a RUN, DEMO, OPEN, TEST, or PRINT mode.
     * Maps a property's KEY to its VALUE.
     *
     * @param modeNode
     * @return
     * @throws ConfigurationException
     */
    private static Map<String, String> getPropertyMap(Node modeNode)
            throws ConfigurationException
    {
        Map<String, List<Node>> propertyNodes = getChildren(modeNode,
                        new String[] { PROPERTY });
        Map<String, String> properties = new HashMap<String, String>();

        for(Node propertyNode : propertyNodes.get(PROPERTY))
        {
            AttributeMap attrs = getAttributes(propertyNode,
                    new String[] { KEY, VALUE }, new String[] { KEY, VALUE });

            properties.put(attrs.getString(KEY), attrs.getString(VALUE));
        }

        return properties;
    }

    /**
     * Parses out the LATE-POLICY information from <code>timeNode</code>.
     *
     * <pre>
     * {@code
     * <LATE-POLICY TYPE="NO_LATE" AFFECT-ALL="TRUE" EC-IF-LATE="FALSE" UNITS="PERCENTAGE">
     *     <ONTIME MONTH="9" DAY="14" YEAR="2010" TIME="23:59:59"/>
     * </LATE-POLICY>
     * }
     * </pre>
     *
     * @param timeNode
     * @return
     * @throws ConfigurationException
     */
    private static TimeInformation getTimeInfo(Node timeNode) throws ConfigurationException
    {
        AttributeMap timeAttrs = getAttributes(timeNode,
                new String[] { TYPE }, new String [] { TYPE, UNITS, AFFECT_ALL, EC_IF_LATE });

        // Late Policy
        LatePolicy policy = LatePolicy.valueOf(timeAttrs.getString(TYPE));

        // Grade Units (not necessary if the policy is NO_LATE)
        GradeUnits units = null;
        if(policy != LatePolicy.NO_LATE)
        {
            if(timeAttrs.hasAttribute(UNITS))
            {
                units = GradeUnits.valueOf(timeAttrs.getString(UNITS));
            }
            else
            {
                throw new ConfigurationException(LATE_POLICY + " must have a " +
                        UNITS + " attribute when " + TYPE + "=" + policy);
            }
        }

        // Affect-All (optional, defaults to false) - whether late policy deductions
        // apply to all (entire assignment) or just the handin parts of the rubric
        boolean affectAll = timeAttrs.getBoolean(AFFECT_ALL, false);
        
        // If extra credit is allowed for late handins (optional, defaults to true)
        boolean ecIfLate = timeAttrs.getBoolean(EC_IF_LATE, true);

        // TimeInformation
        TimeInformation timeInfo = new TimeInformation(policy, units, affectAll, ecIfLate);

        Map<String, Node> times = getUniqueChildren(timeNode,
                new String[] { ONTIME }, new String[] { EARLY, ONTIME, LATE });

        CalendarValue calval = getTimeFromNode(times.get(ONTIME));
        timeInfo.setOntime(calval.cal, calval.val);
        if(times.containsKey(EARLY))
        {
            calval = getTimeFromNode(times.get(EARLY));
            timeInfo.setEarly(calval.cal, calval.val);
        }
        if(times.containsKey(LATE))
        {
            calval = getTimeFromNode(times.get(LATE));
            timeInfo.setLate(calval.cal, calval.val);
        }

        return timeInfo;
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
        AttributeMap attrs = getAttributes(node,
                new String[] { MONTH, DAY, YEAR, TIME },
                new String[] { MONTH, DAY, YEAR, TIME, VALUE });

        String month = attrs.getString(MONTH);
        String day = attrs.getString(DAY);
        String year = attrs.getString(YEAR);
        String time = attrs.getString(TIME);
        int val = attrs.getInt(VALUE, 0);

        Calendar cal = Allocator.getCalendarUtilities().getCalendar(year, month, day, time);

        return new ConfigurationParser.CalendarValue(cal, val);
    }

    /**
     * A simple data structure that holds a Calendar and an integer value.
     * Used by {@link #getTimeFromNode(Node}) to return both the calendar
     * and associated point or percentage value from either a late or early handin.
     */
    private static class CalendarValue
    {
        final Calendar cal;
        final int val;

        CalendarValue(Calendar cal, int val)
        {
            this.cal = cal;
            this.val = val;
        }
    }
}