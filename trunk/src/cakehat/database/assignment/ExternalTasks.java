package cakehat.database.assignment;

import cakehat.Allocator;
import cakehat.database.Student;
import cakehat.database.Group;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tasks that rely upon commands external to cakehat.
 *
 * @author jak2
 */
class ExternalTasks implements TaskProvider
{
    @Override
    public String getNamespace()
    {
        return "external";
    }

    @Override
    public Set<? extends Task> getTasks()
    {
        return ImmutableSet.of(new NoGroupCommand(), new SingleGroupCommand(), new MultiGroupCommand());
    }

    //Replacement sequences
    private static final String
            GROUPS_INFO = "^groups_info^",
            STUDENT_LOGINS = "^student_logins^",
            GROUP_NAME = "^group_name^",
            UNARCHIVE_DIR = "^unarchive_dir^",
            TEMP_DIR = "^temp_dir^",
            ASSIGNMENT_NAME = "^assignment_name^",
            GRADABLE_EVENT_NAME = "^gradable_event_name^",
            PART_NAME = "^part_name^",
            ACTION_NAME = "^action_name^";

    private class NoGroupCommand extends Command
    {
        private final TaskProperty COMMAND_PROPERTY =
            new TaskProperty("command",
            "The command that will be executed. Special character sequences placed in the command will be replaced " +
            "with cakehat supplied information.<br>" +
            "Character sequences:<br>" +
            ASSIGNMENT_NAME     + " - The name of the assignment.<br>" +
            GRADABLE_EVENT_NAME + " - The name of the gradable event.<br>" +
            PART_NAME           + " - The name of the part.<br>" +
            ACTION_NAME         + " - The name of the action.<br>" +
            TEMP_DIR            + " - The fully qualified path to a temporary directory that may be used while " +
                                  "running this command. This directory will be empty each time the command is " +
                                  "run.<br><br>" +
            "Information will be provided as JSON.<br>" +
            "String value: \"An Assignment Name\"",
            true);
        
        private NoGroupCommand()
        {
            super("no-group-command");
        }

        @Override
        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. Information is limited to that of " +
                   "the assignment, gradable event, and part; no information will be provided about students, " +
                   "groups, or digital handins. The command will be executed in a temporary directory that will be " +
                   "empty each time this task is run.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return false;
        }

