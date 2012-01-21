package cakehat.assignment;

import cakehat.newdatabase.Group;
import java.util.Collection;

/**
 * An implementation of a mode (run, open, demo, test, and print) for a {@link Part}.
 *
 * @author jak2
 */
interface PartAction
{
    /**
     * Performs an action for the specified group.
     *
     * @param part the Part this action is being performed for
     * @param group
     */
    public void performAction(Part part, Group group) throws ActionException;

    /**
     * Performs an action for the specified groups. Only applicable for printing.
     *
     * @param part the Part this action is being performed for
     * @param groups
     */
    public void performAction(Part part, Collection<Group> groups) throws ActionException;
}