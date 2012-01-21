package cakehat.assignment;

import cakehat.assignment.PartActionDescription.ActionType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provides {@link PartActionDescription}s that describe implementations of {@link PartAction}. Creates
 * {@code PartAction}s in a package private manner.
 *
 * @author jak2
 */
public class ActionRepository
{
    private static class ActionRepositoryException extends RuntimeException
    {
        ActionRepositoryException(String msg)
        {
            super(msg);
        }
    }
    
    private static final ActionRepository INSTANCE = new ActionRepository();
    
    public static ActionRepository get()
    {
        return INSTANCE;
    }
    
    private final ImmutableMap<String, PartActionDescription> _descriptions;

    private ActionRepository()
    {
        ImmutableMap.Builder<String, PartActionDescription> descriptionsBuilder = ImmutableMap.builder();
        
        this.addActionDescriptions(descriptionsBuilder, new JavaActions());
        this.addActionDescriptions(descriptionsBuilder, new MatlabActions());
        this.addActionDescriptions(descriptionsBuilder, new PrintActions());
        this.addActionDescriptions(descriptionsBuilder, new ApplicationActions());
        this.addActionDescriptions(descriptionsBuilder, new ExternalActions());
        
        _descriptions = descriptionsBuilder.build();
    }
    
    private void addActionDescriptions(ImmutableMap.Builder<String, PartActionDescription> descriptionsBuilder,
            ActionProvider provider)
    {
        for(PartActionDescription description : provider.getActionDescriptions())
        {
            descriptionsBuilder.put(description.getFullName(), description);
        }
    }
    
    /**
     * Returns an immutable collection of all {@link PartActionDescription} implementations.
     * 
     * @return 
     */
    public Collection<PartActionDescription> getAllActionDescriptions()
    {
        return _descriptions.values();
    }
    
    /**
     * Returns an immutable list of {@link PartActionDescription} implementations with a suggested type of {@code type}.
     * The list is sorted alphabetically by {@link PartActionDescription#getFullName()}.
     * 
     * @param type
     * @return 
     */
    public List<PartActionDescription> getSuggestedActionDescriptions(ActionType type)
    {
        ArrayList<PartActionDescription> suggested = new ArrayList<PartActionDescription>();
        for(PartActionDescription description : _descriptions.values())
        {
            if(description.getSuggestedTypes().contains(type))
            {
                suggested.add(description);
            }
        }
        Collections.sort(suggested, new Comparator<PartActionDescription>()
        {
            @Override
            public int compare(PartActionDescription d1, PartActionDescription d2)
            {
                return d1.getFullName().compareTo(d2.getFullName());
            }
        });
        
        return ImmutableList.copyOf(suggested);
    }
    
    /**
     * Returns an immutable list of {@link PartActionDescription} implementations with a compatible type of
     * {@code type}. The list is sorted alphabetically by {@link PartActionDescription#getFullName()}.
     * 
     * @param type
     * @return 
     */
    public List<PartActionDescription> getCompatibleActionDescriptions(ActionType type)
    {
        ArrayList<PartActionDescription> compatible = new ArrayList<PartActionDescription>();
        for(PartActionDescription description : _descriptions.values())
        {
            if(description.getCompatibleTypes().contains(type))
            {
                compatible.add(description);
            }
        }
        Collections.sort(compatible, new Comparator<PartActionDescription>()
        {
            @Override
            public int compare(PartActionDescription d1, PartActionDescription d2)
            {
                return d1.getFullName().compareTo(d2.getFullName());
            }
        });
        
        return ImmutableList.copyOf(compatible);
    }

    /**
     * Returns the action specified by {@code actionName}.
     *
     * @param intendendType the type this action will be used for
     * @param actionName the full name
     * @param parsedProperties map of key to value as specified in database
     *
     * @return action
     *
     * @throws ActionRespositoryException thrown if there is no action by the name of {@code actionName}, the action is
     * incompatible with {@code intendedType}, or if the properties map is lacking required properties or includes
     * nonexistent properties for the action
     */
    PartAction getAction(ActionType intendendType, String actionName, Map<String, String> parsedProperties)
    {
        if(_descriptions.containsKey(actionName))
        {
            PartActionDescription description = _descriptions.get(actionName);

            if(!description.getCompatibleTypes().contains(intendendType))
            {
                throw new ActionRepositoryException(description.getFullName() + " is not compatible with type: " +
                        intendendType + "\n" + "Compatible types: " + description.getCompatibleTypes());
            }

            PartAction action = description.getAction(this.convertProperties(description, parsedProperties));

            return action;
        }
        else
        {
            throw new ActionRepositoryException("Unknown part action name [" + actionName + "]");
        }
    }

    /**
     * Takes in the properties as map of strings from property name to property value. An immutable map is returned from
     * the object representation of the property ({@link PartActionProperty}) instead of the just name to the value.
     * <br/><br/>
     * Checks that all required properties of the {@code description} are present and that no properties are specified
     * which do not belong to the {@code description}.
     *
     * @param description
     * @param parsedProperties
     * 
     * @throws ActionRepositoryException if one of the checks fails
     */
    private Map<PartActionProperty, String> convertProperties(PartActionDescription description,
            Map<String, String> parsedProperties)
    {
        Set<PartActionProperty> descriptionProperties = description.getProperties();
        ImmutableMap.Builder<PartActionProperty, String> convertedProperties = ImmutableMap.builder();

        //Check that each required property exists
        //"Converts" all properties from String to DistributableProperty representation
        for(PartActionProperty actualProperty : descriptionProperties)
        {
            //If this property is represented in the parsed properties
            if(parsedProperties.containsKey(actualProperty.getName()))
            {
                //Add this "converted" property
                convertedProperties.put(actualProperty, parsedProperties.get(actualProperty.getName()));
            }
            //If not represented and it is required
            else if(actualProperty.isRequired())
            {
                throw new ActionRepositoryException("Required property [" + actualProperty.getName() + "] for [" +
                        description.getFullName() + "]" + " was not provided in configuration");
            }
        }

        //Check that each property specified in the configuration is a property that belongs to the description
        for(String parsedPropertyName : parsedProperties.keySet())
        {
            boolean propertyExists = false;
            for(PartActionProperty actualProperty : descriptionProperties)
            {
                if(actualProperty.getName().equals(parsedPropertyName))
                {
                    propertyExists = true;
                    break;
                }
            }

            if(!propertyExists)
            {
                throw new ActionRepositoryException("Nonexistent property [" + parsedPropertyName +
                        "] was specified for [" + description.getFullName() + "] in the configuration");
            }
        }

        return convertedProperties.build();
    }
}