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

                        ASSIGNMENT = "ASSIGNMENT", NAME = "NAME", NUMBER = "NUMBER", HAS_GROUPS = "HAS-GROUPS",
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
        File configFile = Allocator.getCourseInfo().getConfigurationFile();

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
}