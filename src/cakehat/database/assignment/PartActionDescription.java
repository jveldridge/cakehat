package cakehat.database.assignment;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;

/**
 * Describes an action that will be used for running, testing, demoing, opening, printing, grading guide, and readmes.
 *
 * @author jak2
 */
public abstract class PartActionDescription
{
    public static enum ActionType
    {
        RUN(true, true, "Run"),
        OPEN(true, true, "Open"),
        TEST(true, true, "Test"),
        PRINT(true, true, "Print"),
        DEMO(false, true, "Demo"),
        README(true, false, "Readme"),
        GRADING_GUIDE(false, true, "Grading Guide");
        
        private final boolean _requiresDigitalHandin;
        private final boolean _userConfigurable;
        private final String _userFriendlyName;
        
        private ActionType(boolean requiresDigitalHandin, boolean userConfigurable, String userFriendlyName)
        {
            _requiresDigitalHandin = requiresDigitalHandin;
            _userConfigurable = userConfigurable;
            _userFriendlyName = userFriendlyName;
        }
        
        /**
         * Whether the type requires that the part operate on digital handins.
         * 
         * @return 
         */
        public boolean requiresDigitalHandin()
        {
            return _requiresDigitalHandin;
        }
        
        /**
         * Whether the action for this type may be configured by the user in the config manager. 
         * 
         * @return 
         */
        public boolean isUserConfigurable()
        {
            return _userConfigurable;
        }
        
        public String getUserFriendlyName()
        {
            return _userFriendlyName;
        }
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
     * Returns the required properties of this action.
     * 
     * @return 
     */
    public Set<PartActionProperty> getRequiredProperties()
    {
        ImmutableSet.Builder<PartActionProperty> required = ImmutableSet.builder();
        for(PartActionProperty prop : getProperties())
        {
            if(prop.isRequired())
            {
                required.add(prop);
            }
        }
       
        return required.build();
    }
    
    /**
     * Returns the optional properties of this action. 
     * 
     * @return 
     */
    public Set<PartActionProperty> getOptionalProperties()
    {
       ImmutableSet.Builder<PartActionProperty> optional = ImmutableSet.builder();
        for(PartActionProperty prop : getProperties())
        {
            if(!prop.isRequired())
            {
                optional.add(prop);
            }
        }
       
        return optional.build(); 
    }

    /**
     * Actions may be used interchangeably for a given action type so long as if the action requires a digital handin
     * that action type also require a digital handin. For instance a type that opens files in Kate would have a
     * suggested type of OPEN, but could be configured to be the RUN type. The types returned by this method are only
     * suggested uses.
     *
     * @return
     */
    public abstract Set<ActionType> getSuggestedTypes();
    
    /**
     * Whether the action operates on a digital handin. If so, then the action should only map to an action type that
     * requires a digital handin. If mapped to an action type that does not support digital handins then the action
     * will never be eligible to perform its action.
     * 
     * @return 
     */
    public abstract boolean requiresDigitalHandin();

    /**
     * Returns an implementation described by this action that has the property values provided.
     *
     * @param properties
     * @return
     */
    abstract PartAction getAction(Map<PartActionProperty, String> properties);
}