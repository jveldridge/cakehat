package cakehat.config.handin;

import cakehat.config.ConfigurationException;
import cakehat.config.handin.DistributableActionDescription.ActionMode;
import cakehat.views.shared.TextViewerView;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates {@link DistributableAction}s while verifying the supplied properties
 * contain all required properties.
 *
 * @author jak2
 */
public class ActionRepository
{
    private final Map<String, DistributableActionDescription> _descriptions;

    public ActionRepository()
    {
        _descriptions = new HashMap<String, DistributableActionDescription>();

        this.addActionDescriptions(new JavaActions());
        this.addActionDescriptions(new MatlabActions());
        this.addActionDescriptions(new PrintActions());
        this.addActionDescriptions(new ApplicationActions());
        this.addActionDescriptions(new ExternalActions());
    }

    /**
     * Until the Configuration GUI is created, this method is a stopgap measure.
     */
    private String getActionsInfo()
    {
        StringBuffer buffer = new StringBuffer();

        for(String name : _descriptions.keySet())
        {
            DistributableActionDescription description = _descriptions.get(name);

            buffer.append("\n");

            buffer.append(name + "\n");
            buffer.append(description.getDescription() + "\n");
            buffer.append("\n");
            buffer.append("Suggested Modes: ");
            for(DistributableActionDescription.ActionMode mode : description.getSuggestedModes())
            {
                buffer.append(mode + " ");
            }
            if(!description.getProperties().isEmpty())
            {
                buffer.append("\n\n");
                buffer.append("Properties:\n");
                for(DistributableActionProperty property : description.getProperties())
                {
                    buffer.append(property.getName() + " (");
                    if(property.isRequired())
                    {
                        buffer.append("REQUIRED)\n");
                    }
                    else
                    {
                        buffer.append("OPTIONAL)\n");
                    }
                    buffer.append(property.getDescription() + "\n\n");
                }
            }
            else
            {
                buffer.append("\n\n");
            }

            buffer.append("--------------------------------------------------------------\n");
        }

        return buffer.toString();
    }

    public void displayActionsInfo()
    {
        new TextViewerView(getActionsInfo(), "Available Actions");
    }

    private void addActionDescriptions(ActionProvider provider)
    {
        String namespace = provider.getNamespace();
        for(DistributableActionDescription description : provider.getActionDescriptions())
        {
            _descriptions.put(namespace + ":" + description.getName(), description);
        }
    }

    /**
     * Returns the action specified by <code>actionName</code>.
     *
     * @param intendendMode the mode this action will be used for
     * @param actionName the full name, including the namespace
     * @param parsedProperties map of KEY to VALUE as specified in configuration
     *
     * @return action
     *
     * @throws ConfigurationException thrown if there is no action by the name
     * of actionName, the action is incompatible with this mode, or if the
     * properties map is lacking required properties or includes non-existent
     * properties for the action
     */
    public DistributableAction getAction(ActionMode intendendMode, String actionName,
            Map<String, String> parsedProperties) throws ConfigurationException
    {
        if(_descriptions.containsKey(actionName))
        {
            DistributableActionDescription description = _descriptions.get(actionName);

            if(!description.getCompatibleModes().contains(intendendMode))
            {
                throw new ConfigurationException(getFullName(description) +
                        " is not compatible with mode: " + intendendMode + "\n" +
                        "Compatible modes: \n" +
                        Arrays.toString(description.getCompatibleModes().toArray()));
            }

            DistributableAction action =
                    description.getAction(this.convertProperties(description, parsedProperties));

            return action;
        }
        else
        {
            throw new ConfigurationException("[" + actionName + "] is not a valid mode.");
        }
    }

    /**
     * Takes in the properties as map of strings from property name to property
     * value. A map is returned from the object representation of the property
     * (DistributableProperty) instead of the just name to the value.
     *
     * Checks that all required properties of the <code>description</code> are
     * present and that no properties are specified which do not belong to the
     * <code>description</code>.
     *
     * @param description
     * @param parsedProperties
     *
     * @throws ConfigurationException
     */
    private Map<DistributableActionProperty, String> convertProperties(
            DistributableActionDescription description,
            Map<String, String> parsedProperties) throws ConfigurationException
    {
        List<DistributableActionProperty> descriptionProperties = description.getProperties();
        Map<DistributableActionProperty, String> convertedProperties = new HashMap<DistributableActionProperty, String>();

        //Check that each required property exists
        //"Converts" all properties from String to DistributableProperty representation
        for(DistributableActionProperty actualProperty : descriptionProperties)
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
                throw new ConfigurationException("Required property [" +
                        actualProperty.getName() + "] for [" +
                        getFullName(description) + "]" +
                        " was not provided in configuration.");
            }
        }

        //Check that each property specified in the configuration is a property
        //that belongs to the description
        for(String parsedPropertyName : parsedProperties.keySet())
        {
            boolean propertyExists = false;
            for(DistributableActionProperty actualProperty : descriptionProperties)
            {
                if(actualProperty.getName().equals(parsedPropertyName))
                {
                    propertyExists = true;
                }
            }

            if(!propertyExists)
            {
                throw new ConfigurationException("Non-existent property [" +
                        parsedPropertyName + "] was specified for [" +
                        getFullName(description) + "]" +
                        "] in the configuration.");
            }
        }

        return convertedProperties;
    }

    /**
     * Gets the full name (namespace:action-name) of the description.
     *
     * @param description
     * @return
     */
    private String getFullName(DistributableActionDescription description)
    {
        return description.getProvider().getNamespace() + ":" + description.getName();
    }
    
    //TESTING

    public static void main(String[] args) throws ConfigurationException
    {
        ActionRepository repository = new ActionRepository();

        repository.displayActionsInfo();
    }
}