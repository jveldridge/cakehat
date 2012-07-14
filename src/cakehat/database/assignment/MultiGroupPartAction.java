package cakehat.database.assignment;

import cakehat.database.Group;
import java.util.Set;

/**
 * A convenience partial implementation of {@code PartAction} for actions which support operating on one or more groups
 * at a time.
 *
 * @author jak2
 */
abstract class MultiGroupPartAction implements PartAction
{
    @Override
    public boolean isActionSupported(Part part, Set<Group> groups)
    {
        return !groups.isEmpty();
    }
}