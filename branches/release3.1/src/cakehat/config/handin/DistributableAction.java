package cakehat.config.handin;

import cakehat.database.Group;
import java.util.Collection;

/**
 * An implementation of a mode (run, open, demo, test, and print) for a
 * {@link DistributablePart}.
 *
 * @author jak2
 */
public interface DistributableAction
{
    /**
     * Performs an action for the specified group.
     *
     * @param part the DistributablePart this action is being performed for
     * @param group
     */
    public void performAction(DistributablePart part, Group group) throws ActionException;

    /**
     * Performs an action for the specified groups. Only applicable for
     * printing.
     *
     * @param part the DistributablePart this action is being performed for
     * @param groups
     */
    public void performAction(DistributablePart part, Collection<Group> groups) throws ActionException;
}