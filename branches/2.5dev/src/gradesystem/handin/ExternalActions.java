package gradesystem.handin;

import gradesystem.Allocator;
import gradesystem.database.Group;
import gradesystem.views.shared.ErrorView;
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
        ArrayList<DistributableActionDescription> descriptions =
                new ArrayList<DistributableActionDescription>();

        descriptions.add(new Terminal());
        descriptions.add(new HandinCommand());
        descriptions.add(new DemoCommand());
        descriptions.add(new PrintCommand());

        return descriptions;
    }

    private class Terminal implements DistributableActionDescription
    {
        public ActionProvider getProvider()
        {
            return ExternalActions.this;
        }

        public String getName()
        {
            return "terminal";
        }

        public String getDescription()
        {
            return "Opens a terminal that is in the root directory of the " +
                    "unarchived handin";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return new ArrayList<DistributableActionProperty>();
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN, ActionMode.OPEN });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN, ActionMode.OPEN, ActionMode.TEST });
        }

        public DistributableAction getAction(Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);

                    String terminalName = group.getName() + "'s " + part.getAssignment().getName();
                    try
                    {
                        Allocator.getExternalProcessesUtilities()
                                .executeInVisibleTerminal(terminalName, "cd " + unarchiveDir.getAbsolutePath());
                    }
                    catch(IOException e)
                    {
                        new ErrorView(e, "Unable to open terminal for " + group.getName());
                    }
                }
            };

            return action;
        }
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
            ASSIGNMENT_NAME   + " - The name of the assignment" +
            ASSIGNMENT_NUMBER + " - The number of the assignment" +
            PART_NAME         + " - The name of the part" +
            PART_NUMBER       + " - The number of the part" + "\n\n" +
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
                   "students, and the directory the handins have been unarchived into. ";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return Arrays.asList(new DistributableActionProperty[]{ COMMAND_PROPERTY,
                SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.PRINT });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.PRINT,
                ActionMode.RUN, ActionMode.OPEN, ActionMode.TEST });
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
                    runCommand(command, part, properties);
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
            ASSIGNMENT_NAME   + " - The name of the assignment" +
            ASSIGNMENT_NUMBER + " - The number of the assignment" +
            PART_NAME         + " - The name of the part" +
            PART_NUMBER       + " - The number of the part" + "\n\n" +
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
                   "students, and the directory the handin have been unarchived into. ";
        }
        
        public List<DistributableActionProperty> getProperties()
        {
            return Arrays.asList(new DistributableActionProperty[]{ COMMAND_PROPERTY,
                SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN,
                ActionMode.OPEN, ActionMode.TEST });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN,
                ActionMode.OPEN, ActionMode.TEST });
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

                    runCommand(command, part, properties);
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
            ASSIGNMENT_NAME   + " - The name of the assignment" +
            ASSIGNMENT_NUMBER + " - The number of the assignment" +
            PART_NAME         + " - The name of the part" +
            PART_NUMBER       + " - The number of the part" + "\n\n" +
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
                   "handins.";
        }

        public List<DistributableActionProperty> getProperties()
        {
            return Arrays.asList(new DistributableActionProperty[]{ COMMAND_PROPERTY,
                SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY });
        }

        public List<ActionMode> getSuggestedModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.DEMO });
        }

        public List<ActionMode> getCompatibleModes()
        {
            return Arrays.asList(new ActionMode[] { ActionMode.RUN,
                ActionMode.DEMO, ActionMode.OPEN, ActionMode.TEST });
        }

        public DistributableAction getAction(final Map<DistributableActionProperty, String> properties)
        {
            DistributableAction action = new StandardDistributableAction()
            {
                public void performAction(DistributablePart part, Group group) throws ActionException
                {
                    String command = properties.get(COMMAND_PROPERTY);
                    command = replaceAssignmentSequences(command, part);

                    runCommand(command, part, properties);
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

        protected void runCommand(String command, DistributablePart part,
                Map<DistributableActionProperty, String> properties) throws ActionException
        {
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
                            .executeInVisibleTerminal(title, command);
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
                            .executeAsynchronously(command);
                }
                catch(IOException e)
                {
                   throw new ActionException("Cannot execute command: " +
                            command, e);
                }
            }
        }
    }

    /**
     * Helper methods
     */


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

            command = performReplacement(command, GROUPS_INFO, groupsInfo);
            //command = replace(command, GROUPS_INFO, groupsInfo);
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
        String jsonInfo = "{\"name\":\"" + group.getName() + "\",";

        jsonInfo += "\"members\":" +
                buildJSONArray(new ArrayList<String>(group.getMembers()), true) + ",";

        File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);
        jsonInfo += "\"unarchive_dir\":\"" + unarchiveDir.getAbsolutePath() + "\"}";

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
            File unarchiveDir = Allocator.getGradingServices().getUnarchiveHandinDirectory(part, group);

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
        String replacement = "\"" + data + "\"";

        return performReplacement(command, replacementSequence, replacement);
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

        return performReplacement(command, replacementSequence, replacement);
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

        return performReplacement(command, replacementSequence, replacement);
    }

    private static String buildJSONArray(List<String> dataList, boolean quoteString)
    {
        String array = "[";
        for(int i = 0; i < dataList.size(); i++)
        {
            String str = dataList.get(i);

            if(quoteString)
            {
                array += "\"" + str + "\"";
            }
            else
            {
                array += str;
            }

            if(i != dataList.size() - 1)
            {
                array += ",";
            }
        }
        array += "]";

        return array;
    }

    /**
     * Helper method for the replace(...) functions. Returns the contents of
     * <code>command</code>, replacing each instance of
     * <code>replacementSequence</code> with <code>replaceWith</code>.
     *
     * @param command
     * @param replacementSequence
     * @param replaceWith
     * @return
     */
    private static String performReplacement(String command,
            String replacementSequence, String replaceWith)
    {
        //Replacement requires escaping the ^ because that character has special
        //meaning in regex
        replacementSequence = replacementSequence.replaceAll("\\^", "\\\\^");

        command = command.replaceAll(replacementSequence, replaceWith);

        return command;
    }
}