package cakehat.database.assignment;

import cakehat.database.Group;
import java.util.Set;

/**
 * An implementation of an {@link PartActionDescription.ActionType} for a {@link Part}.
 *
 * @author jak2
 */
interface PartAction
{
    /**
     * Whether the action is supported. If the action is not supported for the provided arguments then
     * {@link #performAction(ActionContext, Part, Set<Group>)} should not be called with those arguments.
     * 
     * @param part
     * @param groups
     * @return
     * @throws ActionException 
     */
    boolean isActionSupported(Part part, Set<Group> groups) throws ActionException;
    
    /**
     * Performs the action for the specified part and groups.
     * 
     * @param context
     * @param part
     * @param groups
     * @return
     * @throws ActionException 
     */
    ActionResult performAction(ActionContext context, Part part, Set<Group> groups) throws ActionException;
}