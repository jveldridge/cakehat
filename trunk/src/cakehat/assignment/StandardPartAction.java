package cakehat.assignment;

import cakehat.database.Group;
import java.util.Collection;

/**
 * A convenience partial implementation of PartAction which always throws an exception if the
 * {@link #performAction(cakehat.assignment.Part, java.util.Collection) } method is invoked. This class is intended to
 * be used to simplify creating {@code PartAction}s for RUN, TEST, DEMO and OPEN modes as only the PRINT mode needs to
 * be able to take in multiple groups.
 *
 * @author jak2
 */
abstract class StandardPartAction implements PartAction
{
    public void performAction(Part part, Collection<Group> groups) throws ActionException
    {
        throw new ActionException("This type of action is only intended for printing");
    }
}