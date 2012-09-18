package cakehat.assignment;

import cakehat.database.Group;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A convenience partial implementation of {@code Task} for tasks which only support operating on a single group at
 * a time.
 *
 * @author jak2
 */
abstract class SingleGroupTask extends Task
{
    SingleGroupTask(TaskProvider provider, String name)
    {
        super(provider, name);
    }
    
    @Override
    public void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Set<Group> groups)
            throws TaskException, TaskConfigurationIssue
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
        
        this.performTask(properties, context, action, group);
    }
    
    abstract void performTask(Map<TaskProperty, String> properties, TaskContext context, Action action, Group group)
            throws TaskException, TaskConfigurationIssue;

    @Override
    public boolean isTaskSupported(Action action, Set<Group> groups)
    {
        return (groups.size() == 1);
    }
}