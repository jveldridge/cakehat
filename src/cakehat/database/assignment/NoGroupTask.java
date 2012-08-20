package cakehat.database.assignment;

import cakehat.database.Group;
import java.util.Map;
import java.util.Set;

/**
 * A convenience partial implementation of {@code Task} for tasks which do not require any groups to operate on.
 *
 * @author jak2
 */
abstract class NoGroupTask extends Task
{
    NoGroupTask(TaskProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Set<Group> groups)
            throws TaskException, TaskConfigurationIssue
    {
        this.performTask(properties, context, action);
    }
    
    abstract void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action)
        throws TaskException, TaskConfigurationIssue;

    @Override
    public boolean isTaskSupported(Action action, Set<Group> groups)
    {
        return true;
    }
}