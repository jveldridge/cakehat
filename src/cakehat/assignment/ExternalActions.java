package cakehat.assignment;

import cakehat.Allocator;
import cakehat.database.Student;
import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Actions that rely upon commands external to cakehat.
 *
 * @author jak2
 */
class ExternalActions implements ActionProvider
{
    public String getNamespace()
    {
        return "external";
    }

    public Set<? extends PartActionDescription> getActionDescriptions()
    {
        return ImmutableSet.of(new DigitalHandinCommand(), new DemoCommand(), new PrintCommand());
    }

    //Replacement sequences
    private static final String
            GROUPS_INFO = "^groups_info^",
            STUDENT_LOGINS = "^student_logins^",
            GROUP_NAME = "^group_name^",
            UNARCHIVE_DIR = "^unarchive_dir^",
            ASSIGNMENT_NAME = "^assignment_name^",
            GRADABLE_EVENT_NAME = "^gradable_event_name^",
            PART_NAME = "^part_name^";

    private class PrintCommand extends Command
    {
        private final PartActionProperty COMMAND_PROPERTY =
            new PartActionProperty("command",
            "The command that will be executed. Special character sequences placed in the command will be replaced " +
            "with cakehat supplied information.\n" +
            "Character sequences: \n" +
            GROUPS_INFO         + " - Information about the groups. This will be an array of maps that include the " +
                                  "group name, student logins in the group and the fully qualified path to the " +
                                  "directory the handin has been unarchived into.\n" +
            ASSIGNMENT_NAME     + " - The name of the assignment.\n" +
            GRADABLE_EVENT_NAME + " - The name of the gradable event.\n" +
            PART_NAME           + " - The name of the part.\n\n" +
            "Information will be provided as JSON values, JSON maps, and JSON arrays.\n" +
            "String value: \"An Assignment Name\" \n" +
            "Array: [\"jak2\",\"jeldridg\"] \n" +
            "Map: {\"name\":\"the group\",\"members\":[\"jak2\",\"jeldridg\"]," +
            "\"unarchive_dir\":\"/course/cs000/.cakehat/workspaces/ta_login/asgn/gradable_event/part/the_group/\"}",
            true);

        private PrintCommand()
        {
            super("print-command");
        }

        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. This is intended to be used for " +
                   "customizing the print command. Information pertains to the assignment, gradable event, part, " +
                   "groups, students, and the directory the handins have been unarchived into. The command will be " +
                   "executed in the grader's temporary grading directory.";
        }

        public Set<PartActionProperty> getProperties()
        {
            return ImmutableSet.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }

        public Set<ActionType> getSuggestedTypes()
        {
            return ImmutableSet.of(ActionType.PRINT);
        }

        public Set<ActionType> getCompatibleTypes()
        {
            return ImmutableSet.of(ActionType.PRINT, ActionType.RUN, ActionType.OPEN, ActionType.TEST);
        }

