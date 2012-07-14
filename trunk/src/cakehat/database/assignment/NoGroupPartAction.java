package cakehat.database.assignment;

import cakehat.database.Group;
import java.util.Set;

/**
 * A convenience partial implementation of {@code PartAction} for actions which does not require any groups to operate
 * on. An example implementation of this is an action which runs a demo.
 *
 * @author jak2
 */
abstract class NoGroupPartAction implements PartAction
{
    @Override
    public ActionResult performAction(ActionContext context, Part part, Set<Group> groups) throws ActionException
    {
        return this.performAction(context, part);
    }
    
    abstract ActionResult performAction(ActionContext context, Part part) throws ActionException;

    @Override
    public boolean isActionSupported(Part part, Set<Group> groups)
    {
        return true;
    }
}