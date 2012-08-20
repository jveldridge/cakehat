package cakehat.database.assignment;

import cakehat.database.Group;
import java.util.Set;

/**
 * A convenience partial implementation of {@code Task} for tasks which support operating on one or more groups at a
 * time.
 *
 * @author jak2
 */
abstract class MultiGroupTask extends Task
{
    MultiGroupTask(TaskProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public boolean isTaskSupported(Action action, Set<Group> groups)
    {
        return !groups.isEmpty();
    }
}