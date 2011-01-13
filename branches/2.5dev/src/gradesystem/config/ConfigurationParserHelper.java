package gradesystem.config;

import gradesystem.Allocator;
import java.io.File;
import org.w3c.dom.Node;
import static gradesystem.config.XMLParsingUtilities.*;

/**
 * Helper functions and constants for parsing the configuration file. This is
 * class is used by both {@link ConfigurationParser} and
 * {@link LabConfigurationParser}.
 *
 * @author jak2
 */
class ConfigurationParserHelper
{
    private final static String CONFIG_FILE_NAME = "config.xml";
    
    //For testing purposes, specifies which course's configuration file to read
    private final static String TESTING_COURSE = "cs000";

    //String constants that represent the tags used in the XML markup
    final static String CONFIG = "CONFIG",

                        ASSIGNMENTS = "ASSIGNMENTS", TAS = "TAS", DEFAULTS = "DEFAULTS", EMAIL = "EMAIL",

                        LENIENCY = "LENIENCY",
                        SUBMIT_OPTIONS = "SUBMIT-OPTIONS",
                        SUBMIT = "SUBMIT", NOTIFY = "NOTIFY", EMAIL_GRD = "EMAIL-GRD", PRINT_GRD = "PRINT-GRD",

                        TA = "TA", DEFAULT_GRADER = "DEFAULT-GRADER", ADMIN = "ADMIN",

                        NOTIFY_ADDRESS = "NOTIFY", ADDRESS = "ADDRESS",
                        SEND_FROM = "SEND-FROM", LOGIN = "LOGIN", PASSWORD = "PASSWORD",
                        CERT_PATH = "CERT-PATH", CERT_PASSWORD = "CERT-PASSWORD",

                        ASSIGNMENT = "ASSIGNMENT", NAME = "NAME", NUMBER = "NUMBER",
                        RUBRIC = "RUBRIC", DEDUCTIONS = "DEDUCTIONS", LOCATION = "LOCATION",

                        LAB = "LAB", NON_HANDIN = "NON-HANDIN", HANDIN = "HANDIN", PART = "PART",
                        LAB_NUMBER = "LAB-NUMBER", POINTS = "POINTS",
                        LATE_POLICY = "LATE-POLICY", TYPE = "TYPE", UNITS = "UNITS", AFFECT_ALL = "AFFECT-ALL", EC_IF_LATE = "EC-IF-LATE",
                        INCLUDE_FILES = "INCLUDE-FILES", DIRECTORY = "DIRECTORY", FILE = "FILE", PATH = "PATH",

                        EARLY = "EARLY", ONTIME = "ONTIME", LATE = "LATE",
                        MONTH = "MONTH", DAY = "DAY", YEAR = "YEAR", TIME = "TIME",
                        VALUE = "VALUE",
                        RUN = "RUN", DEMO = "DEMO", TEST = "TEST", OPEN = "OPEN", PRINT = "PRINT", MODE = "MODE",
                        PROPERTY = "PROPERTY", KEY = "KEY";

    /**
     * Returns the root node of the configuration file.
     *
     * @return
     * @throws ConfigurationException
     */
    static Node getConfigurationRoot() throws ConfigurationException
    {
        File configFile = getConfigurationFile();

        //Check the config file exists
        if(!configFile.exists())
        {
            throw new ConfigurationException("Could not find configuration file at: "
                    + configFile.getAbsolutePath());
        }

        //Get root node
        Node configNode = getRootNode(getDocument(configFile), CONFIG);

        return configNode;
    }

    /**
     * Builds the path to the configuration file.
     *
     * <pre>
     * {@code
     * /course/<course code>/.cakehat/<current year>/config/config.xml
     * }
     * </pre>
     *
     * @return
     */
    private static File getConfigurationFile()
    {
        File configFile = new File(new File(new File(new File(new File(new File
                ("/course"),
                getCourse()),
                "/.cakehat"),
                Integer.toString(Allocator.getCalendarUtilities().getCurrentYear())),
                "/config"),
                CONFIG_FILE_NAME);

        return configFile;
    }

    /**
     * Retrieves the course code, e.g. cs000. This is done by examining the
     * location of the running code. If it is determined that the code is
     * running for the cakehat jar as would be during normal operation, the
     * course code is extracted from the path. If the code is instead believed
     * to be running in development mode, a hard coded test value is used.
     *
     * @return
     */
    static String getCourse()
    {
        //Get the location of where this code is running
        String loc = ConfigurationParserHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        //If this is actually the jar we are running from
        if(loc.endsWith("jar") && loc.startsWith("/course/cs"))
        {
            String course = loc.replace("/course/", "");
            course = course.substring(0, course.indexOf("/"));

            return course;
        }
        else
        {
            System.out.println("Using hard-coded test value for course: " + TESTING_COURSE);

            return TESTING_COURSE;
        }
    }
}