        public PartAction getAction(final Map<PartActionProperty, String> properties)
        {
            PartAction action = new PartAction()
            {
                public void performAction(Part part, Group group) throws ActionException
                {
                    this.performAction(part, Arrays.asList(new Group[] { group }));
                }

                public void performAction(Part part, Collection<Group> groups) throws ActionException
                {
                    String command = properties.get(COMMAND_PROPERTY);
                    command = replaceAssignmentSequences(command, part);
                    command = replaceGroupInfoSequences(command, part, new ArrayList<Group>(groups));

                    File workspace = Allocator.getPathServices().getUserWorkspaceDir();
                    runCommand(command, workspace, part, properties);
                }
            };

            return action;
        }
    }

    private class DigitalHandinCommand extends Command
    {
        private final PartActionProperty COMMAND_PROPERTY =
            new PartActionProperty("command",
            "The command that will be executed. Special character sequences placed in the command will be replaced " +
            "with cakehat supplied information.\n" +
            "Character sequences: \n" +
            GROUP_NAME          + " - The name of the group.\n" +
            STUDENT_LOGINS      + " - The logins of all students in the group.\n" +
            UNARCHIVE_DIR       + " - The fully qualified path to the directory the the handin has been unarchived " +
                                  "into.\n" +
            ASSIGNMENT_NAME     + " - The name of the assignment.\n" +
            GRADABLE_EVENT_NAME + " - The name of the gradable event.\n" +
            PART_NAME           + " - The name of the part.\n\n" +
            "Information will be provided as a JSON value or a JSON array. \n" +
            "String value: \"An Assignment Name\" \n" +
            "Array: [\"jak2\",\"jeldridg\"]",
            true);

        private DigitalHandinCommand()
        {
            super("digital-handin-command");
        }

        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. Information pertains to the " +
                   "assignment, gradable event, part, group, students, and the directory the handin have been " +
                   "unarchived into. The  command will be executed in the directory containing the unarchived " +
                   "contents of the digital handin that belong to this part.";
        }
        
        public Set<PartActionProperty> getProperties()
        {
            return ImmutableSet.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }

        public Set<ActionType> getSuggestedTypes()
        {
            return ImmutableSet.of(ActionType.RUN, ActionType.OPEN, ActionType.TEST);
        }

        public Set<ActionType> getCompatibleTypes()
        {
            return ImmutableSet.of(ActionType.RUN, ActionType.OPEN, ActionType.TEST);
        }

        public PartAction getAction(final Map<PartActionProperty, String> properties)
        {
            PartAction action = new StandardPartAction()
            {
                public void performAction(Part part, Group group) throws ActionException
                {
                    String command = properties.get(COMMAND_PROPERTY);
                    command = replaceAssignmentSequences(command, part);
                    command = replaceDigitalHandinSequences(command, part, group);

                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
                    runCommand(command, unarchiveDir, part, properties);
                }
            };

            return action;
        }
    }

    private class DemoCommand extends Command
    {
        private final PartActionProperty COMMAND_PROPERTY =
            new PartActionProperty("command",
            "The command that will be executed. Special character sequences placed in the command will be replaced " +
            "with cakehat supplied information.\n" +
            "Character sequences:\n" +
            ASSIGNMENT_NAME     + " - The name of the assignment.\n" +
            GRADABLE_EVENT_NAME + " - The name of the gradable event.\n" +
            PART_NAME           + " - The name of the part.\n" +
            "Information will be provided as a JSON value. \n" +
            "String value: \"An Assignment Name\" \n",
            true);
        
        private DemoCommand()
        {
            super("demo-command");
        }

        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. Information is limited to that of " +
                   "the assignment, gradable event, and part; no information will be provided about students, " +
                   "groups, or digital handins. The command will be executed in the grader's temporary grading " +
                   "directory.";
        }

        public Set<PartActionProperty> getProperties()
        {
            return ImmutableSet.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }

        public Set<ActionType> getSuggestedTypes()
        {
            return ImmutableSet.of(ActionType.DEMO);
        }

        public Set<ActionType> getCompatibleTypes()
        {
            return ImmutableSet.of(ActionType.RUN, ActionType.DEMO, ActionType.OPEN, ActionType.TEST);
        }

        public PartAction getAction(final Map<PartActionProperty, String> properties)
        {
            PartAction action = new StandardPartAction()
            {
                public void performAction(Part part, Group group) throws ActionException
                {
                    String command = properties.get(COMMAND_PROPERTY);
                    command = replaceAssignmentSequences(command, part);

                    File workspace = Allocator.getPathServices().getUserWorkspaceDir();
                    runCommand(command, workspace, part, properties);
                }
            };

            return action;
        }
    }

    private abstract class Command extends PartActionDescription
    {
        protected final PartActionProperty SHOW_TERMINAL_PROPERTY =
            new PartActionProperty("show-terminal",
            "By default no terminal is shown when executing the command. Set the value of this property to TRUE to " +
            "execute the command in a terminal that will be available for the grader to interact with.",
            false);

        protected final PartActionProperty TERMINAL_TITLE_PROPERTY =
            new PartActionProperty("terminal-name",
            "If a terminal is shown the value of this property will be displayed as the title of the terminal. If " +
            "this value is not set the terminal's title will be '[Assignment Name] - [Gradable Event Name] - " +
            "[Part Name]'.",
            false);

        Command(String actionName)
        {
            super(ExternalActions.this, actionName);
        }

        protected void runCommand(String command, File directory, Part part,
                Map<PartActionProperty, String> properties) throws ActionException
        {
            command = consoleEscape(command);

            if(properties.containsKey(SHOW_TERMINAL_PROPERTY) &&
                    properties.get(SHOW_TERMINAL_PROPERTY).equalsIgnoreCase("TRUE"))
            {
                String title;

                if(properties.containsKey(TERMINAL_TITLE_PROPERTY))
                {
                    title = properties.get(TERMINAL_TITLE_PROPERTY);
                }
                else
                {
                    title = part.getFullDisplayName();
                }
                try
                {
                    Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(title, command, directory);
                }
                catch (IOException e)
                {
                    throw new ActionException("Cannot execute command in visible terminal: " +  command, e);
                }
            }
            else
            {
                try
                {
                    Allocator.getExternalProcessesUtilities().executeAsynchronously(command, directory);
                }
                catch(IOException e)
                {
                   throw new ActionException("Cannot execute command: " + command, e);
                }
            }
        }
    }


    /******************************************************************************************************************\
    |*                                                Helper methods                                                  *|
    \******************************************************************************************************************/

    private static List<String> getMemberLogins(Group group)
    {
        ArrayList<String> logins = new ArrayList<String>();
        for(Student student : group)
        {
            logins.add(student.getLogin());
        }
        
        return logins;
    }
    
    /**
     * Returns a string replacing the ^groups_info^ sequence in {@code command} with the names, members, and unarchive
     * directories for the groups provided.
     *
     * @param command
     * @param part
     * @param groups
     * @return
     */
    static String replaceGroupInfoSequences(String command, Part part, List<Group> groups)
    {
        if(command.contains(GROUPS_INFO))
        {
            //Build array of group information
            List<String> jsonGroups = new ArrayList<String>();
            for(Group group : groups)
            {
                jsonGroups.add(buildJSONGroupMap(part, group));
            }
            String groupsInfo = buildJSONArray(jsonGroups, false);

            command = command.replace(GROUPS_INFO, groupsInfo);
        }

        return command;
    }

    /**
     * Builds a JSON map for the {@code group} that contains the name, members (student logins), and unarchive directory
     * for the group.
     *
     * @param part
     * @param group
     * @return
     */
    private static String buildJSONGroupMap(Part part, Group group)
    {
        String jsonInfo = "{" + quote("name") + ":" + quote(jsonEscape(group.getName())) + ",";
        jsonInfo += quote("members") + ":" + buildJSONArray(getMemberLogins(group), true) + ",";

        File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
        jsonInfo += quote("unarchive_dir") + ":" + quote(jsonEscape(unarchiveDir.getAbsolutePath())) + "}";

        return jsonInfo;
    }

    /**
     * Returns a string replacing the special sequences in {@code command}:
     * <ul>
     * <li>^student_logins^</li>
     * <li>^group_name^</li>
     * <li>^unarchive_dir^</li>
     * </ul>
     * Package private for testing purposes.
     *
     * @param command
     * @param part
     * @param group
     * @return
     */
    static String replaceDigitalHandinSequences(String command, Part part, Group group)
    {
        if(command.contains(STUDENT_LOGINS))
        {
            command = replace(command, STUDENT_LOGINS, getMemberLogins(group));
        }
        if(command.contains(GROUP_NAME))
        {
            command = replace(command, GROUP_NAME, group.getName());
        }
        if(command.contains(UNARCHIVE_DIR))
        {
            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);

            command = replace(command, UNARCHIVE_DIR, unarchiveDir.getAbsolutePath());
        }

        return command;
    }

    /**
     * Returns a string replacing the special sequences in {@code command}:
     * <ul>
     * <li>^assignment_name^</li>
     * <li>^gradable_event_name</li>
     * <li>^part_name^</li>
     * </ul>
     * Package private for testing purposes.
     *
     * @param command the command specified in the configuration file
     * @param part
     * @return
     */
    static String replaceAssignmentSequences(String command, Part part)
    {
        if(command.contains(ASSIGNMENT_NAME))
        {
            command = replace(command, ASSIGNMENT_NAME, part.getGradableEvent().getAssignment().getName());
        }
        if(command.contains(GRADABLE_EVENT_NAME))
        {
            command = replace(command, GRADABLE_EVENT_NAME, part.getGradableEvent().getName());
        }
        if(command.contains(PART_NAME))
        {
            command = replace(command, PART_NAME, part.getName());
        }

        return command;
    }

    /**
     * Returns the contents of {@code command} with each instance of {@code replacementSequence} replaced with
     * the formatted contents of {@code data}.
     *
     * @param command
     * @param replacementSequence
     * @param data
     * @return
     */
    private static String replace(String command, String replacementSequence, String data)
    {
        String replacement = quote(jsonEscape(data));

        return command.replace(replacementSequence, replacement);
    }

    /**
     * Returns the contents of {@code command} with each instance of {@code replacementSequence} replaced with the
     * formatted contents of {@code data}.
     *
     * @param command
     * @param replacementSequence
     * @param data
     * @return
     */
    private static String replace(String command, String replacementSequence, int data)
    {
        String replacement = Integer.toString(data);

        return command.replace(replacementSequence, replacement);
    }

    /**
     * Returns the contents of {@code command} with each instance of {@code replacementSequence} replaced with the
     * formatted contents of {@code dataList}.
     *
     * @param command
     * @param replacementSequence
     * @param dataList
     * @return
     */
    private static String replace(String command, String replacementSequence, List<String> dataList)
    {
        //Build replacement string
        String replacement = buildJSONArray(dataList, true);

        return command.replace(replacementSequence, replacement);
    }

    /**
     * Builds a JSON array out of the elements in {@code dataList}. If {@code escapeAndQuote} is {@code true} then the
     * elements will be made to comply with the JSON standard such that they will work properly in the console. If
     * {@code false} then the array will be built with the unescaped, unquoted contents of {@code dataList} - this may
     * be wanted when building a JSON array out of other JSON objects.
     *
     * @param dataList
     * @param escapeAndQuote
     * @return
     */
    private static String buildJSONArray(List<String> dataList, boolean escapeAndQuote)
    {
        String array = "[";
        for(int i = 0; i < dataList.size(); i++)
        {
            String str = dataList.get(i);

            if(escapeAndQuote)
            {
                str = quote(jsonEscape(str));
            }

            array += str;

            if(i != dataList.size() - 1)
            {
                array += ",";
            }
        }
        array += "]";

        return array;
    }

    /**
     * Escapes characters that have a special meaning in the console.
     *
     * @param str
     * @return
     */
    private static String consoleEscape(String str)
    {
        //Escape the \ character
        str = str.replace("\\", "\\\\");

        //Escape the " character
        str = str.replace("\"", "\\\"");

        //Escape the ' character
        str = str.replace("'", "\\\'");

        return str;
    }

    /**
     * Escapes {@code "} from inside of strings such that they become valid JSON.
     * 
     * @param str
     * @return
     */
    private static String jsonEscape(String str)
    {
        str = str.replace("\"", "\\\"");
        
        return str;
    }

    /**
     * Places the string inside of quotation marks: {@code "}
     *
     * @param str
     * @return
     */
    private static String quote(String str)
    {
        str = "\"" + str + "\"";

        return str;
    }
}