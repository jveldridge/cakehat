package cakehat.database.assignment;

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
    public TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part,
        Set<Group> groups) throws TaskException
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
        
        return this.performTask(properties, context, part, group);
    }
    
    abstract TaskResult performTask(Map<TaskProperty, String> properties, TaskContext context, Part part, Group group)
            throws TaskException;

    @Override
    public boolean isTaskSupported(Part part, Set<Group> groups)
    {
        return (groups.size() == 1);
    }
}