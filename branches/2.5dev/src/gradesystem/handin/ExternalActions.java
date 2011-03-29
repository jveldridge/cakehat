package gradesystem.handin;

import com.google.common.collect.ImmutableList;
import gradesystem.Allocator;
import gradesystem.database.Group;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    public List<DistributableActionDescription> getActionDescriptions()
    {
        ImmutableList.Builder<DistributableActionDescription> builder = ImmutableList.builder();

        builder.add(new HandinCommand());
        builder.add(new DemoCommand());
        builder.add(new PrintCommand());

        return builder.build();
    }

    //Replacement sequences
    private static final String
            GROUPS_INFO = "^groups_info^",
            STUDENT_LOGINS = "^student_logins^",
            GROUP_NAME = "^group_name^",
            UNARCHIVE_DIR = "^unarchive_dir^",
            ASSIGNMENT_NAME = "^assignment_name^",
            ASSIGNMENT_NUMBER = "^assignment_number^",
            PART_NAME = "^part_name^",
            PART_NUMBER = "^part_number^";

    private class PrintCommand extends Command
    {
        private final DistributableActionProperty COMMAND_PROPERTY =
            new DistributableActionProperty("command",
            "The command that will be executed. Special character sequences " +
            "placed in the command will be replaced with cakehat supplied " +
            "information.\n" +
            "Character sequences: \n" +
            GROUPS_INFO       + " - Information about the groups. This will be " +
                                   "an array of maps that include the group  " +
                                   "name, student logins in the group and the " +
                                   "fully qualified path to the directory the " +
                                   "handin has been unarchived into. \n" +
            ASSIGNMENT_NAME   + " - The name of the assignment \n" +
            ASSIGNMENT_NUMBER + " - The number of the assignment \n" +
            PART_NAME         + " - The name of the part \n" +
            PART_NUMBER       + " - The number of the part \n\n" +
            "Information will be provided as JSON values, JSON maps, and JSON arrays. \n" +
            "String value: \"An Assignment Name\" \n" +
            "Number value: 11 \n" +
            "Array: [\"jak2\",\"jeldridg\"] \n" +
            "Map: {\"name\":\"the group\",\"members\":[\"jak2\",\"jeldridg\"]," +
            "\"unarchive_dir\":\"/course/cs000/.cakehat/.aunger/asgn/part/the group/\"}",
            true);

        public String getName()
        {
            return "print-command";
        }

        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. " +
                   "This is intended to be used for customizing the print command. " +
                   "Information pertains to the assignment, part, groups, " +
                   "students, and the directory the handins have been unarchived into. " +
                   "The command will be executed in the grader's temporary " +
                   "grading directory.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.PRINT);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.PRINT, ActionMode.RUN, ActionMode.OPEN, ActionMode.TEST);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new DistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    this.performAction(part, Arrays.asList(new Group[] { group }));
                }

                public void performAction(DistributablePart part, Collection<Group> groups) throws ActionException
                {
                    String command = properties.get(COMMAND_PROPERTY);
                    command = replaceAssignmentSequences(command, part);
                    command = replaceGroupInfoSequences(command, part,
                            new ArrayList<Group>(groups));


                    File workspace = Allocator.getPathServices().getUserWorkspaceDir();
                    runCommand(command, workspace, part, properties);
                }
            };

            return action;
        }
    }

    private class HandinCommand extends Command
    {
        private final DistributableActionProperty COMMAND_PROPERTY =
            new DistributableActionProperty("command",
            "The command that will be executed. Special character sequences " +
            "placed in the command will be replaced with cakehat supplied " +
            "information.\n" +
            "Character sequences: \n" +
            GROUP_NAME        + " - The name of the group \n" +
            STUDENT_LOGINS    + " - The logins of all students in the group \n" +
            UNARCHIVE_DIR     + " - The fully qualified path to the directory the " +
                                   "the handin has been unarchived into. " +
            ASSIGNMENT_NAME   + " - The name of the assignment \n" +
            ASSIGNMENT_NUMBER + " - The number of the assignment \n" +
            PART_NAME         + " - The name of the part \n" +
            PART_NUMBER       + " - The number of the part \n\n" +
            "Information will be provided as a JSON value or a JSON array. \n" +
            "String value: \"An Assignment Name\" \n" +
            "Number value: 11 \n" +
            "Array: [\"jak2\",\"jeldridg\"]",
            true);

        public String getName()
        {
            return "handin-command";
        }

        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. " +
                   "Information pertains to the assignment, part, group, " +
                   "students, and the directory the handin have been unarchived into. " +
                   "The command will be executed in the directory containing the unarchived " +
                   "contents of the handin that belong to this part.";
        }
        
        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.RUN, ActionMode.OPEN, ActionMode.TEST);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.RUN, ActionMode.OPEN, ActionMode.TEST);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    String command = properties.get(COMMAND_PROPERTY);
                    command = replaceAssignmentSequences(command, part);
                    command = replaceHandinSequences(command, part, group);

                    File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
                    runCommand(command, unarchiveDir, part, properties);
                }
            };

            return action;
        }
    }

    private class DemoCommand extends Command
    {
        private final DistributableActionProperty COMMAND_PROPERTY =
            new DistributableActionProperty("command",
            "The command that will be executed. Special character sequences " +
            "placed in the command will be replaced with cakehat supplied " +
            "information.\n" +
            "Character sequences: \n" +
            ASSIGNMENT_NAME   + " - The name of the assignment \n" +
            ASSIGNMENT_NUMBER + " - The number of the assignment \n" +
            PART_NAME         + " - The name of the part \n" +
            PART_NUMBER       + " - The number of the part \n\n" +
            "Information will be provided as a JSON value. \n" +
            "String value: \"An Assignment Name\" \n" +
            "Number value: 11",
            true);
        
        public String getName()
        {
            return "demo-command";
        }

        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. " +
                   "Information is limited to that of the assignment and part, " +
                   "no information will be provided about students, groups, or " +
                   "handins. The command will be executed in the grader's " +
                   "temporary grading directory.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return ImmutableList.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }

        public List<ActionMode> getSuggestedModes()
        {
            return ImmutableList.of(ActionMode.DEMO);
        }

        public List<ActionMode> getCompatibleModes()
        {
            return ImmutableList.of(ActionMode.RUN, ActionMode.DEMO, ActionMode.OPEN, ActionMode.TEST);
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
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

    private abstract class Command implements DistributableActionDescription
    {
        protected final DistributableActionProperty SHOW_TERMINAL_PROPERTY =
            new DistributableActionProperty("show-terminal",
            "By default no terminal is shown when executing the command. " +
            "Set the value of this property to TRUE to execute the command in a " +
            "terminal that will be available for the grader to interact with.",
            false);

        protected final DistributableActionProperty TERMINAL_TITLE_PROPERTY =
            new DistributableActionProperty("terminal-name",
            "If a terminal is shown the value of this property will " +
            "be displayed as the title of the terminal. If this value " +
            "is not set the terminal's title will be '[Assignment Name] - [Part Name]'.",
            false);

        public ActionProvider getProvider()
        {
            return ExternalActions.this;
        }

        protected void runCommand(String command, File directory,
                DistributablePart part,
                Map<DistributableActionProperty, String> properties) throws ActionException
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
                    title = part.getAssignment().getName() + " - " +
                            part.getName();
                }
                try
                {
                    Allocator.getExternalProcessesUtilities()
                            .executeInVisibleTerminal(title, command, directory);
                }
                catch (IOException e)
                {
                    throw new ActionException("Cannot execute command " +
                            "in visible terminal: " +  command, e);
                }
            }
            else
            {
                try
                {
                    Allocator.getExternalProcessesUtilities()
                            .executeAsynchronously(command, directory);
                }
                catch(IOException e)
                {
                   throw new ActionException("Cannot execute command: " +
                            command, e);
                }
            }
        }
    }


    /**************************************************************************\
    |*                           Helper methods                               *|
    \**************************************************************************/


    /**
     * Returns a string replacing the ^groups_info^ sequence in
     * <code>command</code> with the names, members, and unarchive directories
     * for the groups provided.
     *
     * @param command
     * @param part
     * @param groups
     * @return
     */
    static String replaceGroupInfoSequences(String command,
            DistributablePart part, List<Group> groups)
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
     * Builds a JSON map for the <code>group</code> that contains the name,
     * members (student logins), and unarchive directory for the group.
     *
     * @param part
     * @param group
     * @return
     */
    private static String buildJSONGroupMap(DistributablePart part, Group group)
    {
        String jsonInfo = "{" + quote("name") + ":" + quote(jsonEscape(group.getName())) + ",";

        jsonInfo += quote("members") + ":" +
                buildJSONArray(new ArrayList<String>(group.getMembers()), true) + ",";

        File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(part, group);
        jsonInfo += quote("unarchive_dir") + ":" + quote(jsonEscape(unarchiveDir.getAbsolutePath())) + "}";

        return jsonInfo;
    }

    /**
     * Returns a string replacing the special sequences in <code>command</code>:
     * ^student_logins^
     * ^group_name^
     * ^unarchive_dir^
     *
     * @param command
     * @param part
     * @param group
     * @return
     */
    static String replaceHandinSequences(String command, DistributablePart part,
            Group group)
    {
        if(command.contains(STUDENT_LOGINS))
        {
            ArrayList<String> logins = new ArrayList<String>(group.getMembers());

            command = replace(command, STUDENT_LOGINS, logins);
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
     * Returns a string replacing the special sequences in <code>command</code>:
     * ^assignment_name^
     * ^assignment_number^
     * ^part_name^
     * ^part_number^
     *
     * @param command the command specified in the configuration file
     * @param part
     * @return
     */
    static String replaceAssignmentSequences(String command, DistributablePart part)
    {

        if(command.contains(ASSIGNMENT_NAME))
        {
            command = replace(command, ASSIGNMENT_NAME, part.getAssignment().getName());
        }
        if(command.contains(ASSIGNMENT_NUMBER))
        {
            command = replace(command, ASSIGNMENT_NUMBER, part.getAssignment().getNumber());
        }
        if(command.contains(PART_NAME))
        {
            command = replace(command, PART_NAME, part.getName());
        }
        if(command.contains(PART_NUMBER))
        {
            command = replace(command, PART_NUMBER, part.getNumber());
        }

        return command;
    }

    /**
     * Returns the contents of <code>command</code> with each instance of
     * <code>replacementSequence</code> replaced with the formatted contents of
     * <code>data</code>.
     *
     * @param command
     * @param replacementSequence
     * @param data
     * @return
     */
    private static final String replace(String command,
            String replacementSequence, String data)
    {
        String replacement = quote(jsonEscape(data));

        return command.replace(replacementSequence, replacement);
    }

    /**
     * Returns the contents of <code>command</code> with each instance of
     * <code>replacementSequence</code> replaced with the formatted contents of
     * <code>data</code>.
     *
     * @param command
     * @param replacementSequence
     * @param data
     * @return
     */
    private static String replace(String command,
            String replacementSequence, int data)
    {
        String replacement = Integer.toString(data);

        return command.replace(replacementSequence, replacement);
    }

    /**
     * Returns the contents of <code>command</code> with each instance of
     * <code>replacementSequence</code> replaced with the formatted contents of
     * <code>dataList</code>.
     *
     * @param command
     * @param replacementSequence
     * @param dataList
     * @return
     */
    private static String replace(String command, String replacementSequence,
            List<String> dataList)
    {
        //Build replacement string
        String replacement = buildJSONArray(dataList, true);

        return command.replace(replacementSequence, replacement);
    }

    /**
     * Builds a JSON array out of the elements in <code>dataList</code>. If
     * <code>escapeAndQuote</code> is <code>true</code> then the elements will
     * be made to comply the JSON standard such that they will work properly in
     * the console. If <code>false</code> then the array will be built with the
     * unescaped, unquoted contents of <code>dataList</code> - this may be
     * wanted when building a JSON array out of other JSON objects.
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
     * Escapes <code>"</code> from inside of strings such that they become
     * valid JSON.
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
     * Places the string inside of quotation marks: <code>"</code>
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