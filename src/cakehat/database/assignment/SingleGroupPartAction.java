package cakehat.database.assignment;

import cakehat.database.Group;
import java.util.Iterator;
import java.util.Set;

/**
 * A convenience partial implementation of {@code PartAction} for actions which only support operating on a single
 * group at a time.
 *
 * @author jak2
 */
abstract class SingleGroupPartAction implements PartAction
{
    @Override
    public ActionResult performAction(ActionContext context, Part part, Set<Group> groups) throws ActionException
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
        
        return this.performAction(context, part, group);
    }
    
    abstract ActionResult performAction(ActionContext context, Part part, Group group) throws ActionException;

    @Override
    public boolean isActionSupported(Part part, Set<Group> groups)
    {
        return (groups.size() == 1);
    }
}