        @Override
        boolean isTaskSupported(Action action, Set<Group> groups) throws TaskException
        {
            return true;
        }

        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Set<Group> groups)
                throws TaskException
        {
            String command = properties.get(COMMAND_PROPERTY);
            command = replaceAssignmentSequences(command, action);

            File tempDir = Allocator.getPathServices().getActionTempDir(action, null);
            runCommand(command, tempDir, action, properties);
        }
    }
    
    private class SingleGroupCommand extends Command
    {
        private final TaskProperty COMMAND_PROPERTY =
            new TaskProperty("command",
            "The command that will be executed. Special character sequences placed in the command will be replaced " +
            "with cakehat supplied information.<br>" +
            "Character sequences:<br>" +
            GROUP_NAME          + " - The name of the group.<br>" +
            STUDENT_LOGINS      + " - The logins of all students in the group.<br>" +
            UNARCHIVE_DIR       + " - The fully qualified path to the directory that the handin has been unarchived " +
                                  "into.<br>" +
            TEMP_DIR            + " - The fully qualified path to a temporary directory that may be used while " +
                                  "running this command. This directory will be empty each time the command is " +
                                  "run.<br>" +
            ASSIGNMENT_NAME     + " - The name of the assignment.<br>" +
            GRADABLE_EVENT_NAME + " - The name of the gradable event.<br>" +
            PART_NAME           + " - The name of the part.<br>" +
            ACTION_NAME         + " - The name of the action.<br><br>" + 
            "Information will be provided as a JSON value or a JSON array.<br>" +
            "String value: \"An Assignment Name\"<br>" +
            "Array: [\"jak2\",\"jeldridg\"]",
            true);

        private SingleGroupCommand()
        {
            super("single-group-command");
        }

        @Override
        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. Information pertains to the " +
                   "assignment, gradable event, part, group, students, the directory the handin has been unarchived " +
                   "into, and a temporary directory. The command will be executed in a temporary directory that will " +
                   "be empty each time this task is run.";
        }
        
        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }

        @Override
        boolean isTaskSupported(Action action, Set<Group> groups) throws TaskException
        {
            return groups.size() == 1;
        }

        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Set<Group> groups)
                throws TaskException
        {
            //Enforce that the set passed in only has one group
            Iterator<Group> iter = groups.iterator();
            if(!iter.hasNext())
            {
                throw new IllegalArgumentException("Groups iterator contains no elements");
            }
            Group group = iter.next();

            if(iter.hasNext())
            {
                throw new IllegalArgumentException("Groups iterator contains more than one element");
            }
            
            //Perform task
            String command = properties.get(COMMAND_PROPERTY);
            command = replaceAssignmentSequences(command, action);
            command = replaceDigitalHandinSequences(command, action, group);

            File tempDir = Allocator.getPathServices().getActionTempDir(action, group);
            runCommand(command, tempDir, action, properties);
        }
    }
    
    private class MultiGroupCommand extends Command
    {
        private final TaskProperty COMMAND_PROPERTY =
            new TaskProperty("command",
            "The command that will be executed. Special character sequences placed in the command will be replaced " +
            "with cakehat supplied information.<br>" +
            "Character sequences:<br>" +
            GROUPS_INFO         + " - Information about the groups. This will be an array of maps that include the " +
                                  "group name, student logins in the group, the fully qualified path to the " +
                                  "directory the handin has been unarchived into, and the fully qualified path to " +
                                  "the temporary directory for the task and group.<br>" +
            ASSIGNMENT_NAME     + " - The name of the assignment.<br>" +
            GRADABLE_EVENT_NAME + " - The name of the gradable event.<br>" +
            PART_NAME           + " - The name of the part.<br>" +
            ACTION_NAME         + " - The name of the action.<br><br>" + 
            "Information will be provided as JSON values, JSON maps, and JSON arrays.<br>" +
            "String value: \"An Assignment Name\"<br>" +
            "Array: [\"jak2\",\"jeldridg\"]<br>" +
            "Map: {\"name\":\"the group\",\"members\":[\"jak2\",\"jeldridg\"]," +
            "\"unarchive_dir\":\"/course/cs000/.cakehat/handin/4/1/2/94/\"," +
            "\"temp_dir\":\"/course/cs000/.cakehat/temp/4/1/2/5/94/\"}",
            true);

        private MultiGroupCommand()
        {
            super("multi-group-command");
        }

        @Override
        public String getDescription()
        {
            return "Runs a specified command with cakehat supplied information. Information pertains to the " +
                   "assignment, gradable event, part, action, groups, students, the directory the handins have been " +
                   "unarchived into, and temporary directories. The command will be executed in the temporary " + 
                   "directory for this task.";
        }

        @Override
        public Set<TaskProperty> getProperties()
        {
            return ImmutableSet.of(COMMAND_PROPERTY, SHOW_TERMINAL_PROPERTY, TERMINAL_TITLE_PROPERTY);
        }
        
        @Override
        public boolean requiresDigitalHandin()
        {
            return true;
        }

        @Override
        void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Set<Group> groups)
                throws TaskException
        {
            String command = properties.get(COMMAND_PROPERTY);
            command = replaceAssignmentSequences(command, action);
            command = replaceGroupInfoSequences(command, action, new ArrayList<Group>(groups));

            File tempDir = Allocator.getPathServices().getActionTempDir(action);
            runCommand(command, tempDir, action, properties);
        }

        @Override
        boolean isTaskSupported(Action action, Set<Group> groups) throws TaskException
        {
            return !groups.isEmpty();
        }
    }
    
    private abstract class Command extends Task
    {
        protected final TaskProperty SHOW_TERMINAL_PROPERTY =
            new TaskProperty("show-terminal",
            "By default no terminal is shown when executing the command. Set the value of this property to TRUE to " +
            "execute the command in a terminal that will be available for the grader to interact with.",
            false);

        protected final TaskProperty TERMINAL_TITLE_PROPERTY =
            new TaskProperty("terminal-name",
            "If a terminal is shown the value of this property will be displayed as the title of the terminal. If " +
            "this value is not set the terminal's title will be '[Assignment Name] - [Gradable Event Name] - " +
            "[Part Name]'.",
            false);

        Command(String taskName)
        {
            super(ExternalTasks.this, taskName);
        }
        
        @Override
        public Set<ActionDescription> getSuggestedActionDescriptions()
        {
            return ImmutableSet.<ActionDescription>of();
        }

        protected void runCommand(String command, File directory, Action action, Map<TaskProperty, String> properties)
                throws TaskException
        {
            command = consoleEscape(command);

            if("TRUE".equalsIgnoreCase(properties.get(SHOW_TERMINAL_PROPERTY)))
            {
                String title;

                if(properties.containsKey(TERMINAL_TITLE_PROPERTY))
                {
                    title = properties.get(TERMINAL_TITLE_PROPERTY);
                }
                else
                {
                    title = action.getPart().getFullDisplayName();
                }
                try
                {
                    Allocator.getExternalProcessesUtilities().executeInVisibleTerminal(title, command, directory);
                }
                catch (IOException e)
                {
                    throw new TaskException("Cannot execute command in visible terminal: " +  command, e);
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
                   throw new TaskException("Cannot execute command: " + command, e);
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
     * @param context
     * @param groups
     * @return
     */
    static String replaceGroupInfoSequences(String command, Action action, List<Group> groups)
    {
        if(command.contains(GROUPS_INFO))
        {
            //Build array of group information
            List<String> jsonGroups = new ArrayList<String>();
            for(Group group : groups)
            {
                jsonGroups.add(buildJSONGroupMap(action, group));
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
     * @param action
     * @param group
     * @return
     */
    private static String buildJSONGroupMap(Action action, Group group)
    {
        String jsonInfo = "{" + quote("name") + ":" + quote(jsonEscape(group.getName())) + ",";
        jsonInfo += quote("members") + ":" + buildJSONArray(getMemberLogins(group), true) + ",";

        File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
        jsonInfo += quote("unarchive_dir") + ":" + quote(jsonEscape(unarchiveDir.getAbsolutePath())) + ",";
        
        File tempDir = Allocator.getPathServices().getActionTempDir(action, group);
        jsonInfo += quote("temp_dir") + ":" + quote(jsonEscape(tempDir.getAbsolutePath())) + "}";

        return jsonInfo;
    }

    /**
     * Returns a string replacing the special sequences in {@code command}:
     * <ul>
     * <li>^student_logins^</li>
     * <li>^group_name^</li>
     * <li>^unarchive_dir^</li>
     * <li>^temp_dir^</li>
     * </ul>
     * Package private for testing purposes.
     *
     * @param command
     * @param action
     * @param group
     * @return
     */
    static String replaceDigitalHandinSequences(String command, Action action, Group group)
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
            File unarchiveDir = Allocator.getPathServices().getUnarchiveHandinDir(action.getPart(), group);
            command = replace(command, UNARCHIVE_DIR, unarchiveDir.getAbsolutePath());
        }
        if(command.contains(TEMP_DIR))
        {
            File tempDir = Allocator.getPathServices().getActionTempDir(action, group);
            command = replace(command, TEMP_DIR, tempDir.getAbsolutePath());
        }

        return command;
    }

    /**
     * Returns a string replacing the special sequences in {@code command}:
     * <ul>
     * <li>^assignment_name^</li>
     * <li>^gradable_event_name</li>
     * <li>^part_name^</li>
     * <li>^action_name^</li>
     * </ul>
     * Package private for testing purposes.
     *
     * @param command the command specified in the configuration file
     * @param part
     * @return
     */
    static String replaceAssignmentSequences(String command, Action action)
    {
        if(command.contains(ASSIGNMENT_NAME))
        {
            command = replace(command, ASSIGNMENT_NAME, action.getPart().getGradableEvent().getAssignment().getName());
        }
        if(command.contains(GRADABLE_EVENT_NAME))
        {
            command = replace(command, GRADABLE_EVENT_NAME, action.getPart().getGradableEvent().getName());
        }
        if(command.contains(PART_NAME))
        {
            command = replace(command, PART_NAME, action.getPart().getName());
        }
        if(command.contains(ACTION_NAME))
        {
            command = replace(command, ACTION_NAME, action.getName());
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