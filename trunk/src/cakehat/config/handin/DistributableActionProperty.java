package cakehat.config.handin;

/**
 * The description of a property.
 *
 * @author jak2
 */
@Deprecated
public class DistributableActionProperty
{
    private final String _name;
    private final String _description;
    private final boolean _required;

    public DistributableActionProperty(String name, String description, boolean required)
    {
        _name = name;
        _description = description;
        _required = required;
    }

    /**
     * The name of the property.
     *
     * @return
     */
    public String getName()
    {
        return _name;
    }

    /**
     * A human readable description of the property. This description will be
     * displayed to users of cakehat when setting up the configuration file.
     *
     * @return
     */
    public String getDescription()
    {
        return _description;
    }

    /**
     * Whether this property is required.
     *
     * @return
     */
    public boolean isRequired()
    {
        return _required;
    }
    
    @Override
    public String toString() {
        return _name;
    }
    
}