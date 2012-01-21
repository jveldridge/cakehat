package cakehat.assignment;

import java.util.Map;
import java.util.Set;

/**
 * Describes an action that will be used for running, testing, demoing, opening, and printing.
 *
 * @author jak2
 */
public abstract class PartActionDescription
{
    public static enum ActionType
    {
        RUN, OPEN, TEST, DEMO, PRINT;
    }
    
    private final String _fullName;
    
    PartActionDescription(ActionProvider provider, String actionName)
    {
        _fullName = provider.getNamespace() + ":" + actionName;
    }
    
    /**
     * The fully qualified name of this action.
     * 
     * @return 
     */
    public String getFullName()
    {
        return _fullName;
    }

    /**
     * A human readable description of the action. This description will be displayed to users in the cakehat
     * configuration manager.
     *
     * @return
     */
    public abstract String getDescription();

    /**
     * Returns the properties this action either requires or optionally allows.
     * 
     * @return
     */
    public abstract Set<PartActionProperty> getProperties();

    /**
     * Types are RUN, OPEN, TEST, DEMO & PRINT.
     * <br/><br/>
     * Generally speaking actions may be used interchangeably. For instance a type that opens files in Kate would have a
     * suggested type of OPEN, but could be configured to be the RUN type. The types returned by this method are only
     * suggested uses.
     * <br/><br/>
     * There are some fundamental limitations, so not all actions can be used for all types. Consult
     * {@link #getCompatibleTypes()} for this.
     *
     * @return
     */
    public abstract Set<ActionType> getSuggestedTypes();

    /**
     * Types are RUN, OPEN, TEST, DEMO & PRINT.
     * <br/><br/>
     * Running, opening, and testing code works for one group at a time. Demos operate on no groups. Printing operates
     * on any number of groups at once. Due to these differences not all {@link PartAction}s can be used for all types.
     *
     * @return
     */
    public abstract Set<ActionType> getCompatibleTypes();

    /**
     * Returns an implementation described by this action that has the property values provided.
     *
     * @param properties
     * @return
     */
    abstract PartAction getAction(Map<PartActionProperty, String> properties);
}