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
    public TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part,
        Set<Group> groups) throws TaskException
    {
        return this.performTask(properties, context, part);
    }
    
    abstract TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part)
        throws TaskException;

    @Override
    public boolean isTaskSupported(Part part, Set<Group> groups)
    {
        return true;
    }
}